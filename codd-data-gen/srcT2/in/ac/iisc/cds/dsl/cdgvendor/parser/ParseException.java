package in.ac.iisc.cds.dsl.cdgvendor.parser;

public class ParseException extends RuntimeException {

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

}
