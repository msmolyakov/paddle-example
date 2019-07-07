package lib.exceptions;

public class NodeError extends Error {

    public NodeError(Throwable e) {
        super(e);
    }

    public NodeError(String message) {
        super(message);
    }

}
