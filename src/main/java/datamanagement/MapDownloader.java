package datamanagement;
import com.vividsolutions.jts.geom.Envelope;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

/**
 * Downloads an OpenStreetMap region delimited by a bounding box.
 * 
 */
public class MapDownloader {
    
    /**
     * Downloads the OpenStreetMap region delimited by a bounding box.
     * The output file extension is ".osm";
     * 
     * @param env the bounding box.
     * @param dir the directory of the output file. If omitted it is replaced with the default temporary directory.
     * @param fileName the name of the output file. If omitted it is replaced with "map".
     */
    public void download(Envelope env, String dir, String fileName) throws IOException{
        String urlString = String.format(Locale.US,"http://overpass-api.de/api/map/interpreter?data=[bbox];(way[highway];node(w));out;&bbox=%.4f,%.4f,%.4f,%.4f",env.getMinX(), env.getMinY(), env.getMaxX(),env.getMaxY());
        URL url = new URL(urlString);

        if(dir==null) dir = System.getProperty("java.io.tmpdir");
        if(fileName==null) fileName = "map";

        Path path = Paths.get(dir+"\\"+fileName+".osm");
        System.out.println("Downloading data from : "+urlString);
        try (InputStream in = url.openStream()){
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
