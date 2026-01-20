-- convert_datetime_to_offset.sql
-- Safe, idempotent conversion of DATETIME2 -> DATETIMEOFFSET(6) for production
-- WARNING: BACKUP your database before running this script.
-- This script treats existing datetime2 values as UTC. If your stored values are in a different timezone,
-- replace 'UTC' with the appropriate Windows timezone name (for example 'India Standard Time').

SET XACT_ABORT ON;

BEGIN TRY
  BEGIN TRANSACTION;

  ------------------------------------------------------------
  -- user_feedback.created_at (datetime2 -> datetimeoffset(6))
  ------------------------------------------------------------
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS c
    WHERE c.TABLE_SCHEMA='dbo' AND c.TABLE_NAME='user_feedback' AND c.COLUMN_NAME='created_at'
      AND LOWER(c.DATA_TYPE) = 'datetime2'
  )
  BEGIN
    PRINT 'Converting dbo.user_feedback.created_at from datetime2 to datetimeoffset(6)';
    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE Name = N'created_at_tmp' AND Object_ID = Object_ID(N'dbo.user_feedback'))
    BEGIN
      ALTER TABLE dbo.user_feedback ADD created_at_tmp DATETIMEOFFSET(6);
      EXEC('UPDATE dbo.user_feedback SET created_at_tmp = created_at AT TIME ZONE ''UTC''');
      ALTER TABLE dbo.user_feedback DROP COLUMN created_at;
      EXEC('EXEC sp_rename ''dbo.user_feedback.created_at_tmp'', ''created_at'', ''COLUMN''');
    END
  END

  ------------------------------------------------------------
  -- user_quiz_result.taken_at (datetime2 -> datetimeoffset(6))
  ------------------------------------------------------------
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS c
    WHERE c.TABLE_SCHEMA='dbo' AND c.TABLE_NAME='user_quiz_result' AND c.COLUMN_NAME='taken_at'
      AND LOWER(c.DATA_TYPE) = 'datetime2'
  )
  BEGIN
    PRINT 'Converting dbo.user_quiz_result.taken_at from datetime2 to datetimeoffset(6)';
    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE Name = N'taken_at_tmp' AND Object_ID = Object_ID(N'dbo.user_quiz_result'))
    BEGIN
      ALTER TABLE dbo.user_quiz_result ADD taken_at_tmp DATETIMEOFFSET(6);
      EXEC('UPDATE dbo.user_quiz_result SET taken_at_tmp = taken_at AT TIME ZONE ''UTC''');
      ALTER TABLE dbo.user_quiz_result DROP COLUMN taken_at;
      EXEC('EXEC sp_rename ''dbo.user_quiz_result.taken_at_tmp'', ''taken_at'', ''COLUMN''');
    END
  END

  ------------------------------------------------------------
  -- Normalize precision if existing columns are datetimeoffset but not (6)
  ------------------------------------------------------------
  -- user_feedback.created_at precision normalization
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS c
    WHERE c.TABLE_SCHEMA='dbo' AND c.TABLE_NAME='user_feedback' AND c.COLUMN_NAME='created_at'
      AND LOWER(c.DATA_TYPE) = 'datetimeoffset' AND (c.DATETIME_PRECISION IS NULL OR c.DATETIME_PRECISION <> 6)
  )
  BEGIN
    PRINT 'Normalizing dbo.user_feedback.created_at to DATETIMEOFFSET(6) precision';
    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE Name = N'created_at_tmp' AND Object_ID = Object_ID(N'dbo.user_feedback'))
    BEGIN
      ALTER TABLE dbo.user_feedback ADD created_at_tmp DATETIMEOFFSET(6);
      EXEC('UPDATE dbo.user_feedback SET created_at_tmp = CAST(created_at AS DATETIMEOFFSET(6))');
      ALTER TABLE dbo.user_feedback DROP COLUMN created_at;
      EXEC('EXEC sp_rename ''dbo.user_feedback.created_at_tmp'', ''created_at'', ''COLUMN''');
    END
  END

  -- user_quiz_result.taken_at precision normalization
  IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS c
    WHERE c.TABLE_SCHEMA='dbo' AND c.TABLE_NAME='user_quiz_result' AND c.COLUMN_NAME='taken_at'
      AND LOWER(c.DATA_TYPE) = 'datetimeoffset' AND (c.DATETIME_PRECISION IS NULL OR c.DATETIME_PRECISION <> 6)
  )
  BEGIN
    PRINT 'Normalizing dbo.user_quiz_result.taken_at to DATETIMEOFFSET(6) precision';
    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE Name = N'taken_at_tmp' AND Object_ID = Object_ID(N'dbo.user_quiz_result'))
    BEGIN
      ALTER TABLE dbo.user_quiz_result ADD taken_at_tmp DATETIMEOFFSET(6);
      EXEC('UPDATE dbo.user_quiz_result SET taken_at_tmp = CAST(taken_at AS DATETIMEOFFSET(6))');
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

PRINT 'Conversion script finished.';

