package games;

public class InvalidInputException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -2696997529274555630L;

    public InvalidInputException(String message, String input) {
        super(message);
    }

}
