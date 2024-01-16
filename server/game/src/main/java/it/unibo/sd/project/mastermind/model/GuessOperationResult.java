package it.unibo.sd.project.mastermind.model;

import it.unibo.sd.project.mastermind.model.match.Match;

public class GuessOperationResult extends OperationResult {
    private final Match playedMatch;

    public GuessOperationResult(short statusCode, String resultMessage) {
        super(statusCode, resultMessage);
        playedMatch = null;
    }

    public GuessOperationResult(short statusCode, String resultMessage, Match playedMatch) {
        super(statusCode, resultMessage);
        this.playedMatch = playedMatch;
    }

    public Match getPlayedMatch() {
        return playedMatch;
    }
}
