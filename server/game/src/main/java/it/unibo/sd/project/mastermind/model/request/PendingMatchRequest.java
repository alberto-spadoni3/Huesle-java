package it.unibo.sd.project.mastermind.model.request;

public class PendingMatchRequest extends OperationRequest {
    private String matchAccessCode;

    public PendingMatchRequest(String requesterUsername) {
        super(requesterUsername);
        this.matchAccessCode = null;
    }

    public String getMatchAccessCode() {
        return matchAccessCode;
    }

    public void setMatchAccessCode(String matchAccessCode) {
        if (this.matchAccessCode == null)
            this.matchAccessCode = matchAccessCode;
    }
}
