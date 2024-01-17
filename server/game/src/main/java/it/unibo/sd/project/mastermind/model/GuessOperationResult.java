package it.unibo.sd.project.mastermind.model;

import it.unibo.sd.project.mastermind.model.match.MatchStatus;

;

public class GuessOperationResult extends OperationResult {
    private final MatchStatus updatedState;
    private final Hints submittedAttemptHints;

    public GuessOperationResult(short statusCode, String resultMessage) {
        super(statusCode, resultMessage);
        submittedAttemptHints = null;
        updatedState = null;
    }

    public GuessOperationResult(short statusCode, String resultMessage, MatchStatus updatedState, Hints submittedAttemptHints) {
        super(statusCode, resultMessage);
        this.updatedState = updatedState;
        this.submittedAttemptHints = submittedAttemptHints;
    }

    public MatchStatus getUpdatedState() {
        return updatedState;
    }

    public Hints getSubmittedAttemptHints() {
        return submittedAttemptHints;
    }
}
