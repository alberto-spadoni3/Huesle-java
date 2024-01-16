package it.unibo.sd.project.mastermind.model;

public class OperationRequest {
    private final String requesterUsername;

    protected OperationRequest(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }
}
