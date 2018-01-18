-- FUNCTION: public.matchseg()

-- DROP FUNCTION public.matchseg();

CREATE OR REPLACE FUNCTION public.matchseg()
RETURNS void LANGUAGE 'plpgsql' AS $BODY$
DECLARE
    sh_cur cursor for select* from gtfs.shape_points ORDER BY shape_id, sequencenumber;
    seg_id INTEGER;
    lastCoordinate geometry;
    lastShape TEXT;
    last_seg INTEGER;
    i INTEGER;
    angle float;
DECLARE
    lastCoordinate=null;
    lastShape=null;
    for sh_rec in sh_cur loop
        i := 0;
        IF(lastShape IS NULL OR lastShape<>sh_rec.shape_id) THEN 
            lastShape:= sh_rec.shape_id;

            select s.id INTO seg_id
            FROM segments S
            WHERE ST_DWithin(sh_rec.coordinate, S.segment, 0.0001)
            ORDER BY St_Distance(sh_rec.coordinate, S.segment)
            LIMIT 1;    
        ELSE
            select degrees(st_azimuth(lastCoordinate,sh_rec.coordinate))
            into angle;

            select S2.id INTO seg_id
            FROM (Select * FROM segments S
                       where ST_DWithin(sh_rec.coordinate, S.segment, 0.0001)
                    /*WHERE ST_DWithin(sh_rec.coordinate, S.segment, st_distance(sh_rec.coordinate,lastCoordinate)) 
                     AND	ST_DWithin(lastCoordinate, S.segment, st_distance(sh_rec.coordinate,lastCoordinate))
                   */ORDER BY St_Distance(sh_rec.coordinate, S.segment) ASC
                   LIMIT 4

                  ) S2
            ORDER BY (CASE WHEN S2.oneway IS NOT NULL AND S2.oneway='true' THEN MinimumAngle(angle, ST_StartPoint(S2.segment), ST_EndPoint(S2.segment))
                              ELSE LEAST(MinimumAngle(angle,ST_StartPoint(S2.segment), ST_EndPoint(S2.segment)),
                                  MinimumAngle(angle, ST_EndPoint(S2.segment), ST_StartPoint(S2.segment))) END) ASC
                        /*(CASE WHEN S2.oneway IS NOT NULL AND S2.oneway='true' THEN MinimumAngle(angle, ST_StartPoint(S2.segment), ST_PointN(S2.segment,2))
                              ELSE (CASE WHEN ST_DISTANCE(sh_rec.coordinate,ST_StartPoint(S2.segment)) < ST_DISTANCE(sh_rec.coordinate,ST_EndPoint(S2.segment))
                                      THEN  
                                            (MinimumAngle(angle,ST_StartPoint(S2.segment), ST_PointN(S2.segment,2))) 
                                  ELSE
                                            (MinimumAngle(angle, ST_EndPoint(S2.segment), ST_PointN(S2.segment,-2)))END) END)*/
                                --  *0.95 +
                  -- ST_Distance(lastCoordinate, S2.segment)
                            /*(CASE WHEN S2.oneway IS NOT NULL AND S2.oneway='true' THEN ST_Distance(geography(lastCoordinate), geography(ST_StartPoint(S2.segment)),true)
                                      ELSE LEAST(ST_Distance(geography(lastCoordinate), geography(ST_StartPoint(S2.segment)),true),
                                         ST_Distance(geography(lastCoordinate), geography(ST_EndPoint(S2.segment)),true)
                                          ) END)*/
                       --*0.05 ASC
            LIMIT 1;
        END IF;

        IF(last_seg IS NOT NULL AND last_seg<>seg_id) THEN
            INSERT into matchedsegments(segment, shape_id, sequencenumber) values(seg_id,sh_rec.shape_id,i);
        END IF;
        i := i+1;            
        lastCoordinate := sh_rec.coordinate;
        last_seg := seg_id;
    end loop;
end; $BODY$;