package bg.sofia.uni.fmi.mjt.server.exceptions;

public class NoResultsFoundException extends FoodAnalyzerException {

    public NoResultsFoundException(String message) {
        super(message);
    }

    public NoResultsFoundException(String message, Exception e) {
        super(message, e);
    }

}
