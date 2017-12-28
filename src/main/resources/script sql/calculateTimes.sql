CREATE OR REPLACE FUNCTION CalculateTimes() RETURNS VOID AS $$
DECLARE
    
    route TEXT;
    trip_cursor CURSOR FOR (SELECT * FROM GTFS.TRIPS );
    route_cursor CURSOR FOR(SELECT COALESCE(shortname,longname) AS NAME, ID FROM GTFS.ROUTES WHERE TYPE=3);
    stp_cursor CURSOR FOR  (SELECT ST.TRIP, ST.ARRIVAL, ST.SEQUENCENUMBER,S.COORDINATE
                            FROM GTFS.STOP_TIMES ST JOIN GTFS.STOP S ON ST.STOP = S.ID
                            ORDER BY ST.TRIP ASC, ST.SEQUENCENUMBER ASC );
    stp_rec RECORD;
    trip_rec RECORD;
    trip_id TEXT;
    shp_sequence INTEGER;
    shp_target INTEGER;
    last_time INTEGER;
    seg_id INTEGER;
    len float;
    avg_vel float;
    path_rec RECORD;
    dist_traveled float;

BEGIN 
    
    FOR route_rec IN route_cursor
    LOOP
        FOR trip_rec IN (SELECT * FROM GTFS.TRIPS T WHERE T.ROUTE = route_rec.id AND T.shape IS NOT NULL)
        LOOP
            shp_sequence := 0;
            FOR stp_rec IN (SELECT gtfsTimeToSeconds(ST.ARRIVAL) AS arrival,S.COORDINATE 
                            FROM GTFS.STOP_TIMES ST JOIN GTFS.STOPS S ON ST.STOP = S.ID
                            WHERE ST.trip = trip_rec.id
                            ORDER BY ST.sequenceNumber ASC)
            LOOP
                IF(shp_sequence = 0) THEN
                    SELECT M.segment INTO seg_id
                    FROM MATCHEDSEGMENTS M
                    WHERE M.SHAPE_ID = trip_rec.shape AND M.SEQUENCENUMBER = 1;

                    INSERT INTO PASSAGES(SEGMENT,CALENDAR,BUS,TIMEPASS) 
                    VALUES(seg_id, trip_rec.calendar, route_rec.name, stp_rec.arrival);
                        
                    last_time := stp_rec.arrival;
                    shp_sequence := 1;
                ELSE 
                    SELECT S.ID, M.SEQUENCENUMBER INTO seg_id, shp_target 
                    FROM MATCHEDSEGMENTS M JOIN SEGMENTS S ON M.SEGMENT = S.ID 
                    WHERE M.SHAPE_ID = trip_rec.shape AND M.SEQUENCENUMBER>shp_sequence AND ST_DWITHIN(S.SEGMENT, stp_rec.coordinate,0.0001)
                    ORDER BY M.SEQUENCENUMBER ASC LIMIT 1;
                    
                    SELECT SUM(METERS)/(stp_rec.arrival-last_time) INTO avg_vel
                    FROM MATCHEDSEGMENTS M JOIN SEGMENTS S ON M.segment = S.id
                    WHERE M.shape_id = trip_rec.shape AND M.sequencenumber>=shp_sequence AND M.sequencenumber<shp_target;
                    
                    dist_traveled := 0;
                    FOR path_rec IN(SELECT S.meters,S.id  
                                    FROM MATCHEDSEGMENTS M JOIN SEGMENTS S ON M.segment = S.id
                                    WHERE M.shape_id = trip_rec.shape AND M.sequencenumber>=shp_sequence AND M.sequencenumber<=shp_target
                                    ORDER BY M.sequenceNumber ASC)
                    LOOP
                        IF(dist_traveled = 0) THEN
                            dist_traveled := path_rec.meters;
                        ELSE 

                            INSERT INTO PASSAGES(SEGMENT,CALENDAR,BUS,TIMEPASS) 
                            VALUES(path_rec.id, trip_rec.calendar, route_rec.name, last_time + dist_traveled*avg_vel);
                      
                            dist_traveled := dist_traveled + path_rec.meters;
                        END IF;
                    END LOOP;
                    last_time := stp_rec.arrival;
                    shp_sequence := shp_target;
                END IF;
            END LOOP;
            return;
        END LOOP;
    END LOOP;

END;
$$ LANGUAGE plpgsql;

