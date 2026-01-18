-- Combined canonical migrations for Production (SQL Server)
-- Run this script against the production Azure SQL database.
-- It is idempotent and guarded: safe to run multiple times.

-- ========== V1: create exhibition table ===========
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = N'exhibition' AND schema_name(schema_id) = N'dbo')
BEGIN
  CREATE TABLE dbo.exhibition (
      exhibition_id VARCHAR(255) PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      description VARCHAR(MAX),
      start_date DATE,
      end_date DATE,
      location VARCHAR(255),
      status VARCHAR(100)
  );
END
GO

-- ========== V2: create module, stall, poster_content tables ===========
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = N'module' AND schema_name(schema_id) = N'dbo')
BEGIN
  CREATE TABLE dbo.[module] (
      module_id VARCHAR(255) PRIMARY KEY,
      name VARCHAR(255),
      description VARCHAR(MAX),
      assigned_team_id VARCHAR(255),
      exhibition_id VARCHAR(255)
  );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = N'stall' AND schema_name(schema_id) = N'dbo')
BEGIN
  CREATE TABLE dbo.stall (
      stall_id VARCHAR(255) PRIMARY KEY,
      name VARCHAR(255),
      description VARCHAR(MAX),
      stall_number VARCHAR(255),
      layout VARCHAR(MAX),
      module_id VARCHAR(255)
  );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = N'poster_content' AND schema_name(schema_id) = N'dbo')
BEGIN
  CREATE TABLE dbo.poster_content (
      content_id VARCHAR(255) PRIMARY KEY,
      language_code VARCHAR(10),
      poster_media_url VARCHAR(1024),
      content_text VARCHAR(MAX),
      stall_id VARCHAR(255)
  );
END
GO

-- FK: module.exhibition -> exhibition.exhibition_id
IF NOT EXISTS (
  SELECT 1 FROM sys.foreign_keys fk
  WHERE fk.parent_object_id = OBJECT_ID('dbo.[module]')
    AND fk.referenced_object_id = OBJECT_ID('dbo.exhibition')
)
BEGIN
  ALTER TABLE dbo.[module] ADD CONSTRAINT FK_module_exhibition FOREIGN KEY (exhibition_id) REFERENCES dbo.exhibition (exhibition_id);
END
GO

-- FK: stall.module -> module.module_id
IF NOT EXISTS (
  SELECT 1 FROM sys.foreign_keys fk
  WHERE fk.parent_object_id = OBJECT_ID('dbo.stall')
    AND fk.referenced_object_id = OBJECT_ID('dbo.[module]')
)
BEGIN
  ALTER TABLE dbo.stall ADD CONSTRAINT FK_stall_module FOREIGN KEY (module_id) REFERENCES dbo.[module] (module_id);
END
GO

-- FK: poster_content.stall -> stall.stall_id
IF NOT EXISTS (
  SELECT 1 FROM sys.foreign_keys fk
  WHERE fk.parent_object_id = OBJECT_ID('dbo.poster_content')
    AND fk.referenced_object_id = OBJECT_ID('dbo.stall')
)
BEGIN
  ALTER TABLE dbo.poster_content ADD CONSTRAINT FK_postercontent_stall FOREIGN KEY (stall_id) REFERENCES dbo.stall (stall_id);
END
GO

-- ========== V3: create user profile, feedback, quiz_result tables ===========
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = N'app_user_profile' AND schema_name(schema_id) = N'dbo')
BEGIN
  CREATE TABLE dbo.app_user_profile (
      user_id VARCHAR(36) PRIMARY KEY,
      full_name VARCHAR(255),
      email VARCHAR(255),
      phone VARCHAR(50),
      preferred_language VARCHAR(50),
      country VARCHAR(100),
      address VARCHAR(MAX)
  );
  -- create unique index on email if required (will be enforced later by V4 safely)
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = N'user_feedback' AND schema_name(schema_id) = N'dbo')
BEGIN
  CREATE TABLE dbo.user_feedback (
      feedback_id VARCHAR(36) PRIMARY KEY,
      user_id VARCHAR(36),
      exhibition_id VARCHAR(255),
      comments VARCHAR(MAX),
      rating INT,
      created_at DATETIMEOFFSET(6)
  );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = N'user_quiz_result' AND schema_name(schema_id) = N'dbo')
BEGIN
  CREATE TABLE dbo.user_quiz_result (
      result_id VARCHAR(36) PRIMARY KEY,
      user_id VARCHAR(36),
      module_id VARCHAR(255),
      result INT,
      total_marks INT,
      percentage FLOAT,
      points INT,
      taken_at DATETIMEOFFSET(6)
  );
END
GO

-- FK: user_feedback.user_id -> app_user_profile.user_id
IF NOT EXISTS (
  SELECT 1 FROM sys.foreign_keys fk
  WHERE fk.parent_object_id = OBJECT_ID('dbo.user_feedback')
    AND fk.referenced_object_id = OBJECT_ID('dbo.app_user_profile')
)
BEGIN
  ALTER TABLE dbo.user_feedback ADD CONSTRAINT FK_feedback_user FOREIGN KEY (user_id) REFERENCES dbo.app_user_profile (user_id);
END
GO

-- FK: user_quiz_result.user_id -> app_user_profile.user_id
IF NOT EXISTS (
  SELECT 1 FROM sys.foreign_keys fk
  WHERE fk.parent_object_id = OBJECT_ID('dbo.user_quiz_result')
    AND fk.referenced_object_id = OBJECT_ID('dbo.app_user_profile')
)
BEGIN
  ALTER TABLE dbo.user_quiz_result ADD CONSTRAINT FK_quiz_user FOREIGN KEY (user_id) REFERENCES dbo.app_user_profile (user_id);
END
GO

-- ========== V4: alter user_profile add country, address, gender, password_hash; make email/phone NOT NULL and UNIQUE safely ===========
-- Add columns if missing
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

-- Safely make email NOT NULL and UNIQUE if data is clean
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
  -- Drop existing unique constraints or indexes on email
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

  -- Drop unique indexes that include email
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

  -- Alter column to NOT NULL
  EXEC('ALTER TABLE dbo.app_user_profile ALTER COLUMN email VARCHAR(255) NOT NULL');

  -- Recreate named unique constraint if not exists
  IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE name = N'UQ_app_user_profile_email' AND type = 'UQ')
  BEGIN
    ALTER TABLE dbo.app_user_profile ADD CONSTRAINT UQ_app_user_profile_email UNIQUE (email);
  END
END
GO

-- Safely make phone NOT NULL and UNIQUE
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
  -- Drop unique constraints referencing phone
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

  -- Drop unique indexes including phone
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

  -- Alter phone column to NOT NULL
  EXEC('ALTER TABLE dbo.app_user_profile ALTER COLUMN phone VARCHAR(50) NOT NULL');

  -- Recreate named unique constraint if not exists
  IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE name = N'UQ_app_user_profile_phone' AND type = 'UQ')
  BEGIN
    ALTER TABLE dbo.app_user_profile ADD CONSTRAINT UQ_app_user_profile_phone UNIQUE (phone);
  END
END
GO

-- ========== V5: add module_id to user_feedback and FK ===========
IF NOT EXISTS (
  SELECT 1 FROM sys.columns
  WHERE Name = N'module_id' AND Object_ID = Object_ID(N'dbo.user_feedback')
)
BEGIN
  ALTER TABLE dbo.user_feedback ADD module_id VARCHAR(255);
END
GO

IF NOT EXISTS (
  SELECT 1 FROM sys.foreign_keys fk
  JOIN sys.tables t ON fk.parent_object_id = t.object_id
  WHERE fk.name = N'FK_feedback_module' AND t.name = N'user_feedback'
)
BEGIN
  ALTER TABLE dbo.user_feedback
    ADD CONSTRAINT FK_feedback_module FOREIGN KEY (module_id) REFERENCES dbo.[module] (module_id);
END
GO

-- ========== V6: add questions column to user_feedback ===========
IF NOT EXISTS (
  SELECT 1 FROM sys.columns
  WHERE Name = N'questions' AND Object_ID = Object_ID(N'dbo.user_feedback')
)
BEGIN
  ALTER TABLE dbo.user_feedback ADD questions VARCHAR(MAX);
END
GO

-- ========== V7: add role column to app_user_profile, default 'normal', and check constraint ===========
IF NOT EXISTS (
  SELECT 1 FROM sys.columns
  WHERE Name = N'role' AND Object_ID = Object_ID(N'dbo.app_user_profile')
)
BEGIN
  EXEC('ALTER TABLE dbo.app_user_profile ADD role VARCHAR(20) CONSTRAINT DF_app_user_profile_role DEFAULT (''normal'')');
END
GO

IF EXISTS (
  SELECT 1 FROM sys.columns
  WHERE Name = N'role' AND Object_ID = Object_ID(N'dbo.app_user_profile')
)
BEGIN
  UPDATE dbo.app_user_profile SET role = 'normal' WHERE role IS NULL;
END
GO

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

-- ========== Safe conversion: Convert existing DATETIME2 columns to DATETIMEOFFSET(6) if needed ==========
-- This block is idempotent and will only run when the current column type is datetime2.
-- It treats existing datetime2 values as UTC when assigning an offset. If your timestamps
-- are in a different timezone, replace 'UTC' with the correct Windows timezone name.
BEGIN TRY
  BEGIN TRANSACTION;

  -- Convert user_feedback.created_at if it's datetime2
  IF EXISTS (
    SELECT 1
    FROM INFORMATION_SCHEMA.COLUMNS c
    WHERE c.TABLE_SCHEMA = 'dbo' AND c.TABLE_NAME = 'user_feedback' AND c.COLUMN_NAME = 'created_at'
      AND LOWER(c.DATA_TYPE) = 'datetime2'
  )
  BEGIN
    PRINT 'Converting dbo.user_feedback.created_at from datetime2 to datetimeoffset(6)';
    IF NOT EXISTS (
      SELECT 1 FROM sys.columns WHERE Name = N'created_at_tmp' AND Object_ID = Object_ID(N'dbo.user_feedback')
    )
    BEGIN
      ALTER TABLE dbo.user_feedback ADD created_at_tmp DATETIMEOFFSET(6);
      -- Use dynamic SQL to avoid parser complaining about the newly added column at parse-time
      EXEC('UPDATE dbo.user_feedback SET created_at_tmp = created_at AT TIME ZONE ''UTC''');
      ALTER TABLE dbo.user_feedback DROP COLUMN created_at;
      EXEC('EXEC sp_rename ''dbo.user_feedback.created_at_tmp'', ''created_at'', ''COLUMN''');
    END
  END

  -- Convert user_quiz_result.taken_at if it's datetime2
  IF EXISTS (
    SELECT 1
    FROM INFORMATION_SCHEMA.COLUMNS c
    WHERE c.TABLE_SCHEMA = 'dbo' AND c.TABLE_NAME = 'user_quiz_result' AND c.COLUMN_NAME = 'taken_at'
      AND LOWER(c.DATA_TYPE) = 'datetime2'
  )
  BEGIN
    PRINT 'Converting dbo.user_quiz_result.taken_at from datetime2 to datetimeoffset(6)';
    IF NOT EXISTS (
      SELECT 1 FROM sys.columns WHERE Name = N'taken_at_tmp' AND Object_ID = Object_ID(N'dbo.user_quiz_result')
    )
    BEGIN
      ALTER TABLE dbo.user_quiz_result ADD taken_at_tmp DATETIMEOFFSET(6);
      EXEC('UPDATE dbo.user_quiz_result SET taken_at_tmp = taken_at AT TIME ZONE ''UTC''');
      ALTER TABLE dbo.user_quiz_result DROP COLUMN taken_at;
      EXEC('EXEC sp_rename ''dbo.user_quiz_result.taken_at_tmp'', ''taken_at'', ''COLUMN''');
    END
  END

  COMMIT TRANSACTION;
END TRY
BEGIN CATCH
  ROLLBACK TRANSACTION;
  DECLARE @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE();
  PRINT 'Error during datetime2 -> datetimeoffset conversion: ' + @ErrMsg;
  THROW;
END CATCH;
GO

-- End of combined migrations
