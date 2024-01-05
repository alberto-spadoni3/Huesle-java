package it.unibo.sd.project.mastermind.model.match;

public class PendingMatchRequest {
    private final String requesterUsername;
    private String matchAccessCode;

    public PendingMatchRequest(String requesterUsername) {
        this.requesterUsername = requesterUsername;
        this.matchAccessCode = null;
    }

    public PendingMatchRequest(String requesterUsername, String matchAccessCode) {
        this.requesterUsername = requesterUsername;
        this.matchAccessCode = matchAccessCode;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public String getMatchAccessCode() {
        return matchAccessCode;
    }

    public void setMatchAccessCode(String matchAccessCode) {
        if (this.matchAccessCode == null)
            this.matchAccessCode = matchAccessCode;
    }
}
