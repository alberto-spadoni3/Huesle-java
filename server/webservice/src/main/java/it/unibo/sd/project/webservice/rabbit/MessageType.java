package it.unibo.sd.project.webservice.rabbit;

public enum MessageType {
    SEARCH_MATCH("createMatch"),
    JOIN_PRIVATE_MATCH("joinPrivateMatch"),
    GET_MATCHES_OF_USER("getMatches"),
    GET_MATCH("getMatch"),
    LEAVE_MATCH("leaveMatch"),

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
