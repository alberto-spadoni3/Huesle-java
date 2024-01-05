package it.unibo.sd.project.mastermind.model.match;

import it.unibo.sd.project.mastermind.model.OperationResult;

public class MatchOperationResult extends OperationResult {
    private final String matchAccessCode;

    public MatchOperationResult(short statusCode, String resultMessage) {
        super(statusCode, resultMessage);
        this.matchAccessCode = null;
    }

    public MatchOperationResult(short statusCode, String resultMessage, String matchAccessCode) {
        super(statusCode, resultMessage);
        this.matchAccessCode = matchAccessCode;
    }

    public String getMatchAccessCode() {
        return this.matchAccessCode;
    }
}
