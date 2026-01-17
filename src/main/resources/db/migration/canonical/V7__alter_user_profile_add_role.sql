-- V7: Add role column to app_user_profile and set default to 'normal' (SQL Server canonical)

-- Add column using dynamic SQL so subsequent batches can reference it without parse-time errors
IF NOT EXISTS (
  SELECT 1 FROM sys.columns
  WHERE Name = N'role' AND Object_ID = Object_ID(N'dbo.app_user_profile')
)
BEGIN
  EXEC('ALTER TABLE dbo.app_user_profile ADD role VARCHAR(20) CONSTRAINT DF_app_user_profile_role DEFAULT (''normal'')');
END
GO

-- Update existing rows to default if null (guarded and in its own batch)
IF EXISTS (
  SELECT 1 FROM sys.columns
  WHERE Name = N'role' AND Object_ID = Object_ID(N'dbo.app_user_profile')
)
BEGIN
  UPDATE dbo.app_user_profile SET role = 'normal' WHERE role IS NULL;
END
GO

-- Add check constraint for allowed values if not exists (guarded)
IF NOT EXISTS (
  SELECT 1 FROM sys.check_constraints WHERE name = N'CK_app_user_profile_role'
)
BEGIN
  IF EXISTS (
    SELECT 1 FROM sys.columns
    WHERE Name = N'role' AND Object_ID = Object_ID(N'dbo.app_user_profile')
  )
  BEGIN
    ALTER TABLE dbo.app_user_profile
    ADD CONSTRAINT CK_app_user_profile_role CHECK (role IN ('admin','normal'));
  END
END
GO
