# Exhibition Management System

This repository contains a Spring Boot microservice for managing exhibitions, modules, stalls, and poster content.

## Local development

1. Build:

```bash
mvn -DskipTests package
```

2. Run locally using H2 (dev profile is preconfigured):

```bash
java -jar target/exhibition-management-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

3. Run locally connecting to Azure SQL (production-like):

```bash
# set DB password as environment variable (do NOT commit this)
export AZ_SQL_SERVER_PASSWORD='your-db-password'

# build (driver is included in pom)
mvn -DskipTests package

# run with azure profile (uses src/main/resources/application-azure.properties)
java -jar target/exhibition-management-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=azure
```

## Flyway migrations

- Migrations are in `src/main/resources/db/migration/` and will be applied automatically by Flyway at startup for the active profile.
- The repository contains SQL Server-compatible migrations (V1 and V2) for use against Azure SQL.

## Azure DB & Flyway note (important)

By default the `azure` profile has automatic Flyway migrations disabled to avoid startup failures seen in some Azure SQL compatibility configurations (error: "Unsupported Database: Microsoft SQL Server 12.0").

You should run the Flyway migrations against the Azure SQL database manually (once) before starting the app in the `azure` profile. Two simple approaches:

### Option A — Use `sqlcmd` (recommended for manual runs)

1. Install `sqlcmd` (part of Microsoft ODBC tools) for your OS.
2. Run the migrations from the `src/main/resources/db/migration` folder in order:

```bash
export AZ_SQL_SERVER_PASSWORD='your-db-password'
SQLCMD="sqlcmd -S sai-sqlserver.database.windows.net -U sai-admin@sai-sqlserver -P $AZ_SQL_SERVER_PASSWORD -d saikvtdb -N"

# Example: run V1 then V2
$SQLCMD -i src/main/resources/db/migration/V1__create_exhibition_table.sql
$SQLCMD -i src/main/resources/db/migration/V2__create_module_stall_poster_tables.sql
```

### Option B — Use Azure CLI (run a one-off command to execute SQL)

You can execute SQL statements with `az sql db` or by using `az webapp ssh` into a machine that has access to the DB; for simple migration files, it's easiest to use `sqlcmd` locally or from a secure VM in your VNet.

### After migrations

Once the schema is applied, start the app with the `azure` profile (it will not attempt to run Flyway on startup):

```bash
export AZ_SQL_SERVER_PASSWORD='your-db-password'
java -jar target/exhibition-management-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=azure
```

If you prefer automatic migrations on startup, you can re-enable `spring.flyway.enabled=true` in `application-azure.properties` after verifying the DB compatibility level and Flyway behavior against your Azure SQL instance.

## Swagger & Actuator

- Swagger UI: `/swagger-ui.html` (e.g. `http://localhost:8080/swagger-ui.html`)
- Health: `/actuator/health` (e.g. `http://localhost:8080/actuator/health`)

## GitHub Actions: Deploy to Azure

Workflow path: `.github/workflows/azure-deploy.yml`

### Required repository secrets

- `AZURE_WEBAPP_PUBLISH_PROFILE` — contents of the Azure Web App publish profile (XML) (used to deploy the JAR and configure App Settings).
- `AZ_SQL_SERVER_PASSWORD` — password for the Azure SQL user (e.g., `sai-admin@sai-sqlserver`).

### How the workflow works

- On push to `main`, the workflow builds the JAR, deploys it to the specified Azure Web App using the publish profile, and sets the `AZ_SQL_SERVER_PASSWORD` application setting on the Web App so the app can pick it up at runtime.

### Notes

- Ensure the SQL Server firewall allows your Web App to reach it (enable `Allow Azure services` or add the Web App outbound IPs).
- For production, consider storing DB credentials in Azure Key Vault and referencing them from App Service.
