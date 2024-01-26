package it.unibo.sd.project.webservice.rabbit;

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
    DO_GUESS("doGuess"),

    // Settings-related types
    GET_SETTINGS("getSettings"),
    UPDATE_SETTINGS("updateSettings"),
    UPDATE_PROFILE_PIC("updateProfilePicId"),
    UPDATE_EMAIL("updateEmail"),
    UPDATE_PASSWORD("updatePassword"),

    // Stats-related types
    GET_USER_STATS("getUserStats"),

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
