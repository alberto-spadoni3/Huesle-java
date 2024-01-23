import { useState, createContext, useEffect } from "react";
import EventBus from "@vertx/eventbus-bridge-client.js";
import useAuth from "../hooks/useAuth";
import { BACKEND_SOCKET_ENDPOINT } from "../api/backend_endpoints";
import { useSnackbar } from "notistack";

const SocketContext = createContext({});

export const SocketProvider = ({ children }) => {
    const BASE_ADDRESS = "huesle.";
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
                    BASE_ADDRESS + auth.username,
                    (_, message) => {
                        const body = JSON.parse(message.body);
                        console.log(body);
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

    // useEffect(() => {
    //     if (socketOpened) {
    //         socket.registerHandler(
    //             BASE_ADDRESS + auth.username,
    //             (_, message) => {
    //                 const body = JSON.parse(message.body);
    //                 console.log(body);
    //                 const notificationType = body.notificationType;
    //                 const opponent = body.originPlayer;
    //                 switch (notificationType) {
    //                     case NotificationTypes.NEW_MATCH:
    //                         enqueueSnackbar("New match found", {
    //                             variant: "info",
    //                             autoHideDuration: 2500,
    //                         });
    //                         break;
    //                     case NotificationTypes.NEW_MOVE:
    //                         enqueueSnackbar(
    //                             "New move made on match against " + opponent,
    //                             {
    //                                 variant: "info",
    //                                 autoHideDuration: 2500,
    //                             }
    //                         );
    //                         break;
    //                     case NotificationTypes.MATCH_OVER:
    //                         enqueueSnackbar(
    //                             "Match against " + opponent + " is over!",
    //                             {
    //                                 variant: "info",
    //                                 autoHideDuration: 2500,
    //                             }
    //                         );
    //                         break;
    //                 }
    //             }
    //         );
    //     }
    // }, [socket]);

    const registerHandler = (username, callback) => {
        if (socketOpened) {
            console.log("reg1");
            socket.registerHandler(BASE_ADDRESS + username, (_, msg) =>
                callback(msg)
            );
        }
    };

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
                registerHandler,
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
