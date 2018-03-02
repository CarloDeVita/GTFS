create or replace function minimumangle(
        ref_angle double precision,
        segment geometry,
        p1 geometry,
        p2 geometry,
        oneway boolean)
RETURNS double precision
AS $$
DECLARE
    angle double precision;
    angle2 double precision;
    fraction1 double precision;
    fraction2 double precision;
    p1_interpolated geometry;
    p2_interpolated geometry;
BEGIN
    fraction1 = ST_LineLocatePoint(segment,p1);
    fraction2 = ST_LineLocatePoint(segment,p2);
   
    IF(ST_NumPoints(segment)=2 OR fraction1=fraction2) THEN
        p1_interpolated := ST_StartPoint(segment);
        p2_interpolated := ST_EndPoint(segment);
    ELSIF(oneway AND fraction1>fraction2) THEN --swap interpolated points if needed
        p1_interpolated := ST_LineInterpolatePoint(segment, fraction2);
        p2_interpolated := ST_LineInterpolatePoint(segment, fraction1);
    ELSE
        p1_interpolated := ST_LineInterpolatePoint(segment, fraction1);
        p2_interpolated := ST_LineInterpolatePoint(segment, fraction2);        
    END IF;
    
    angle := abs(degrees(ST_Azimuth(p1_interpolated, p2_interpolated)) - ref_angle);
    IF(angle>180) THEN angle:= 360-angle; END IF;

    IF(NOT oneway) THEN
        angle2 := abs(degrees(ST_Azimuth(p2_interpolated,p1_interpolated)) - ref_angle);
        IF(angle2>180) THEN angle2 := 360-angle2; END IF;
        IF(angle2<angle) THEN angle := angle2; END IF;
    END IF;

    return angle;
END ; $$ LANGUAGE 'plpgsql'

