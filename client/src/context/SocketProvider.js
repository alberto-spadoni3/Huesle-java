import { useState, createContext, useEffect } from "react";
import EventBus from "@vertx/eventbus-bridge-client.js";
import useAuth from "../hooks/useAuth";
import {
    BACKEND_SOCKET_ENDPOINT,
    BASE_NOTIFICATION_ADDRESS,
} from "../api/backend_endpoints";
import { useSnackbar } from "notistack";

const SocketContext = createContext({});

export const SocketProvider = ({ children }) => {
    const { auth } = useAuth();
    const [socket, setSocket] = useState({});
    const [socketOpened, setSocketOpened] = useState(false);
    const { enqueueSnackbar } = useSnackbar();

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
            socket.onopen = () => {
                setSocketOpened(true);
                socket.registerHandler(
                    BASE_NOTIFICATION_ADDRESS + auth.username,
                    (_, message) => {
                        const body = JSON.parse(message.body);
                        const notificationType = body.notificationType;
                        const opponent = body.originPlayer;
                        const snackbarOptions = {
                            variant: "info",
                            autoHideDuration: 3500,
                        };
                        switch (notificationType) {
                            case NotificationTypes.NEW_MATCH:
                                enqueueSnackbar(
                                    "New match found",
                                    snackbarOptions
                                );
                                break;
                            case NotificationTypes.NEW_MOVE:
                                enqueueSnackbar(
                                    "New move made on match against " +
                                        opponent,
                                    snackbarOptions
                                );
                                break;
                            case NotificationTypes.MATCH_OVER:
                                enqueueSnackbar(
                                    "Match against " + opponent + " is over!",
                                    snackbarOptions
                                );
                                break;
                        }
                    }
                );
            };
        } else setSocket({});

        return () => {
            closeSocket();
        };
    }, [auth]);

    const closeSocket = () => {
        if (socketOpened) {
            socket.close();
            setSocketOpened(false);
            setSocket({});
        }
    };

    const NotificationTypes = {
        NEW_MATCH: "newMatch",
        NEW_MOVE: "newMove",
        MATCH_OVER: "matchOver",
    };

    return (
        <SocketContext.Provider
            value={{
                socket,
                closeSocket,
                socketOpened,
                NotificationTypes,
            }}
        >
            {children}
        </SocketContext.Provider>
    );
};

export default SocketContext;
