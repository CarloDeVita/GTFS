CREATE OR REPLACE FUNCTION CreatePassagesTable(vehicle_type TEXT)
RETURNS TEXT AS $$
DECLARE
    query TEXT := 
    'CREATE TABLE IF NOT EXISTS %s_PASSAGES (
     SEGMENT INTEGER NOT NULL,
     VEHICLE TEXT,
     PASSAGE_TIME TIMESTAMP WITHOUT TIME ZONE,
     CONSTRAINT pass_seg_fk FOREIGN KEY (SEGMENT) REFERENCES SEGMENTS(id))';

    index_query TEXT := 'CREATE INDEX IF NOT EXISTS %s_names_index ON %1$s_PASSAGES(vehicle)';
BEGIN
    EXECUTE format(query,vehicle_type);
    EXECUTE format(index_query,vehicle_type);
    RETURN 'OK';
EXCEPTION WHEN OTHERS THEN
    RETURN SQLERRM;
END; $$ LANGUAGE plpgsql

