CREATE OR REPLACE FUNCTION CreateSegmentsTable() RETURNS VOID AS $$
BEGIN
    CREATE TABLE IF NOT EXISTS SEGMENTS
    (
        id SERIAL PRIMARY KEY,
        osm_id INTEGER NOT NULL,
        segment geometry NOT NULL,
        source INTEGER,
        target INTEGER,
        name text,
        highway text,
        meters float,
        oneway boolean
    );
    
    CREATE INDEX IF NOT EXISTS segments_index ON segments USING GIST(segment); 

    CREATE TABLE IF NOT EXISTS MATCHEDSEGMENTS
    (
        id SERIAL PRIMARY KEY,
        segment INTEGER,
        shape_id TEXT,
        sequencenumber INTEGER,

        CONSTRAINT matched_segment_fk FOREIGN KEY (segment) REFERENCES segments(id)
    );

END;
$$ LANGUAGE plpgsql;