package it.unibo.sd.project.mastermind.model;

public class GuessRequest extends OperationRequest {
    private final String matchID;
    private final Attempt attempt;

    public GuessRequest(String requesterUsername, String matchID, Attempt attempt) {
        super(requesterUsername);
        this.matchID = matchID;
        this.attempt = attempt;
    }

    public Attempt getAttempt() {
        return attempt;
    }

    public String getMatchID() {
        return matchID;
    }
}
