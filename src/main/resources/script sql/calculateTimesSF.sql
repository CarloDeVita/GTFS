CREATE OR REPLACE FUNCTION CalculateTimes() RETURNS VOID AS $$
DECLARE
    stop_times_cursor CURSOR FOR (SELECT COALESCE(R.shortname,R.longname) AS name,
                            T.id AS trip, T.shape AS shape, T.calendar AS calendar,
                            gtfsTimeToSeconds(ST.ARRIVAL) AS arrival, S.coordinate AS coordinate
                    FROM ((GTFS.ROUTES R JOIN GTFS.TRIPS T on T.route=R.id) JOIN GTFS.STOP_TIMES ST ON ST.trip=T.id) JOIN GTFS.STOPS S ON ST.stop=S.id
                    WHERE R.type=3 AND T.shape IS NOT NULL
                    ORDER BY ST.trip ASC, ST.sequenceNumber ASC);
    trip_id TEXT;
    shp_sequence INTEGER;
    shp_target INTEGER;
    last_time INTEGER;
    seg_id INTEGER;
    avg_spd float;
    path_rec RECORD;
    dist_traveled float;
    active boolean[];

    last_calendar TEXT;

    start_epoch INTEGER;
    end_epoch INTEGER;
    loop_time INTEGER;
    first_day INTEGER;
    current_day INTEGER;
    

    n_trips INTEGER;
BEGIN 
    last_calendar := null;
    trip_id := null;
    n_trips := 0;
    FOR cur_rec IN stop_times_cursor
    LOOP
        IF(trip_id IS NULL OR trip_id <> cur_rec.trip) THEN -- new trip being calculated
            -- save calendar information if you don't have them
            IF(last_calendar IS NULL OR last_calendar<>cur_rec.calendar) THEN
                SELECT  EXTRACT(epoch FROM c.startDate),EXTRACT (epoch FROM c.enddate), extract(isodow from c.startdate)-1,
                        array[monday,tuesday,wednesday,thursday,friday,saturday,sunday] 
                INTO start_epoch,end_epoch,first_day, active
                FROM GTFS.CALENDARS C WHERE C.ID = cur_rec.calendar;
            END IF;

            trip_id := cur_rec.trip;
            n_trips := n_trips + 1;
            RAISE NOTICE 'n trips = %', n_trips;

            --Match first time with first segment
            SELECT M.segment INTO seg_id
            FROM MATCHEDSEGMENTS M
            WHERE M.SHAPE_ID = cur_rec.shape AND M.SEQUENCENUMBER = 1;

            -- loop through all calendar days
            loop_time := start_epoch;
            current_day := first_day;
            LOOP
                EXIT WHEN (loop_time>end_epoch);
                IF(active[current_day+1] = TRUE) THEN -- if trip is active on that day
                    INSERT INTO PASSAGES(SEGMENT,VEHICLE,PASSAGE_TIME) 
                    VALUES(seg_id, cur_rec.name, loop_time + cur_rec.arrival);
                END IF;
                loop_time := loop_time + 86400;
                current_day := (current_day+1)%7;
            END LOOP;
            
            last_time := cur_rec.arrival;
            shp_sequence := 1;

        ELSIF last_time<> cur_rec.arrival THEN -- condition to avoid division by zero

            -- find with the closest segment among the matched ones that comes after the last found
            SELECT M.SEQUENCENUMBER INTO shp_target 
            FROM MATCHEDSEGMENTS M JOIN SEGMENTS S ON M.SEGMENT = S.ID 
            WHERE M.SHAPE_ID = cur_rec.shape AND M.SEQUENCENUMBER>shp_sequence AND ST_DWITHIN(S.SEGMENT, cur_rec.coordinate,0.00025)
            ORDER BY M.SEQUENCENUMBER ASC LIMIT 1;

            -- calculate the average speed from the last segment to the one found throw the matched trajectory
            SELECT SUM(meters)/(cur_rec.arrival-last_time)::float INTO avg_spd
            FROM MATCHEDSEGMENTS M JOIN SEGMENTS S ON M.segment = S.id
            WHERE M.shape_id = cur_rec.shape AND M.sequencenumber>=shp_sequence AND M.sequencenumber<shp_target;

            -- loop through the path
            dist_traveled := 0;
            FOR path_rec IN(SELECT S.meters,S.id  
                            FROM MATCHEDSEGMENTS M JOIN SEGMENTS S ON M.segment = S.id
                            WHERE M.shape_id = cur_rec.shape AND M.sequencenumber>=shp_sequence AND M.sequencenumber<=shp_target
                            ORDER BY M.sequencenumber ASC)
            LOOP
                IF(dist_traveled <> 0) THEN --first one is ignored
                    
                    -- loop through calendar days
                    loop_time := start_epoch + last_time;
                    current_day := first_day;
                    LOOP
                        EXIT WHEN (loop_time>end_epoch);
                        IF(active[current_day+1] = TRUE) THEN 
                            INSERT INTO PASSAGES(SEGMENT,VEHICLE,PASSAGE_TIME) 
                            VALUES(path_rec.id, cur_rec.name, loop_time+(dist_traveled/avg_spd));
                        END IF;
                        loop_time := loop_time + 86400;
                        current_day := (current_day+1)%7;
                    END LOOP;

                END IF;
                dist_traveled := dist_traveled + path_rec.meters;
            END LOOP;
            last_time := cur_rec.arrival;
            shp_sequence := shp_target;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;