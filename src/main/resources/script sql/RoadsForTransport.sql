CREATE OR REPLACE VIEW RoadsForTransport AS
SELECT * FROM planet_osm_line 
where highway NOT IN ('path', 'footway', 'pedestrian', 'bridleway', 'steps','raceway', 'cycleway','proposed','construction')