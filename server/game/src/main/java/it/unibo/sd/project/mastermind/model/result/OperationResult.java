package it.unibo.sd.project.mastermind.model.result;

public class OperationResult {
    protected final short statusCode;
    protected final String resultMessage;

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
