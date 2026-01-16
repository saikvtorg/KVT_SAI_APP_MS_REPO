-- V4: Idempotent alter to add country, address and password_hash to app_user_profile (SQL Server)

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

IF NOT EXISTS (
  SELECT 1 FROM sys.columns
  WHERE Name = N'gender' AND Object_ID = Object_ID(N'dbo.app_user_profile')
)
BEGIN
  ALTER TABLE dbo.app_user_profile ADD gender VARCHAR(50);
END
GO

IF NOT EXISTS (
  SELECT 1 FROM sys.columns
  WHERE Name = N'password_hash' AND Object_ID = Object_ID(N'dbo.app_user_profile')
)
BEGIN
  ALTER TABLE dbo.app_user_profile ADD password_hash VARCHAR(255);
END
GO

-- === EMAIL: make NOT NULL and add unique constraint safely ===
-- If duplicates exist, print message and skip enforcement; admin must clean duplicates first.
IF EXISTS (
  SELECT email FROM dbo.app_user_profile WHERE email IS NOT NULL
  GROUP BY email
  HAVING COUNT(*) > 1
)
BEGIN
  PRINT 'V4 migration: duplicate EMAIL values exist - please resolve duplicates before making email NOT NULL / UNIQUE.';
END
ELSE
BEGIN
  -- Drop all unique constraints that reference email
  DECLARE @constraintName NVARCHAR(200);
  DECLARE constraint_cursor CURSOR FOR
    SELECT tc.name
    FROM sys.key_constraints tc
    JOIN sys.index_columns ic ON tc.parent_object_id = ic.object_id
    JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
    WHERE tc.parent_object_id = OBJECT_ID('dbo.app_user_profile')
      AND c.name = 'email'
      AND tc.[type] = 'UQ';

  OPEN constraint_cursor;
  FETCH NEXT FROM constraint_cursor INTO @constraintName;
  WHILE @@FETCH_STATUS = 0
  BEGIN
    EXEC('ALTER TABLE dbo.app_user_profile DROP CONSTRAINT [' + @constraintName + ']');
    FETCH NEXT FROM constraint_cursor INTO @constraintName;
  END
  CLOSE constraint_cursor;
  DEALLOCATE constraint_cursor;

  -- Drop any unique indexes that include email
  DECLARE @ixName NVARCHAR(200);
  DECLARE ix_cursor CURSOR FOR
    SELECT DISTINCT i.name
    FROM sys.indexes i
    JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
    JOIN sys.columns col ON ic.object_id = col.object_id AND ic.column_id = col.column_id
    WHERE i.object_id = OBJECT_ID('dbo.app_user_profile') AND col.name = 'email' AND i.is_unique = 1;

  OPEN ix_cursor;
  FETCH NEXT FROM ix_cursor INTO @ixName;
  WHILE @@FETCH_STATUS = 0
  BEGIN
    DECLARE @dropSql NVARCHAR(400) = 'DROP INDEX [' + @ixName + '] ON dbo.app_user_profile';
    EXEC(@dropSql);
    FETCH NEXT FROM ix_cursor INTO @ixName;
  END
  CLOSE ix_cursor;
  DEALLOCATE ix_cursor;

  -- Now alter the column to NOT NULL
  ALTER TABLE dbo.app_user_profile ALTER COLUMN email VARCHAR(255) NOT NULL;

  -- Recreate named unique constraint if not exists
  IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE name = N'UQ_app_user_profile_email' AND type = 'UQ')
  BEGIN
    ALTER TABLE dbo.app_user_profile ADD CONSTRAINT UQ_app_user_profile_email UNIQUE (email);
  END
END
GO

-- === PHONE: make NOT NULL and add unique constraint safely ===
IF EXISTS (
  SELECT phone FROM dbo.app_user_profile WHERE phone IS NOT NULL
  GROUP BY phone
  HAVING COUNT(*) > 1
)
BEGIN
  PRINT 'V4 migration: duplicate PHONE values exist - please resolve duplicates before making phone NOT NULL / UNIQUE.';
END
ELSE
BEGIN
  -- Drop any UNIQUE CONSTRAINTS referencing phone
  DECLARE @pConstraint NVARCHAR(200);
  DECLARE pconstraint_cursor CURSOR FOR
    SELECT tc.name
    FROM sys.key_constraints tc
    JOIN sys.index_columns ic ON tc.parent_object_id = ic.object_id
    JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
    WHERE tc.parent_object_id = OBJECT_ID('dbo.app_user_profile')
      AND c.name = 'phone'
      AND tc.[type] = 'UQ';

  OPEN pconstraint_cursor;
  FETCH NEXT FROM pconstraint_cursor INTO @pConstraint;
  WHILE @@FETCH_STATUS = 0
  BEGIN
    EXEC('ALTER TABLE dbo.app_user_profile DROP CONSTRAINT [' + @pConstraint + ']');
    FETCH NEXT FROM pconstraint_cursor INTO @pConstraint;
  END
  CLOSE pconstraint_cursor;
  DEALLOCATE pconstraint_cursor;

  -- Drop any unique indexes that include phone
  DECLARE @pIxName NVARCHAR(200);
  DECLARE pix_cursor CURSOR FOR
    SELECT DISTINCT i.name
    FROM sys.indexes i
    JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
    JOIN sys.columns col ON ic.object_id = col.object_id AND ic.column_id = col.column_id
    WHERE i.object_id = OBJECT_ID('dbo.app_user_profile') AND col.name = 'phone' AND i.is_unique = 1;

  OPEN pix_cursor;
  FETCH NEXT FROM pix_cursor INTO @pIxName;
  WHILE @@FETCH_STATUS = 0
  BEGIN
    DECLARE @pDropSql NVARCHAR(400) = 'DROP INDEX [' + @pIxName + '] ON dbo.app_user_profile';
    EXEC(@pDropSql);
    FETCH NEXT FROM pix_cursor INTO @pIxName;
  END
  CLOSE pix_cursor;
  DEALLOCATE pix_cursor;

  -- Now alter the column to NOT NULL
  ALTER TABLE dbo.app_user_profile ALTER COLUMN phone VARCHAR(50) NOT NULL;

  -- Recreate named unique constraint if not exists
  IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE name = N'UQ_app_user_profile_phone' AND type = 'UQ')
  BEGIN
    ALTER TABLE dbo.app_user_profile ADD CONSTRAINT UQ_app_user_profile_phone UNIQUE (phone);
  END
END
GO
