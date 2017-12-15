-- FUNCTION: public.populatesegments()

-- DROP FUNCTION public.populatesegments();

CREATE OR REPLACE FUNCTION public.populatesegments(
	)
    RETURNS void
    LANGUAGE 'plpgsql'
AS $BODY$

DECLARE
	line_cursor CURSOR FOR SELECT * FROM planet_osm_line 
    			where highway NOT IN ('path', 'footway', 'pedestrian', 'bridleway', 'steps','raceway', 'cycleway','proposed','construction');
    n INTEGER;
    p geometry;
    way geometry;
    counter INTEGER;
    lastfraction float;
    fraction float;
    segment geometry;
    utm_srid INTEGER;
    meters float;
    oneway text;
BEGIN
	--SELECT CreateSegmentsTable();
	
    FOR line_record IN line_cursor LOOP
    	way := line_record.way;
        IF((line_record.junction IS NOT NULL AND line_record.junction='roundabout') 
            OR (line_record.oneway IS NOT NULL AND line_record.oneway='yes')) THEN
        	oneway := true;
        ELSE 
        	oneway:= false;
        END IF;
        
    	SELECT ST_NumPoints(way)-1 INTO n;
        lastfraction := 0;
    	FOR i IN 2..n LOOP
        	SELECT ST_PointN(way, i) INTO p;
            
            SELECT COUNT(*)+1 INTO counter
            FROM planet_osm_line L
            WHERE ST_Intersects(L.way, p) AND
            	 L.highway NOT IN ('path', 'footway', 'pedestrian', 'bridleway', 'steps','raceway', 'cycleway','proposed','construction');
            
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
    
END;

$BODY$;

ALTER FUNCTION public.populatesegments()
    OWNER TO postgres;
