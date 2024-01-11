package it.unibo.sd.project.mastermind.model.match;

public class MatchRequest {
    private String requesterUsername;
    private boolean isMatchPrivate;
    private String matchAccessCode;
    private String matchID;

    public MatchRequest(String requesterUsername) {
        this.requesterUsername = requesterUsername;
        this.isMatchPrivate = false;
        this.matchAccessCode = null;
        this.matchID = null;
    }

    public String getRequesterUsername() {
        return requesterUsername;
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
