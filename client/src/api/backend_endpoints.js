//User Management
export const BACKEND_LOGIN_ENDPOINT = "/user/login";
export const BACKEND_SETTINGS_ENDPOINT = "/protected/settings/profileSettings";
export const BACKEND_REGISTRATION_ENDPOINT = "/user/register";
export const BACKEND_UPDATE_EMAIL = "/protected/settings/updateEmail";
export const BACKEND_UPDATE_PASSWORD_ENDPOINT =
    "/protected/settings/updatePassword";
export const BACKEND_REFRESH_TOKEN_ENDPOINT = "/user/refreshToken";
export const BACKEND_LOGOUT_ENDPOINT = "/user/logout";
export const BACKEND_DELETE_USER_ENDPOINT = "/user/delete";

//Match Search
export const BACKEND_SEARCH_MATCH_ENDPOINT = "/protected/game/searchMatch";
export const BACKEND_JOIN_PRIVATE_MATCH_ENDPOINT =
    "/protected/game/joinPrivateMatch";

//Game Moves
export const BACKEND_GET_MATCH_ENDPOINT = "/protected/game/getMatch";
export const BACKEND_DO_GUESS_ENDPOINT = "/protected/game/doGuess";
export const BACKEND_LEAVE_MATCH_ENDPOINT = "/protected/game/leaveMatch";

//Socket
const BACKEND_HOST = process.env.BACKEND_HOST || "localhost";
export const BACKEND_SOCKET_ENDPOINT =
    "http://" + BACKEND_HOST + ":8080/eventbus";
export const BASE_NOTIFICATION_ADDRESS = "huesle.";
export const EVENTS_NOTIFICATION_ADDRESS =
    BASE_NOTIFICATION_ADDRESS + "notification.events.";
export const PLAYER_REGISTRATION_ADDRESS =
    BASE_NOTIFICATION_ADDRESS + "notification.player-registration.";
export const PLAYER_STATUS_ADDRESS =
    BASE_NOTIFICATION_ADDRESS + "notification.player-status.";

//Stats Management
export const BACKEND_GET_MATCHES_ENDPOINT = "/protected/game/getMatches";
export const BACKEND_UPDATE_USER_PIC_ENDPOINT =
    "/protected/settings/profilePicture";
export const BACKEND_GET_USER_STATS_ENDPOINT = "/protected/stats/userStats";
