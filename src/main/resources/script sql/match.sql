-- FUNCTION: public.matchseg()

-- DROP FUNCTION public.matchseg();

CREATE OR REPLACE FUNCTION public.matchseg(
	)
    RETURNS void
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE 
    ROWS 0
AS $BODY$

    declare
    	sh_cur cursor for select * from gtfs.shape_points ORDER BY shape_id, sequencenumber;
        seg_id INTEGER;
        last_seg INTEGER;
        lastCoordinate geometry;
        lastShape TEXT;
        angle float;
        i INTEGER;
        --
        p INTEGER;--REFCURSOR;--pgr_costResult;
        patharray pgr_costResult[];
        lastsource INTEGER;
        lasttarget INTEGER;
        currsource INTEGER;
        currtarget INTEGER;
        seg geometry;
    begin
        lastShape=null;
        last_seg=null;
    	for sh_rec in sh_cur loop
            IF(lastShape IS NULL OR lastShape<>sh_rec.shape_id) THEN 
                lastShape:= sh_rec.shape_id;
                i := 1;
                last_seg:= null;
                lastCoordinate:=null;
                lastsource=null;
                lasttarget=null;
                currsource=null;
                currtarget=null;

                select s.id,S.source, S.target INTO seg_id,currsource,currtarget
                FROM segments S
                WHERE ST_DWithin(sh_rec.coordinate, S.segment, 0.0001)
                ORDER BY St_Distance(sh_rec.coordinate, S.segment)
                LIMIT 1;    
            ELSE
            	select degrees(st_azimuth(lastCoordinate,sh_rec.coordinate))
                into angle;
            	
                select S2.id, S2.source, S2.target, S2.segment INTO seg_id, currsource, currtarget, seg
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
            
            IF(last_seg IS NULL OR last_seg<>seg_id) THEN
                IF(last_seg IS NOT NULL) THEN
                    seg := ST_MakeLine(lastCoordinate, ST_StartPoint(seg));
                    /*patharray := pgr_astar('SELECT id,source,target,meters, ST_X(ST_StartPoint(segment)), ST_Y(ST_StartPoint(segment)), ST_X(ST_EndPoint(segment)), ST_Y(ST_EndPoint(segment)) FROM public.segments
                                            WHERE ST_DWithin(.....,segment)<0.0009',lasttarget ,currsource,false,false);*/
                    FOR p IN (SELECT edge FROM pgr_astar('SELECT id ,source,target,meters AS cost,CASE WHEN oneway=''true'' THEN -1 ELSE meters END AS reverse_cost, ST_X(ST_StartPoint(segment)) AS x1, ST_Y(ST_StartPoint(segment)) AS y1, ST_X(ST_EndPoint(segment)) AS x2, ST_Y(ST_EndPoint(segment)) AS y2 FROM public.segments
                                            WHERE ST_DWithin(ST_GeomFromText('''|| ST_AsText(seg) ||''',4326),segment,0.0002) AND source IS NOT NULL AND target IS NOT NULL',lasttarget ,currsource,false))
                    LOOP
                        IF(p<>last_seg AND p<>seg_id) THEN
                            INSERT INTO matchedsegments(segment, shape_id, sequencenumber) values(p,sh_rec.shape_id,i);
                            i := i+1;
                        END IF;
                    END LOOP;
                END IF;
                INSERT into matchedsegments(segment, shape_id, sequencenumber) values(seg_id,sh_rec.shape_id,i);
                i := i+1;
                last_seg := seg_id;
            END IF;
            lastCoordinate:=sh_rec.coordinate;
            lastsource:=currsource;
            lasttarget:=currtarget;
        end loop;
        
    end;

$BODY$;

ALTER FUNCTION public.matchseg()
    OWNER TO postgres;
