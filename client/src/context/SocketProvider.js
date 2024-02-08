import { createContext, useEffect, useState } from "react";
import EventBus from "@vertx/eventbus-bridge-client.js";
import useAuth from "../hooks/useAuth";
import {
    BACKEND_SOCKET_ENDPOINT,
    EVENTS_NOTIFICATION_ADDRESS,
    PLAYER_REGISTRATION_ADDRESS,
    PLAYER_STATUS_ADDRESS,
} from "../api/backend_endpoints";
import { useSnackbar } from "notistack";

const SocketContext = createContext({});

export const SocketProvider = ({ children }) => {
    const { auth } = useAuth();
    const [socket, setSocket] = useState(null);
    const [allPlayersStatus, setAllPlayersStatus] = useState([]);
    const { enqueueSnackbar } = useSnackbar();

    const displayGameEvents = (_error, message) => {
        const body = JSON.parse(message.body);
        const notificationType = body.notificationType;
        const opponent = body.originPlayer;
        const snackbarOptions = {
            variant: "info",
            autoHideDuration: 3500,
        };
        switch (notificationType) {
            case NotificationTypes.NEW_MATCH:
                enqueueSnackbar("New match found", snackbarOptions);
                break;
            case NotificationTypes.NEW_MOVE:
                enqueueSnackbar(
                    "New move made on match against " + opponent,
                    snackbarOptions
                );
                break;
            case NotificationTypes.MATCH_OVER:
                enqueueSnackbar(
                    "Match against " + opponent + " is over!",
                    snackbarOptions
                );
                break;
            default:
                console.error("Invalid notification type");
                break;
        }
    };

    const registerHandler = (
        handler,
        address = EVENTS_NOTIFICATION_ADDRESS
    ) => {
        try {
            socket.state === 1 &&
                socket.registerHandler(address + auth.username, handler);
        } catch (error) {
            console.log(error);
        }
    };

    const closeSocket = () => {
        if (socket) {
            try {
                sendStatusChange(PlayerStatus.OFFLINE);
                socket.close();
            } catch (error) {
                console.log(error);
            }
            setSocket(null);
        }
    };

    const registerSocket = () => {
        try {
            socket.send(
                PLAYER_REGISTRATION_ADDRESS,
                {
                    username: auth.username,
                    socketAddress: getRelativeSocketAddress(
                        socket.sockJSConn._transport?.url
                    ),
                },
                (_, currentPlayersStatus) => {
                    setAllPlayersStatus(currentPlayersStatus.body);
                }
            );
        } catch (error) {
            console.log(error);
        }
    };

    const updatePlayerStatus = (_error, message) => {
        const body = JSON.parse(message.body);

        setAllPlayersStatus((prevState) => {
            const existingPlayer = prevState.find(
                (player) => player.username === body.originPlayer
            );

            if (existingPlayer) {
                existingPlayer.status = body.playerStatus;
            } else {
                prevState.push({
                    username: body.originPlayer,
                    status: body.playerStatus,
                });
            }
            return [...prevState];
        });
    };

    const sendStatusChange = (newStatus) => {
        try {
            socket.send(PLAYER_STATUS_ADDRESS, {
                originPlayer: auth.username,
                playerStatus: newStatus,
                socketAddress: getRelativeSocketAddress(
                    socket.sockJSConn._transport?.url
                ),
            });
        } catch (error) {
            console.log(error);
        }
    };

    const getRelativeSocketAddress = (absoluteAddress) => {
        try {
            const url = new URL(absoluteAddress);
            return url.pathname;
        } catch (error) {
            console.error(error);
            return absoluteAddress;
        }
    };

    const socketOptions = {
        vertxbus_reconnect_attempts_max: 500, // Max reconnect attempts
        vertxbus_reconnect_delay_min: 500, // Initial delay (in ms) before first reconnect attempt
        vertxbus_reconnect_delay_max: 2000,
    };

    useEffect(() => {
        if (Object.keys(auth).length > 0) {
            const socket = new EventBus(BACKEND_SOCKET_ENDPOINT, socketOptions);
            socket.enableReconnect(true);
            setSocket(socket);
        } else setSocket(null);

        // eslint-disable-next-line
    }, [auth]);

    useEffect(() => {
        if (socket)
            socket.onopen = () => {
                registerSocket();

                registerHandler((error, message) =>
                    displayGameEvents(error, message)
                );

                registerHandler(
                    (error, message) => updatePlayerStatus(error, message),
                    PLAYER_STATUS_ADDRESS
                );
            };
        // eslint-disable-next-line
    }, [socket]);

    const NotificationTypes = {
        NEW_MATCH: "newMatch",
        NEW_MOVE: "newMove",
        MATCH_OVER: "matchOver",
    };

    const PlayerStatus = {
        ONLINE: "online",
        PLAYING: "playing",
        OFFLINE: "offline",
    };

    return (
        <SocketContext.Provider
            value={{
                socket,
                allPlayersStatus,
                PlayerStatus,
                sendStatusChange,
                closeSocket,
                registerHandler,
            }}
        >
            {children}
        </SocketContext.Provider>
    );
};

export default SocketContext;
