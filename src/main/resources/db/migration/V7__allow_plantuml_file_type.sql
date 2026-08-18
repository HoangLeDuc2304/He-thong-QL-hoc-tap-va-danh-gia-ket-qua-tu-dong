-- Allow PLANTUML as a valid file_type for uml_submissions
ALTER TABLE uml_submissions
DROP CONSTRAINT IF EXISTS chk_file_type;

ALTER TABLE uml_submissions
ADD CONSTRAINT chk_file_type CHECK (file_type IN ('IMAGE', 'PDF', 'PLANTUML'));
