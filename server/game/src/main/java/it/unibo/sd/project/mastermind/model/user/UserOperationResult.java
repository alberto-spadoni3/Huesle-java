package it.unibo.sd.project.mastermind.model.user;

import it.unibo.sd.project.mastermind.model.OperationResult;
import it.unibo.sd.project.mastermind.model.Player;

public class UserOperationResult extends OperationResult {
    private final Player relatedUser;
    private final String accessToken;

    public UserOperationResult(short statusCode, String resultMessage) {
        super(statusCode, resultMessage);
        this.relatedUser = null;
        this.accessToken = null;
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
