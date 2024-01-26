package it.unibo.sd.project.mastermind.model.user;

import it.unibo.sd.project.mastermind.model.OperationResult;

public class StatsOperationResult extends OperationResult {
    private final int matchesWon;
    private final int matchesLost;
    private final int matchesDrawn;

    public StatsOperationResult(short statusCode, String resultMessage) {
        super(statusCode, resultMessage);
        matchesWon = 0;
        matchesLost = 0;
        matchesDrawn = 0;
    }

    public StatsOperationResult(short statusCode, String resultMessage,
                                int matchesWon, int matchesLost, int matchesDrawn) {
        super(statusCode, resultMessage);
        this.matchesWon = matchesWon;
        this.matchesLost = matchesLost;
        this.matchesDrawn = matchesDrawn;
    }

    public int getMatchesWon() {
        return matchesWon;
    }

    public int getMatchesLost() {
        return matchesLost;
    }

    public int getMatchesDrawn() {
        return matchesDrawn;
    }
}
