package it.unibo.sd.project.mastermind.model.result;

import it.unibo.sd.project.mastermind.model.user.Player;

public class UserOperationResult extends OperationResult {
    private final Player relatedUser;
    private final String accessToken;

    public UserOperationResult(short statusCode, String resultMessage) {
        super(statusCode, resultMessage);
        this.relatedUser = null;
        this.accessToken = null;
    }

    public UserOperationResult(short statusCode, String resultMessage, Player user) {
        super(statusCode, resultMessage);
        this.relatedUser = user;
        this.accessToken = "null";
    }

    public UserOperationResult(short statusCode, String resultMessage, Player user, String accessToken) {
        super(statusCode, resultMessage);
        this.relatedUser = user;
        this.accessToken = accessToken;
    }

    public Player getRelatedUser() {
        return relatedUser;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
