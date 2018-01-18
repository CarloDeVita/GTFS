CREATE OR REPLACE FUNCTION public.populatesegments()
RETURNS void LANGUAGE 'plpgsql' AS $BODY$
DECLARE
    line_cursor CURSOR FOR SELECT osm_id, way, junction, oneway, name, highway
                           FROM planet_osm_line 
                           WHERE highway NOT IN ('path', 'footway', 'pedestrian', 'bridleway', 'steps','raceway', 'cycleway','proposed','construction', 'corridor');
    n INTEGER;
    p geometry;
    way geometry;
    counter INTEGER;
    lastfraction float;
    fraction float;
    segment geometry;
    meters float;
    oneway boolean;
BEGIN
	--SELECT CreateSegmentsTable();
	
    FOR line_record IN line_cursor LOOP
    	way := line_record.way;

        oneway := (line_record.junction IS NOT NULL AND line_record.junction='roundabout') 
                   OR (line_record.oneway IS NOT NULL AND line_record.oneway='yes');
        
    	SELECT ST_NumPoints(way)-1 INTO n;
        lastfraction := 0;
    	FOR i IN 2..n LOOP
            SELECT ST_PointN(way, i) INTO p;
            
            SELECT COUNT(*)+1 INTO counter
            FROM planet_osm_line L
            WHERE ST_Intersects(L.way, p) AND
            	 L.highway NOT IN ('path', 'footway', 'pedestrian', 'bridleway', 'steps','raceway', 'cycleway','proposed','construction', 'corridor');
            
            IF(counter>=3) THEN
            	SELECT ST_LineLocatePoint(way,p) INTO fraction;
                SELECT ST_LineSubstring(way,lastfraction,fraction) INTO segment;

                SELECT ST_Length(geography(segment), true) INTO meters;
                --aggiungi segmento
                INSERT INTO SEGMENTS(segment,name,oneway,highway, meters)
                VALUES(segment, line_record.name, oneway, line_record.highway, meters);
            	lastfraction := fraction;
            END IF;
            
        END LOOP;
        
        SELECT ST_LineSubstring(way, lastfraction,1) INTO segment;
        
        SELECT ST_Length(geography(segment), true) INTO meters;
        
        INSERT INTO SEGMENTS(segment,name,oneway,highway, meters)
        VALUES(segment, line_record.name, oneway, line_record.highway, meters);
    END LOOP;
    
    SELECT pgr_createTopology('segments',0,'segment','id','source','target');
END; $BODY$;
