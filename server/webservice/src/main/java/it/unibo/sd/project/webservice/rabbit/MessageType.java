package it.unibo.sd.project.webservice.rabbit;

public enum MessageType {
    // Game-related types
    SEARCH_MATCH("createMatch"),
    JOIN_PRIVATE_MATCH("joinPrivateMatch"),
    GET_MATCHES_OF_USER("getMatches"),
    GET_MATCH("getMatch"),
    LEAVE_MATCH("leaveMatch"),
    DO_GUESS("doGuess"),

    // User-related types
    REGISTER_USER("register"),
    LOGIN_USER("login"),
    REFRESH_ACCESS_TOKEN("refreshAccessToken"),
    LOGOUT_USER("logout"),

    // Websocket-related types
    CREATE_ROOM("createRoom"),
    NEW_MATCH("newMatch"),
    NEW_MOVE("newMove"),
    MATCH_OVER("matchOver");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
