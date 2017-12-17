CREATE OR REPLACE FUNCTION CreateMatchedSegmentsTable() RETURNS VOID AS $$
BEGIN
    CREATE TABLE IF NOT EXISTS MatchedSegments
    (
        id SERIAL PRIMARY KEY,
        segment INTEGER,
        shape_id TEXT,
        sequencenumber INTEGER
    );
END;
$$ LANGUAGE plpgsql;