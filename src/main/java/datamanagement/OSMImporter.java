package datamanagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Launches a process that imports data fetched from OpenStreetMap into a local database.
 * The data format currently supported is xml.
 * 
 */
public class OSMImporter {
    private String username;
    private String password;
    private String database;
    private String directory;
    private int port = 5432;
    private String exec;

    /**
     * 
     * @param username the username to access the database.
     * @return the objects itself so that multiple setter calls can be chained.
     */
    public OSMImporter setUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * 
     * @param password the password to access the database.
     * @return the objects itself so that multiple setter calls can be chained.
     */
    public OSMImporter setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * 
     * @param database the name of the destination database.
     * @return the objects itself so that multiple setter calls can be chained.
     */
    public OSMImporter setDatabase(String database) {
        this.database = database;
        return this;
    }
    
    /**
     * 
     * @param port the listen port of the DBMS.
     * @return the objects itself so that multiple setter calls can be chained.
     */
    public OSMImporter setPort(int port){
        if(port<0) throw new IllegalArgumentException();
        this.port = port;
        return this;
    }

    /**
     * 
     * @param directory the directory of the executable. Must be not null.
     * @return the objects itself so that multiple setter calls can be chained.
     */
    public OSMImporter setDirectory(String directory) {
        this.directory = directory;
        return this;
    }
    
    /**
     * 
     * @param exec the name of the executable.
     * @return the objects itself so that multiple setter calls can be chained.
     */
    public OSMImporter setExecutableName(String exec){
        this.exec = exec;
        return this;
    }

    /**
     * 
     * @param filePath the path of the file containing the OpenStreetMap data.
     * @return true if the import was successful, false otherwise.
     */
    public boolean importData(String filePath){
        if(username==null || password==null || database==null || directory==null) return false;
        // build command
        List<String> command = new LinkedList<>();
        command.add(directory+"\\"+exec); //executable
        command.add("-S");command.add(directory+"\\default.style"); //style file
        command.add("-l"); //WGS84 lat-lon projection
        //command.add("-s"); // slim mode
        command.add("-r");command.add("xml"); //specify file format
        command.add("-U");command.add(username); //Set database user
        command.add("-W"); //Get password from input
        command.add("-d");command.add(database); //Set database
        command.add("-P");command.add(Integer.toString(port)); //Set port
        command.add(filePath); // osm input file
        
        try {
            // create process
            ProcessBuilder pBuilder = new ProcessBuilder(command);
            pBuilder.redirectErrorStream(true);
            System.out.println("-----------------------------------------------------------");
            System.out.println("Executing : "+command);
            Process process = pBuilder.start();
            
            // write the password into the input stream of the sub-process
            OutputStream out = process.getOutputStream();
            out.write((password+"\n").getBytes());
            out.close();

            // consume output to make the process end            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
                System.out.println(line);
          
            // wait for the process to end and check the exit code
            int ret = process.waitFor();
            System.out.println("-----------------------------------------------------------");
            return ret==0;
        } catch (IOException ex) {
            Logger.getLogger(OSMImporter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }catch(InterruptedException ex){ return false; }
    }
}
