package it.unibo.sd.project.mastermind.model.match;

import it.unibo.sd.project.mastermind.model.OperationResult;

import java.util.List;

public class MatchOperationResult extends OperationResult {
    private final String matchAccessCode;
    private final List<Match> matches;
    private final boolean pendingMatchPresence;

    public MatchOperationResult(short statusCode, String resultMessage) {
        super(statusCode, resultMessage);
        this.matchAccessCode = null;
        this.matches = null;
        pendingMatchPresence = false;
    }

    public MatchOperationResult(short statusCode, String resultMessage, List<Match> matches, boolean pendingMatchPresence) {
        super(statusCode, resultMessage);
        this.matchAccessCode = null;
        this.matches = matches;
        this.pendingMatchPresence = pendingMatchPresence;
    }

    public MatchOperationResult(short statusCode, String resultMessage, String matchAccessCode) {
        super(statusCode, resultMessage);
        this.matchAccessCode = matchAccessCode;
        this.matches = null;
        this.pendingMatchPresence = false;
    }

    public String getMatchAccessCode() {
        return this.matchAccessCode;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public boolean isPendingMatchPresence() {
        return pendingMatchPresence;
    }
}
