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

