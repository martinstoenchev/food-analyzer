package bg.sofia.uni.fmi.mjt.server.exceptions;

public class BadInputParameterException extends FoodAnalyzerException {

    public BadInputParameterException(String message) {
        super(message);
    }

    public BadInputParameterException(String message, Exception e) {
        super(message, e);
    }

}
