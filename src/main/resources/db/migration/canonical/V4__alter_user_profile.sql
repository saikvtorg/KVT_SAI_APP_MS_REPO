-- V4: Idempotent alter to add country and address to app_user_profile (SQL Server)

IF NOT EXISTS (
  SELECT 1 FROM sys.columns
  WHERE Name = N'country' AND Object_ID = Object_ID(N'dbo.app_user_profile')
)
BEGIN
  ALTER TABLE dbo.app_user_profile ADD country VARCHAR(100);
END
GO

IF NOT EXISTS (
  SELECT 1 FROM sys.columns
  WHERE Name = N'address' AND Object_ID = Object_ID(N'dbo.app_user_profile')
)
BEGIN
  ALTER TABLE dbo.app_user_profile ADD address VARCHAR(MAX);
END
GO

