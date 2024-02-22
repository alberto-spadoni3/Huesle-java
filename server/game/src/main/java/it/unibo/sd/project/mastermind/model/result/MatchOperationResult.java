package it.unibo.sd.project.mastermind.model.result;

import it.unibo.sd.project.mastermind.model.match.Match;

import java.util.List;

public class MatchOperationResult extends OperationResult {
    private String matchAccessCode;
    private List<Match> matches;
    private boolean pendingMatchPresence;

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

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public void setMatchAccessCode(String matchAccessCode) {
        this.matchAccessCode = matchAccessCode;
    }

    public void setPendingMatchPresence(boolean pendingMatchPresence) {
        this.pendingMatchPresence = pendingMatchPresence;
    }

    public boolean isPendingMatchPresence() {
        return pendingMatchPresence;
    }
}
