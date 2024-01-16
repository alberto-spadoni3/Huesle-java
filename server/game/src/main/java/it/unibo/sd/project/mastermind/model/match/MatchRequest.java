package it.unibo.sd.project.mastermind.model.match;

import it.unibo.sd.project.mastermind.model.OperationRequest;

public class MatchRequest extends OperationRequest {
    private boolean isMatchPrivate;
    private String matchAccessCode;
    protected String matchID;

    public MatchRequest(String requesterUsername) {
        super(requesterUsername);
        this.isMatchPrivate = false;
        this.matchAccessCode = null;
        this.matchID = null;
    }

    public boolean isMatchPrivate() {
        return isMatchPrivate;
    }

    public String getMatchAccessCode() {
        return matchAccessCode;
    }

    public String getMatchID() {
        return matchID;
    }

    public MatchRequest setMatchPrivate(boolean isMatchPrivate) {
        this.isMatchPrivate = isMatchPrivate;
        return this;
    }

    public MatchRequest setMatchAccessCode(String matchAccessCode) {
        if (matchAccessCode != null)
            this.matchAccessCode = matchAccessCode;
        return this;
    }

    public MatchRequest setMatchID(String matchID) {
        if (matchID != null)
            this.matchID = matchID;
        return this;
    }
}
