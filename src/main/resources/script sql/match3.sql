CREATE OR REPLACE FUNCTION public.matchseg()
RETURNS void LANGUAGE 'plpgsql' AS $BODY$
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
BEGIN

    lastShape := NULL;
    last_seg := NULL;
    lastCoordinate := NULL;
    FOR sh_rec IN sh_cur LOOP
        IF(lastShape IS NULL OR lastShape<>sh_rec.shape_id) THEN -- first point of the shape
            lastShape := sh_rec.shape_id;
            shape_seq := 1;
            last_seg := NULL;
            lastCoordinate := NULL;
            lasttarget := NULL;
            currtarget := NULL;
            
            -- Save the id of the closest vertex
            SELECT CASE WHEN ST_Distance(sh_rec.coordinate,ST_StartPoint(S.segment))<ST_Distance(sh_rec.coordinate,ST_EndPoint(S.segment))
                        THEN S.source ELSE S.target END,S.id INTO currtarget, seg_id
            FROM segments S
            WHERE ST_DWithin(sh_rec.coordinate, S.segment, 0.00075)
            ORDER BY St_Distance(sh_rec.coordinate, S.segment)
            LIMIT 1;

            IF(currtarget IS NULL) THEN -- no street found within about 30 meters
                lastShape := NULL; -- next point is considered as the first point
                CONTINUE;
            END IF;
        --ELIF(lastCoordinate IS NOT NULL AND ST_DWithin(lastCoordinate, sh_rec.coordinate, 0.0001) THEN
          --  CONTINUE;
        ELSE
            /* the north-based azimuth as the angle in radians measured clockwise 
            from the vertical the last coordinate of the shape to the current*/
            SELECT degrees(st_azimuth(lastCoordinate,sh_rec.coordinate))
            INTO angle;

            --TODO explain
            SELECT S2.id, S2.source, S2.target, S2.segment INTO seg_id, currsource, currtarget, seg
            FROM (SELECT * FROM segments S
                  WHERE ST_DWithin(sh_rec.coordinate, S.segment, 0.00075)
                  ORDER BY St_Distance(sh_rec.coordinate, S.segment) ASC
                  LIMIT 5) S2
            ORDER BY CAST(((CASE WHEN S2.oneway IS NOT NULL AND S2.oneway='true' THEN MinimumAngle(angle, ST_StartPoint(S2.segment), ST_EndPoint(S2.segment))
                              ELSE LEAST(MinimumAngle(angle,ST_StartPoint(S2.segment), ST_EndPoint(S2.segment)),
                                  MinimumAngle(angle, ST_EndPoint(S2.segment), ST_StartPoint(S2.segment))) END)/10) AS INTEGER) ASC --"groupy by" ranges of inclination similarity                     
                     ,ST_Distance(lastCoordinate, ST_ClosestPoint(S2.segment, sh_rec.coordinate)) -- and get the closest one
                            /*(CASE WHEN S2.oneway IS NOT NULL AND S2.oneway='true' THEN ST_Distance(geography(lastCoordinate), geography(ST_StartPoint(S2.segment)),true)
                                      ELSE LEAST(ST_Distance(geography(lastCoordinate), geography(ST_StartPoint(S2.segment)),true),
                                         ST_Distance(geography(lastCoordinate), geography(ST_EndPoint(S2.segment)),true)
                                          ) END)*/
                      ASC
            LIMIT 1;

            -- skip points not relatable with any street
            IF(seg_id IS NULL) THEN CONTINUE; END IF;

            IF(last_seg<>seg_id) THEN
                seg := ST_MakeLine(lastCoordinate, ST_StartPoint(seg));
 
                -- use routing to find segments between the current and the last
                FOR path_edge IN (SELECT edge 
                          FROM pgr_dijkstra('SELECT id ,source,target,meters AS cost,CASE WHEN oneway=''true'' THEN -1 ELSE meters END AS reverse_cost, 
                                                    ST_X(ST_StartPoint(segment)) AS x1, ST_Y(ST_StartPoint(segment)) AS y1, ST_X(ST_EndPoint(segment)) AS x2, ST_Y(ST_EndPoint(segment)) AS y2 FROM public.segments
                                             WHERE ST_DWithin(ST_GeomFromText('''|| ST_AsText(seg) ||''',4326),segment,0.0008) AND source IS NOT NULL AND target IS NOT NULL'
                                            , lasttarget, currsource, true)
                          ORDER BY path_seq ASC)
                LOOP
                    IF(path_edge<>last_seg AND path_edge<>seg_id AND path_edge<>-1) THEN
                        INSERT INTO matchedsegments(segment, shape_id, sequencenumber)
                        VALUES (path_edge,sh_rec.shape_id,shape_seq);

                        shape_seq := shape_seq+1;
                    END IF;
                END LOOP;
            END IF;
          
            INSERT INTO matchedsegments(segment, shape_id, sequencenumber)
            VALUES (seg_id,sh_rec.shape_id,shape_seq);

            shape_seq := shape_seq+1;
        END IF;

        lastCoordinate:=sh_rec.coordinate;
        lasttarget:=currtarget;
        last_seg := seg_id;

    END LOOP;
END; $BODY$;

