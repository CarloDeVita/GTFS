create or replace function matchSeg () returns void as 
$$
    declare
    	sh_cur cursor for select* from gtfs.shape_points ORDER BY shape_id, sequencenumber;
        seg_id INTEGER;
        last_seg INTEGER;
        lastCoordinate geometry;
        lastShape TEXT;
        angle float;
    begin
    	lastCoordinate=null;
        lastShape=null;
        last_seg=null;
    	for sh_rec in sh_cur loop
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
                      WHERE ST_DWithin(sh_rec.coordinate, S.segment, 0.0001)
                      ORDER BY St_Distance(sh_rec.coordinate, S.segment) ASC
                      LIMIT 4) S2
                ORDER BY CAST(((CASE WHEN S2.oneway IS NOT NULL AND S2.oneway='true' THEN MinimumAngle(angle, ST_StartPoint(S2.segment), ST_EndPoint(S2.segment))
                		  ELSE LEAST(MinimumAngle(angle,ST_StartPoint(S2.segment), ST_EndPoint(S2.segment)),
                                      MinimumAngle(angle, ST_EndPoint(S2.segment), ST_StartPoint(S2.segment))) END)/10) AS INTEGER) ASC                       
                         ,ST_Distance(lastCoordinate, ST_ClosestPoint(S2.segment, sh_rec.coordinate))
                       		/*(CASE WHEN S2.oneway IS NOT NULL AND S2.oneway='true' THEN ST_Distance(geography(lastCoordinate), geography(ST_StartPoint(S2.segment)),true)
                		          ELSE LEAST(ST_Distance(geography(lastCoordinate), geography(ST_StartPoint(S2.segment)),true),
                                             ST_Distance(geography(lastCoordinate), geography(ST_EndPoint(S2.segment)),true)
                                              ) END)*/
                       	  ASC
            	LIMIT 1;
            END IF;
            
            IF(last_seg<>seg_id) THEN
                INSERT into matchedsegments(segment, shape_id, sequencenumber) values(seg_id,sh_rec.shape_id,sh_rec.sequencenumber);
            END IF;
            lastCoordinate:=sh_rec.coordinate;
        end loop;
        
    end;
$$ language plpgsql;