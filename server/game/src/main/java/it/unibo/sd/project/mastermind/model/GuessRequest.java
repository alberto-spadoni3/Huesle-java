package it.unibo.sd.project.mastermind.model;

import it.unibo.sd.project.mastermind.model.match.MatchRequest;

public class GuessRequest extends MatchRequest {
    private final Attempt attempt;

    public GuessRequest(String requesterUsername, Attempt attempt) {
        super(requesterUsername);
        this.attempt = attempt;
    }

    public Attempt getAttempt() {
        return attempt;
    }
}
