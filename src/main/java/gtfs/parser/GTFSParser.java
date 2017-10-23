package gtfs.parser;

import gtfs.entities.GTFS;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.apache.commons.io.input.BOMInputStream;


/**
 * A GTFS entity parser using Template Method pattern.
 * 
 * @param <T> the GTFS entity class.
 */
public abstract class GTFSParser<T extends GTFS> {
    
    /**
     * Tells wether the parser is ready or not.
     * Subclasses that need a preparation should override this method.
     * 
     * @return true if the parser is ready, false otherwise.
     */
    public boolean isReady(){
        return true;
    }
    
    /**
     * Parses a GTFS entity from a feed.
     * 
     * @param dirpath The directory path of the GTFS feed. Must be not null.
     * @return a Collection of entities from the feed
     * @throws RuntimeException if {@link #isReady()} returns false;
     * @throws FileNotFoundException if the directory does not contain the GTFS file specified in method getFileName
     * @throws IOException if an error occurs during file reading
     */
    public Collection<T> parse(String dirpath) throws FileNotFoundException, IOException{
        if(!isReady())
            throw new RuntimeException("Parser not ready"); //TODO custom exception?
        Collection<T> result = new HashSet<>();
        // build file path
        if(dirpath.charAt(dirpath.length()-1)!='/') dirpath += "/";
        String path = dirpath + getFileName();
        
        int numberOfParameters = numberOfParameters();
        int[] columnToParameter = new int[numberOfParameters];
        String row[] = new String[numberOfParameters];
        
        int rowCount = 0;
        BOMInputStream input = new BOMInputStream(new FileInputStream(path),false);//Ignore BOM
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(input))){
            // read the header
            String line = reader.readLine();
            String firstRow[] = line.split(",");
            int columnsFound = firstRow.length;
            rowCount++;
            
            // associate each column with the corresponding parameter number
            for(int j=0 ; j<numberOfParameters ; j++)
                columnToParameter[j] = -1;
            int i = 0;
            for(String s : line.split(",")){
                int p = columnToParameter(s);
                if(p==-1) // unknown column
                    throw new RuntimeException(String.format("Unexpected column \"%s\"", s));
                columnToParameter[i++] = p;
            }
            
            // check if all required fields have been found
            if(!checkRequired(firstRow))
                throw new RuntimeException("Required columns missing");
            
            // read all the lines
            while((line = reader.readLine())!=null){
                rowCount++;
                // fix the row if it ends with ',' without a 
                // this prevents the split method from not finding the last column
                if(line.charAt(line.length()-1)==',')
                    line += " ";
                // split the row by ','
                // ',' protected with double quotes are ignored
                String token[] = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",-1); // divides the rows into tokens

                // check the number of fields of the row
                if(token.length<columnsFound)
                    throw new RuntimeException("Too few columns at line "+rowCount);
                if(token.length>columnsFound)
                    throw new RuntimeException("Too many columns "+Arrays.toString(token)+" at line "+rowCount);
                
                // fill the fields array using the right order of the parameters
                Arrays.fill(row, null);
                for(int j=0;j<token.length;j++){
                    String param = token[j].replace("\"", "").trim();
                    if(!param.isEmpty())
                        row[columnToParameter[j]] = param;
                }
                
                // processe the row
                processRow(row, result);                
            }
        }
        
        // check if the dataset is not empty
        if(rowCount<=1)
            throw new RuntimeException("Empty dataset");
        
        clear();
        return result;
    }
    
    /**
     * Clears parser resources for possibile reuse.
     * Subclasses should override this method also to help the garbage collector.
     */
    public void clear(){
    }
    
    /**
     * @return the name of the file to parse (including it's extension).
     */
    public abstract String getFileName();
    
    /**
     * Associates each column with the corresponding parameter number.
     * <p>The method must map each column in the continous range of integers
     * that goes from 0 to the value returned by {@link #numberOfParameters()}</p>
     * 
     * @param name The name of the column.
     * @return the parameter number if the column is known, -1 otherwise. Parameter count starts with 0.
     */
    protected abstract int columnToParameter(String name);
    
    /**
     * Checks that all the required fields have been found in the first row of the file.
     * 
     * @param firstRow The first row of the file splitted into tokens. Strings that contain only whitespaces are passed as null. 
     * @return false if the first row doesn't contain all the required rows, true otherwise.
     */
    protected abstract boolean checkRequired(String firstRow[]);
    
    /**
     * Processes the current row splitted into tokens.
     * Subclasses should add the object created in the method in the result collection.
     * 
     * @param parameters The row divided into token. Strings that contain only whitespaces are passed null.
     * @param result the result being calculated.
     */
    protected abstract void processRow(String parameters[], Collection<T> result);
    
    /**
     * @return the maximum number of columns of the file.
     */
    public abstract int numberOfParameters();
}
