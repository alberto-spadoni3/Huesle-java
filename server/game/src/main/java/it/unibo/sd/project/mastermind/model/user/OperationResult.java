package it.unibo.sd.project.mastermind.model.user;

import it.unibo.sd.project.mastermind.model.Player;

public class OperationResult {
    private final short statusCode;
    private final String resultMessage;
    private final Player relatedUser;
    private final String accessToken;

    public OperationResult(short statusCode, String resultMessage) {
        this.statusCode = statusCode;
        this.resultMessage = resultMessage;
        this.relatedUser = null;
        this.accessToken = null;
    }

    public OperationResult(short statusCode, String resultMessage, Player user, String accessToken) {
        this.statusCode = statusCode;
        this.resultMessage = resultMessage;
        this.relatedUser = user;
        this.accessToken = accessToken;
    }

    public short getStatusCode() {
        return statusCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public Player getRelatedUser() {
        return relatedUser;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
