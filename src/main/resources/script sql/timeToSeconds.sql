CREATE OR REPLACE FUNCTION gtfsTimeToSeconds(t text) RETURNS INTEGER AS $$
DECLARE
    tarray text[];
BEGIN
    tarray = regexp_split_to_array(t,':');
    return ((tarray[1]::integer)*3600 + (tarray[2]::integer)*60 + tarray[2]::integer);
END;



$$ LANGUAGE plpgsql;
