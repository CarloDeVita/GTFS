DROP FUNCTION IF EXISTS match_gtfs_shapes();
CREATE OR REPLACE FUNCTION public.match_gtfs_shapes()
RETURNS TEXT LANGUAGE 'plpgsql' AS $BODY$
DECLARE
    sh_cur CURSOR FOR SELECT shape_id, coordinate FROM gtfs.shape_points ORDER BY shape_id, sequencenumber ASC;
    seg_id INTEGER;
    last_seg INTEGER;
    lastCoordinate geometry;
    lastShape TEXT;
    angle float;
    shape_seq INTEGER;
    path_edge INTEGER;
    lasttarget INTEGER;
    currsource INTEGER;
    currtarget INTEGER;
    seg geometry;

    tolerance double precision;
BEGIN
    --TRUNCATE TABLE matchedsegments CASCADE;
    
    create temporary table shape_segments(
        s_id INTEGER
    ) ON COMMIT DROP;

    tolerance = 0.00015;

    lastShape := NULL;
    last_seg := NULL;
    lastCoordinate := NULL;

    FOR sh_rec IN sh_cur LOOP
        IF(lastShape IS NULL OR lastShape<>sh_rec.shape_id) THEN -- first point of the shape
            TRUNCATE shape_segments;
            lastShape := sh_rec.shape_id;
            shape_seq := 1;
            seg_id := NULL;
            last_seg := NULL;
            lastCoordinate := NULL;
            lasttarget := NULL;
            currtarget := NULL;
            
            -- Save the id of the closest vertex
            SELECT CASE WHEN ST_Distance(sh_rec.coordinate,ST_StartPoint(S.segment))<ST_Distance(sh_rec.coordinate,ST_EndPoint(S.segment))
                        THEN S.source ELSE S.target END,S.id INTO currtarget, seg_id
            FROM segments S
            WHERE ST_DWithin(sh_rec.coordinate, S.segment, tolerance)
            ORDER BY St_Distance(sh_rec.coordinate, S.segment) ASC
            LIMIT 1;

            IF(currtarget IS NULL) THEN -- no street found within about 30 meters
                lastShape := NULL; -- next point is considered as the first point
                CONTINUE;
            END IF;
        ELSIF(lastCoordinate IS NOT NULL AND ST_DWithin(lastCoordinate, sh_rec.coordinate, tolerance/4)) THEN
            --lastCoordinate:=sh_rec.coordinate;
            CONTINUE;
        ELSE
            /* the north-based azimuth as the angle in radians measured clockwise 
            from the vertical the last coordinate of the shape to the current*/
            angle := degrees(st_azimuth(lastCoordinate,sh_rec.coordinate));

            --//TODO explain
            SELECT S2.id, S2.source, S2.target, S2.segment 
            INTO seg_id, currsource, currtarget, seg
            FROM (SELECT S.id, S.source, S.target, S.segment , 
                        floor(MinimumAngle(angle, S.segment, lastCoordinate, sh_rec.coordinate, ((S.oneway IS NOT NULL AND S.oneway='true') OR (S.highway is not null AND S.highway='motorway')))/20.) delta_angle
                  FROM segments S
                  WHERE ST_DWithin(sh_rec.coordinate, S.segment, tolerance)
                  ORDER BY St_Distance(sh_rec.coordinate, S.segment) ASC /*LIMIT 5*/) S2
            WHERE S2.delta_angle<40
            ORDER BY  S2.delta_angle ASC --"groupy by" ranges of inclination similarity                     
                     --,ST_Distance(lastCoordinate, ST_ClosestPoint(S2.segment, sh_rec.coordinate)) ASC -- and get the closest one
                     --, (ST_Distance(lastCoordinate, S2.segment)/10.+ST_Distance(sh_rec.coordinate, S2.segment)) ASC
                     , ST_Distance(ST_LineInterpolatePoint(ST_MakeLine(lastCoordinate,sh_rec.coordinate),0.85),S2.segment) ASC
            LIMIT 1;

            -- skip points not relatable with any street
            IF(seg_id IS NULL) THEN CONTINUE; END IF;

            IF(seg_id IS NOT NULL AND seg_id NOT IN (select s_id FROM shape_segments)) THEN
                seg := ST_MakeLine(lastCoordinate, sh_rec.coordinate/*ST_StartPoint(seg)*/); --segment that define routing window
                -- use routing to find segments between the current and the last
                FOR path_edge IN (SELECT edge 
                          FROM pgr_dijkstra('SELECT id ,source,target,meters AS cost,CASE WHEN (oneway is not null and oneway=''true'') THEN -1 ELSE meters END AS reverse_cost, 
                                                    ST_X(ST_StartPoint(segment)) AS x1, ST_Y(ST_StartPoint(segment)) AS y1, ST_X(ST_EndPoint(segment)) AS x2, ST_Y(ST_EndPoint(segment)) AS y2 FROM public.segments
                                             WHERE ST_DWithin(ST_GeomFromText('''|| ST_AsText(seg) ||''',4326),segment,'||tolerance||') AND source IS NOT NULL AND target IS NOT NULL'
                                            , lasttarget, currsource, true)
                          WHERE path_seq<>-1
                          ORDER BY path_seq ASC)
                LOOP
                    IF(path_edge<>seg_id AND path_edge NOT IN (SELECT s_id FROM shape_segments )) THEN
                        INSERT INTO matchedsegments(segment, shape_id, sequencenumber, type)
                        VALUES (path_edge,sh_rec.shape_id,shape_seq,2);
                        
                        INSERT INTO shape_segments values (path_edge);

                        shape_seq := shape_seq+1;
                    END IF;
                END LOOP;
            END IF;
          
            IF(seg_id IS NOT NULL AND seg_id NOT IN (Select s_id from shape_segments)) THEN
                INSERT INTO matchedsegments(segment, shape_id, sequencenumber, type)
                VALUES (seg_id,sh_rec.shape_id,shape_seq, 1);

                INSERT INTO shape_segments values(seg_id);
                shape_seq := shape_seq+1;
            END IF;
        END IF;

        lastCoordinate:=sh_rec.coordinate;
        lasttarget:=currtarget;
        last_seg := seg_id;
        
    END LOOP;
    RETURN 'OK';
END; $BODY$;

