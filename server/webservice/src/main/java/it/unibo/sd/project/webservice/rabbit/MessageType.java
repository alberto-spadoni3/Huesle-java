package it.unibo.sd.project.webservice.rabbit;

public enum MessageType {
    SEARCH_MATCH("createMatch"),
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
