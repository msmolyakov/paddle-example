package lib.api.exceptions;

public class NodeError extends Error {
    //trace[]
    //tx{}
    int error;
    String message;

    public NodeError(int error, String message) {
        this.error = error;
        this.message = message;
    }

    public NodeError() {
        this(-1, "Test lib: Unknown error");
    }

    @Override
    public String getMessage() {
        return error + ": " + message;
    }
}
