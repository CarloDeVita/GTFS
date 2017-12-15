CREATE OR REPLACE FUNCTION CreateSegmentsTable() RETURNS VOID AS $$
BEGIN
	CREATE TABLE IF NOT EXISTS SEGMENTS
    (
        id SERIAL,
        segment geometry PRIMARY KEY,
        name text,
        highway text,
        meters float,
        oneway text
    );
    
    CREATE INDEX IF NOT EXISTS segments_index ON segments USING GIST(segment); 
END;
$$ LANGUAGE plpgsql;