CREATE OR REPLACE FUNCTION taxiToPassages() RETURNS VOID AS $$
DECLARE
	t record;
    name text;

BEGIN 
	FOR t IN  (SELECT * from Taxi_traj_dataset) LOOP
    	INSERT INTO PASSAGES(segment,vehicle,passage_time)
        SELECT R::int, 'taxi:'||t.taxi_id,extract(epoch from t.time_sf)
        FROM unnest(regexp_split_to_array(t.roads,',')) as R;
        
    END LOOP;
END;
$$ LANGUAGE plpgsql;