package it.unibo.sd.project.mastermind.rabbit;

public enum MessageType {
    // User-related types
    REGISTER_USER("register"),
    LOGIN_USER("login"),
    REFRESH_ACCESS_TOKEN("refreshAccessToken"),
    LOGOUT_USER("logout"),

    // Game-related types
    SEARCH_MATCH("createMatch"),
    JOIN_PRIVATE_MATCH("joinPrivateMatch"),
    CANCEL_MATCH_SEARCH("cancelMatchSearch"),
    GET_MATCHES_OF_USER("getMatches"),
    GET_MATCH("getMatch"),
    LEAVE_MATCH("leaveMatch"),
    DO_GUESS("doGuess");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
