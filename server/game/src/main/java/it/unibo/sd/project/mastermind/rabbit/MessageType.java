package it.unibo.sd.project.mastermind.rabbit;

public enum MessageType {
    CREATE("create"),
    JOIN("join"),
    START("start"),
    FINISH("finish"),
    UPDATE("update"),
    DISCONNECT("disconnect"),

    // User-related types
    REGISTER_USER("register"),
    LOGIN_USER("login"),
    REFRESH_ACCESS_TOKEN("refreshAccessToken"),
    LOGOUT_USER("logout");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
