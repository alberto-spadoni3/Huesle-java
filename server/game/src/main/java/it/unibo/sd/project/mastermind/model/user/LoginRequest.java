package it.unibo.sd.project.mastermind.model.user;

import it.unibo.sd.project.mastermind.model.OperationRequest;

public class LoginRequest extends OperationRequest {
    private final String clearPassword;

    public LoginRequest(String username, String clearPassword) {
        super(username);
        this.clearPassword = clearPassword;
    }

    public String getClearPassword() {
        return clearPassword;
    }
}
