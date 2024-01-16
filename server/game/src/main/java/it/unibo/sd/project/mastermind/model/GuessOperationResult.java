package it.unibo.sd.project.mastermind.model;

public class GuessOperationResult extends OperationResult {
    private final Hints submittedAttemptHints;

    public GuessOperationResult(short statusCode, String resultMessage) {
        super(statusCode, resultMessage);
        submittedAttemptHints = null;
    }

    public GuessOperationResult(short statusCode, String resultMessage, Hints submittedAttemptHints) {
        super(statusCode, resultMessage);
        this.submittedAttemptHints = submittedAttemptHints;
    }

    public Hints getSubmittedAttemptHints() {
        return submittedAttemptHints;
    }
}
