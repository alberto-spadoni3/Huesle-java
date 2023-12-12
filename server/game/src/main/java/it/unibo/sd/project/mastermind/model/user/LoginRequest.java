package it.unibo.sd.project.mastermind.model.user;

public class LoginRequest {
    private final String username;
    private final String clearPassword;

    public LoginRequest(String username, String clearPassword) {
        this.username = username;
        this.clearPassword = clearPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getClearPassword() {
        return clearPassword;
    }
}
