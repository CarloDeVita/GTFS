CREATE OR REPLACE FUNCTION PopulateTransits() RETURNS void AS $$
DECLARE
	trip gtfs.trips%ROWTYPE;
    routeName TEXT;
    lastRouteID INTEGER;
	service_cursor CURSOR FOR SELECT * FROM gtfs.calendars;
	trip_cursor CURSOR FOR SELECT *
    				 FROM (SELECT R.name, T.id, T.shape, T.calendar 
                           FROM gtfs.trips T JOIN gtfs.routes R ON T.route=R.id
                           WHERE T.shape IS NOT NULL) I;
   	startWeek INTEGER;
    endWeek INTEGER;
    startYear INTEGER;
    endYear INTEGER;
    startDay INTEGER;
    endDay INTEGER;
BEGIN
	CREATE TABLE IF NOT EXISTS TRANSITS(
    	segment INTEGER,
        vehicle TEXT,
        week SMALLINT,
        timeslot SMALLINT,
        counters INTEGER[7],
        CONSTRAINT seg_fk FOREIGN KEY (segment) REFERENCES SEGMENTS(segment),
        CONSTRAINT week_chk CHECK (week>=1 and week<=53),
        --CONSTRAINT dayofweek_chk CHECK (dayofweek>=1 and dayofweek<=7),
        CONSTRAINT timeslot_chk CHECK (timeslot>=1 and timeslot<=4) --defines the timeslots considered
    );
    
    -- for all the schedules
    FOR calendar IN service_cursor LOOP
    	startWeek := EXTRACT(week FROM calendar.startdate);
        endWeek := EXTRACT(week FROM calendar.enddate);
        -- get all the trips that adhere to the schedule
    	lastRouteID := NULL;
        FOR trip IN (SELECT * FROM gtfs.trips T WHERE T.calendar=calendar.id AND T.shape IS NOT NULL) LOOP
        	IF(lastRouteID IS NULL OR lastRouteID!=T.route) THEN
            	SELECT name INTO routeName
        		FROM gtfs.routes WHERE id=trip.route;
            END IF;
            lastRouteID := T.route;
        	
            
            
            FOR w IN startWeek..endWeek LOOP
            	
        	END LOOP;
        END LOOP;
        
    END LOOP;
END;
$$ LANGUAGE plpgsql;