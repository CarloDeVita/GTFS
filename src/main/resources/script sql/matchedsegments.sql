CREATE OR REPLACE FUNCTION CreateMatchedSegmentsTable() RETURNS VOID AS $$
BEGIN
    CREATE TABLE IF NOT EXISTS MatchedSegments
    (
        id SERIAL PRIMARY KEY,
        segment INTEGER,
        shape_id TEXT,
        sequencenumber INTEGER,

        CONSTRAINT matched_seg_fk FOREIGN KEY(segment) REFERENCES segments(id)
    );
END;
$$ LANGUAGE plpgsql;