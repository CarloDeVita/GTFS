-- FUNCTION: public.minimumangle(double precision, geometry, geometry)

-- DROP FUNCTION public.minimumangle(double precision, geometry, geometry);

CREATE OR REPLACE FUNCTION public.minimumangle(
	angle double precision,
	p1 geometry,
	p2 geometry)
    RETURNS double precision
    LANGUAGE 'plpgsql'

AS $BODY$

DECLARE
	angle2 float;
BEGIN
	angle2 := abs(degrees(ST_Azimuth(p1,p2))-angle);
    IF(angle2>180) THEN angle2 := 360-angle2; END IF;
    RETURN angle2;
END;

$BODY$;
