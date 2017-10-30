package gtfs.parser;

/**
 *  An exception occured while parsing a GTFS file.
 */
public class GTFSParsingException extends RuntimeException{
    public GTFSParsingException(String message) {
        super(message);
    }

    public GTFSParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
