package it.unibo.sd.project.mastermind.model.user;

public class OperationResult {
    private final short statusCode;
    private final String resultMessage;

    public OperationResult(short statusCode, String resultMessage) {
        this.statusCode = statusCode;
        this.resultMessage = resultMessage;
    }

    public short getStatusCode() {
        return statusCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }
}
