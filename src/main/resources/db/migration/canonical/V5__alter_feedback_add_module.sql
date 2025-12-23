-- V5: Add module_id to user_feedback (SQL Server) and create FK to module.module_id

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

