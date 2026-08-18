-- Flyway migration V5: ensure plantuml_source exists (safe)
ALTER TABLE uml_submissions
ADD COLUMN IF NOT EXISTS plantuml_source TEXT;
