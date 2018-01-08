CREATE OR REPLACE FUNCTION CalculateTimes() RETURNS VOID AS $$
DECLARE
    curs CURSOR FOR (SELECT COALESCE(R.shortname,R.longname) AS name,
                            T.id AS trip, T.shape AS shape, T.calendar AS calendar,
                            gtfsTimeToSeconds(ST.ARRIVAL) AS arrival, S.coordinate AS coordinate
                    FROM ((GTFS.ROUTES R JOIN GTFS.TRIPS T on T.route=R.id) JOIN GTFS.STOP_TIMES ST ON ST.trip=T.id) JOIN GTFS.STOPS S ON ST.stop=S.id
                    WHERE R.type=3 AND T.shape IS NOT NULL
                    ORDER BY ST.trip ASC, ST.sequenceNumber ASC);
    freq_curs CURSOR FOR (SELECT COALESCE(R.shortname, R.longname) AS name,
                          T.id AS trip, T.shape AS shape, T.calendar AS calendar,
                          F.id AS frequency
                          FROM (GTFS.ROUTES R JOIN GTFS.TRIPS T ON T.route=R.id) JOIN GTFS.FREQUENCIES F ON f.trip=t.ID
                          WHERE R.type=3 AND T.shape IS NOT NULL
                          ORDER BY trip ASC);
    trip_id TEXT;
    shp_sequence INTEGER;
    shp_target INTEGER;
    last_time INTEGER;
    seg_id INTEGER;
    avg_vel float;
    path_rec RECORD;
    dist_traveled float;

    -- for frequencies
    j INTEGER;
    st_rec RECORD;
    secs INTEGER;
BEGIN 
    
    CREATE TEMPORARY TABLE TRIP_PATH_TIMES
    (
      segment INTEGER,
      secs INTEGER -- seconds passed from starttime
    )
    ON COMMIT DROP;

    trip_id := NULL;
    FOR freq_rec IN freq_curs
    LOOP
        IF(trip_id IS NULL OR trip_id<>freq_rec.trip) THEN
            TRUNCATE TRIP_PATH_TIMES;
            trip_id = freq_rec.trip;
            j = 0;
            FOR st_rec IN (SELECT gtfsTimeToSeconds(ST.ARRIVAL) AS arrival, S.coordinate AS coordinate
                           FROM GTFS.stop_times ST JOIN GTFS.stops S ON ST.stop=S.id
                           WHERE ST.trip=trip_id
                           ORDER BY ST.sequencenumber)
            LOOP
                IF (j=0) THEN
                    j := 1;
                    SELECT M.segment INTO seg_id
                    FROM MATCHEDSEGMENTS M
                    WHERE M.SHAPE_ID = cur_rec.shape AND M.SEQUENCENUMBER = 1;

                    --//TODO add frequency info
                    INSERT INTO PASSAGES(SEGMENT,CALENDAR,BUS,TIMEPASS, FREQUENCY) 
                    VALUES(seg_id, freq_rec.calendar, cur_rec.name, freq_rec.arrival, freq_rec.frequency);

                    INSERT INTO TRIP_PATH_TIMES(segment, secs)
                    VALUES(seg_id,0);

                    last_time := st_rec.arrival;
                    shp_sequence := 1;
                    secs := 0;
                ELSE
                    SELECT M.SEQUENCENUMBER INTO shp_target 
                    FROM MATCHEDSEGMENTS M JOIN SEGMENTS S ON M.SEGMENT = S.ID 
                    WHERE M.SHAPE_ID = freq_rec.shape AND M.SEQUENCENUMBER>shp_sequence AND ST_DWITHIN(S.SEGMENT, st_rec.coordinate,0.0001)
                    ORDER BY M.SEQUENCENUMBER ASC LIMIT 1;

                    SELECT SUM(METERS)/(st_rec.arrival-last_time) INTO avg_vel
                    FROM MATCHEDSEGMENTS M JOIN SEGMENTS S ON M.segment = S.id
                    WHERE M.shape_id = cur_rec.shape AND M.sequencenumber>=shp_sequence AND M.sequencenumber<shp_target;

                    dist_traveled := 0;
                    FOR path_rec IN(SELECT S.meters,S.id  
                                    FROM MATCHEDSEGMENTS M JOIN SEGMENTS S ON M.segment = S.id
                                    WHERE M.shape_id = freq_rec.shape AND M.sequencenumber>=shp_sequence AND M.sequencenumber<=shp_target
                                    ORDER BY M.sequenceNumber ASC)
                    LOOP
                        IF(dist_traveled = 0) THEN
                            dist_traveled := path_rec.meters;
                        ELSE 
                            secs := secs + (dist_traveled/avg_vel);

                            INSERT INTO PASSAGES(SEGMENT,CALENDAR,BUS,TIMEPASS, FREQUENCY) 
                            VALUES(path_rec.id, freq_rec.calendar, freq_rec.name, freq_rec.starttime + secs, freq_rec.frequency);

                            INSERT INTO TRIP_PATH_TIMES(segment, secs)
                            VALUES(path_red.id,secs);

                            dist_traveled := dist_traveled + path_rec.meters;
                        END IF;
                    END LOOP;
                    last_time := st_rec.arrival;
                    shp_sequence := shp_target;
                END IF;
            END LOOP;
        ELSE
            INSERT INTO PASSAGES(SEGMENT,CALENDAR,BUS,TIMEPASS, FREQUENCY)
            SELECT TPS.segment, freq_rec.calendar, freq_rec.name, freq_rec.starttime+TPS.secs,freq_rec.frequency
            FROM TRIP_PATH_TIMES TPS;
        END IF;
    END LOOP;

    ----
    trip_id := null;
    FOR cur_rec IN curs 
    LOOP
        IF(trip_id IS NULL OR trip_id <> cur_rec.trip) THEN
            trip_id := cur_rec.trip;
            shp_sequence := 0;
            --Match first time with first segment
            SELECT M.segment INTO seg_id
            FROM MATCHEDSEGMENTS M
            WHERE M.SHAPE_ID = cur_rec.shape AND M.SEQUENCENUMBER = 1;

            INSERT INTO PASSAGES(SEGMENT,CALENDAR,BUS,TIMEPASS) 
            VALUES(seg_id, cur_rec.calendar, cur_rec.name, cur_rec.arrival);

            last_time := cur_rec.arrival;
            shp_sequence := 1;
        ELSE
            SELECT M.SEQUENCENUMBER INTO shp_target 
            FROM MATCHEDSEGMENTS M JOIN SEGMENTS S ON M.SEGMENT = S.ID 
            WHERE M.SHAPE_ID = cur_rec.shape AND M.SEQUENCENUMBER>shp_sequence AND ST_DWITHIN(S.SEGMENT, cur_rec.coordinate,0.0001)
            ORDER BY M.SEQUENCENUMBER ASC LIMIT 1;

            SELECT SUM(METERS)/(cur_rec.arrival-last_time) INTO avg_vel
            FROM MATCHEDSEGMENTS M JOIN SEGMENTS S ON M.segment = S.id
            WHERE M.shape_id = cur_rec.shape AND M.sequencenumber>=shp_sequence AND M.sequencenumber<shp_target;

            dist_traveled := 0;
            FOR path_rec IN(SELECT S.meters,S.id  
                            FROM MATCHEDSEGMENTS M JOIN SEGMENTS S ON M.segment = S.id
                            WHERE M.shape_id = cur_rec.shape AND M.sequencenumber>=shp_sequence AND M.sequencenumber<=shp_target
                            ORDER BY M.sequenceNumber ASC)
            LOOP
                IF(dist_traveled = 0) THEN
                    dist_traveled := path_rec.meters;
                ELSE 
                    INSERT INTO PASSAGES(SEGMENT,CALENDAR,BUS,TIMEPASS) 
                    VALUES(path_rec.id, cur_rec.calendar, cur_rec.name, last_time + dist_traveled/avg_vel);

                    dist_traveled := dist_traveled + path_rec.meters;
                END IF;
            END LOOP;
            last_time := cur_rec.arrival;
            shp_sequence := shp_target;
        
        END IF;
        
    END LOOP;
END;
$$ LANGUAGE plpgsql;