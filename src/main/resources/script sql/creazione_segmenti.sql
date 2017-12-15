CREATE OR REPLACE FUNCTION CreateSegments() RETURNS void AS $$
DECLARE
	ways_cursor CURSOR FOR SELECT * FROM planet_osm_line 
    						WHERE highway NOT IN ('path', 'footway', 'pedestrian', 'bridleway', 'steps');
	crosses_cursor CURSOR FOR SELECT N.coord FROM planet_osm_nodes N
    							WHERE (SELECT COUNT(*) FROM planet_osm_line L
                                       	WHERE ST_Intersects(L.way, N.coord) OR 
                                        ST_Equals(ST_StartPoint(L.way),N.coord) OR ST_Equals(ST_EndPoint(L.way), N.coord))>2
    nodes_cursor REFCURSOR;
    counter INTEGER;
    nodes geometry; 
BEGIN
    CreateSegmentsTable();
    
    FOR crosses IN (SELECT )
    
	FOR seg IN ways_cursor LOOP
    	counter := 0; 
    	SELECT coord  
        FROM planet_osm_nodes WHERE ST_Contains(seg.way, coord)
        	
    	INSERT INTO SEGMENTS(segment, name, highway) VALUES(seg.way, seg.name, seg.highway);
    END LOOP;
END;
$$ LANGUAGE plpgsql;