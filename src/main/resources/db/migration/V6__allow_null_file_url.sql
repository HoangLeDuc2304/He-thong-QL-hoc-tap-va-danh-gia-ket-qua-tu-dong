-- Allow file_url to be null for PlantUML-only submissions
ALTER TABLE uml_submissions
ALTER COLUMN file_url DROP NOT NULL;
