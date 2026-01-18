# Production DB Migration - combined script

This document explains how to safely run the combined canonical migration script located at

`src/main/resources/prod/all_canonical_migrations.sql`

It includes exact `sqlcmd` commands you can run from a workstation or CI runner, verification queries, duplicate detection queries, and recommended safe steps (backup, staging, rollback guidance).

---

## Important safety notes (read first)

- BACKUP your production database before running any DDL. Do not skip backups.
- Run this first on a staging copy that mirrors prod (same compatibility level) to verify behavior.
- The combined script is idempotent and guarded, but some operations (like adding unique constraints) will be skipped if duplicates exist. You must clean duplicates first if you want to enforce uniqueness.
- The script contains `GO` batch separators and T-SQL features. Use `sqlcmd` (or Azure Data Studio / SSMS) which understands `GO`.

---

## Pre-requisites

- `sqlcmd` client installed on the machine that will run the script. On macOS you can install Microsoft ODBC + mssql-tools or use Docker. Example:

  - macOS (Homebrew + mssql-tools):

```bash
# Install prerequisites (macOS example)
brew tap microsoft/mssql-release https://github.com/Microsoft/homebrew-mssql-release
brew update
HOMEBREW_NO_AUTO_UPDATE=1 brew install --no-sandbox msodbcsql17 mssql-tools
# Optionally add to PATH
echo 'export PATH="$PATH:/usr/local/opt/mssql-tools/bin"' >> ~/.zshrc
source ~/.zshrc
```

- Ensure you have an admin or sufficiently privileged SQL user to alter schema and create constraints.
- Ensure you have a tested backup/restore plan (automated backups, point-in-time restore, or bacpac).

---

## Environment variables (recommended)

Set these environment variables in your shell (or CI) before running commands. Replace values with your Azure SQL details.

```bash
export AZ_SQL_SERVER="<your-server>.database.windows.net"
export AZ_SQL_DB="<your-database>"
export AZ_SQL_USER="<admin-user>@<your-server>"   # e.g. sai-admin@saikvt-sqlserver
export AZ_SQL_PASSWORD="<your-db-password>"     # keep this secret
```

Note: On Azure, the full login name typically looks like `adminuser@servername`.

---

## Run the combined migration script (sqlcmd)

From the repository root (where `src/main/resources/prod/all_canonical_migrations.sql` is located), run:

```bash
# one-shot: run the combined migrations against the target DB
sqlcmd -S "tcp:${AZ_SQL_SERVER},1433" -d "$AZ_SQL_DB" -U "$AZ_SQL_USER" -P "$AZ_SQL_PASSWORD" -N -i src/main/resources/prod/all_canonical_migrations.sql
```

- `-N` enables encryption (recommended). Adjust host/port if needed.
- If your password contains special shell characters, wrap appropriately or use a secure secret injection mechanism in CI.

If you prefer to see output in real time, omit `-b` (we did not include `-b`). If `sqlcmd` exits non-zero it will print an error — inspect the message.

### Running via Azure Cloud Shell

You can also run the script from Azure Cloud Shell (Bash) using `sqlcmd` available there. Upload the SQL file (e.g., to a storage container or use the editor), then run the same `sqlcmd` invocation.

---

## Flyway alternative (CI-friendly)

If you want to run migrations via Flyway in CI rather than `sqlcmd`, you can use the Flyway Maven plugin (example):

```bash
mvn -DskipTests org.flywaydb:flyway-maven-plugin:migrate \
  -Dflyway.url="jdbc:sqlserver://${AZ_SQL_SERVER}:1433;database=${AZ_SQL_DB}" \
  -Dflyway.user="${AZ_SQL_USER}" \
  -Dflyway.password="${AZ_SQL_PASSWORD}" \
  -Dflyway.locations=filesystem:src/main/resources/db/migration/canonical
```

This runs each canonical migration file in order. Use this approach if you want migration history tracking in Flyway's schema history table.

---

## Verification queries (run after the script)

1) Check that key tables exist:

```sql
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME IN (
  'exhibition','module','stall','poster_content','app_user_profile','user_feedback','user_quiz_result'
);
```

2) Check `app_user_profile` columns and `role` default:

```sql
-- list columns
SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'app_user_profile' ORDER BY ORDINAL_POSITION;

-- see top row to verify default role
SELECT TOP 10 user_id, email, phone, role FROM dbo.app_user_profile ORDER BY user_id;
```

3) Verify constraints (sample checks):

```sql
-- Check unique constraints by name
SELECT name, type_desc FROM sys.objects WHERE name LIKE 'UQ_app_user_profile_%' OR name = 'UQ_app_user_profile_email' OR name = 'UQ_app_user_profile_phone';

-- Check CHECK constraint for role
SELECT name, definition FROM sys.check_constraints WHERE name = 'CK_app_user_profile_role';
```

4) Verify FKs were created:

```sql
SELECT fk.name, OBJECT_NAME(fk.parent_object_id) AS parent_table, OBJECT_NAME(fk.referenced_object_id) AS referenced_table
FROM sys.foreign_keys fk
WHERE OBJECT_NAME(fk.parent_object_id) IN ('user_feedback','user_quiz_result','module','stall','poster_content');
```

---

## Duplicate detection queries (run before attempting UNIQUE enforcement)

Run these to find problematic duplicates that would prevent adding unique constraints on `email`/`phone`.

**Duplicate emails (case-insensitive):**

```sql
SELECT LOWER(email) AS normalized_email, COUNT(*) AS cnt
FROM dbo.app_user_profile
WHERE email IS NOT NULL
GROUP BY LOWER(email)
HAVING COUNT(*) > 1
ORDER BY cnt DESC;
```

**Rows for duplicate emails:**

```sql
SELECT a.*
FROM dbo.app_user_profile a
JOIN (
  SELECT LOWER(email) AS normalized_email
  FROM dbo.app_user_profile
  WHERE email IS NOT NULL
  GROUP BY LOWER(email)
  HAVING COUNT(*) > 1
) d ON LOWER(a.email) = d.normalized_email
ORDER BY LOWER(a.email), a.user_id;
```

**Duplicate phones (normalized):**

```sql
SELECT normalized_phone, COUNT(*) AS cnt
FROM (
  SELECT user_id, phone,
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(phone, ' ', ''), '-', ''), '(', ''), ')', ''), '.', '') AS normalized_phone
  FROM dbo.app_user_profile
  WHERE phone IS NOT NULL
) t
GROUP BY normalized_phone
HAVING COUNT(*) > 1
ORDER BY cnt DESC;
```

**Rows for normalized duplicate phones:**

```sql
SELECT a.*
FROM (
  SELECT *, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(phone, ' ', ''), '-', ''), '(', ''), ')', ''), '.', '') AS normalized_phone
  FROM dbo.app_user_profile
  WHERE phone IS NOT NULL
) a
JOIN (
  SELECT REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(phone, ' ', ''), '-', ''), '(', ''), ')', ''), '.', '') AS normalized_phone
  FROM dbo.app_user_profile
  WHERE phone IS NOT NULL
  GROUP BY REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(phone, ' ', ''), '-', ''), '(', ''), ')', ''), '.', '')
  HAVING COUNT(*) > 1
) d ON a.normalized_phone = d.normalized_phone
ORDER BY a.normalized_phone, a.user_id;
```

---

## Safe dedupe pattern (preview before executing)

Use ROW_NUMBER() to select duplicates and preview rows that would be deleted (keeps rn = 1):

```sql
-- Preview duplicate email rows to delete (keep rn = 1)
WITH cte AS (
  SELECT user_id, email,
         ROW_NUMBER() OVER (PARTITION BY LOWER(email) ORDER BY user_id) AS rn
  FROM dbo.app_user_profile
  WHERE email IS NOT NULL
)
SELECT * FROM cte WHERE rn > 1 ORDER BY email, user_id;

-- To actually delete duplicates after review (run in transaction):
BEGIN TRANSACTION;
WITH cte AS (
  SELECT user_id,
         ROW_NUMBER() OVER (PARTITION BY LOWER(email) ORDER BY user_id) AS rn
  FROM dbo.app_user_profile
  WHERE email IS NOT NULL
)
DELETE a
FROM dbo.app_user_profile a
JOIN cte ON a.user_id = cte.user_id
WHERE cte.rn > 1;
-- COMMIT if results are correct
COMMIT TRANSACTION;
-- or ROLLBACK TRANSACTION;
```

For phone duplicates use normalized phone in the PARTITION BY expression (similar to earlier queries).

---

## Rollback / recovery notes

- Schema DDL is not always trivially reversible. If something goes wrong, restore the database from the backup taken prior to migration.
- Keep a copy (offline) of the `all_canonical_migrations.sql` that was executed together with the DB backups taken right before/after running it.

---

## Troubleshooting

- If `sqlcmd` errors with "Invalid column name 'X'" when running a script that references a newly-created column, make sure the script uses `GO` separators between the ALTER and statements that reference the new column (this combined file already does that for sensitive steps).
- If a constraint creation fails due to duplicate data, run the duplicate-report queries above to find offending rows and clean them (manual review recommended) before re-running the script.

---

## Example CI snippet (GitHub Actions) to run script using a short-lived runner

```yaml
# run-migrations.yml (example)
name: Run prod db migrations
on: workflow_dispatch
jobs:
  migrate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Install sqlcmd dependencies (Debian/Ubuntu)
        run: |
          sudo apt-get update
          sudo ACCEPT_EULA=Y apt-get install -y msodbcsql17 unixodbc-dev
          curl https://packages.microsoft.com/keys/microsoft.asc | sudo apt-key add -
          # install mssql-tools if needed
      - name: Run combined migrations
        env:
          AZ_SQL_SERVER: ${{ secrets.AZ_SQL_SERVER }}
          AZ_SQL_DB: ${{ secrets.AZ_SQL_DB }}
          AZ_SQL_USER: ${{ secrets.AZ_SQL_USER }}
          AZ_SQL_PASSWORD: ${{ secrets.AZ_SQL_PASSWORD }}
        run: |
          sqlcmd -S "tcp:${AZ_SQL_SERVER},1433" -d "$AZ_SQL_DB" -U "$AZ_SQL_USER" -P "$AZ_SQL_PASSWORD" -N -i src/main/resources/prod/all_canonical_migrations.sql
```

---

If you'd like, I can also:

- (B) generate the dedupe SQL specifically for your current DB contents if you run and paste the duplicate-report query outputs, or
- (C) add a short verification script that runs the verification queries automatically after migration and reports a concise pass/fail summary.

Tell me which of B/C you'd like next, or confirm you want me to proceed with anything else (e.g., pushing this README into your `prod` branch).
