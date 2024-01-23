import React, { useState, useEffect, forwardRef } from "react";
import {
    Button,
    LinearProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Slide,
} from "@mui/material";
import {
    BACKEND_SEARCH_MATCH_ENDPOINT,
    BASE_NOTIFICATION_ADDRESS,
} from "../api/backend_endpoints";
import { useSnackbar } from "notistack";
import { useNavigate } from "react-router-dom";
import useSocket from "../hooks/useSocket";
import useAxiosPrivate from "../hooks/useAxiosPrivate";
import useAuth from "../hooks/useAuth";

const Transition = forwardRef(function Transition(props, ref) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export default function SearchPrivateMatchDialog({
    connectOpen,
    setConnectOpen,
    secretCode,
    setSearchOver,
}) {
    const { enqueueSnackbar } = useSnackbar();
    const axiosPrivate = useAxiosPrivate();
    const { socket, socketOpened } = useSocket();
    const navigate = useNavigate();
    const { auth } = useAuth();

    const handleClose = async (event) => {
        event.preventDefault();
        try {
            await axiosPrivate.delete(BACKEND_SEARCH_MATCH_ENDPOINT);
            enqueueSnackbar("Stopped hosting the private match", {
                variant: "info",
                autoHideDuration: 2500,
            });
        } catch (error) {
            if (!error?.response) {
                console.log(error);
            }
        }
        setConnectOpen(false);
        setSearchOver(false);
    };

    useEffect(() => {
        if (socketOpened)
            socket.registerHandler(
                BASE_NOTIFICATION_ADDRESS + auth.username,
                (_e, _m) => {
                    setConnectOpen(false);
                    setSearchOver(false);
                    navigate("/dashboard", { replace: true });
                }
            );

        window.addEventListener("unload", handleClose);
        return () => {
            window.removeEventListener("unload", handleClose);
        };
    }, [connectOpen]);

    return (
        <div>
            <Dialog
                open={connectOpen}
                TransitionComponent={Transition}
                keepMounted
                onClose={handleClose}
                aria-describedby="alert-dialog-slide-description"
            >
                <DialogTitle>{"Private Match Search"}</DialogTitle>
                <DialogContent>
                    <DialogContentText
                        id="alert-dialog-slide-description"
                        textAlign="center"
                    >
                        Secret Code: {secretCode}
                    </DialogContentText>
                    <LinearProgress sx={{ m: 1.5 }} color="inherit" />
                </DialogContent>
                <DialogActions>
                    <Button
                        sx={{ color: "text.secondary" }}
                        onClick={handleClose}
                    >
                        Cancel
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
