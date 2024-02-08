import React, { forwardRef } from "react";
import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Slide,
} from "@mui/material";

const Transition = forwardRef(function Transition(props, ref) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export default function ConfirmationDialog({
    openStatus,
    setOpenStatus,
    title,
    message,
    callbackOnYes,
}) {
    const handleClose = (event) => {
        event.preventDefault();
        setOpenStatus(false);
    };

    const doAction = async (event) => {
        callbackOnYes();
        handleClose(event);
    };

    return (
        <>
            <Dialog
                open={openStatus}
                TransitionComponent={Transition}
                keepMounted
                onClose={handleClose}
                aria-describedby="leave-dialog-slide-description"
            >
                <DialogTitle>{title}</DialogTitle>
                <DialogContent>
                    <DialogContentText
                        id="alert-dialog-slide-description"
                        textAlign="center"
                    >
                        {message}
                    </DialogContentText>
                    <DialogActions>
                        <Button
                            sx={{ color: "text.secondary" }}
                            onClick={doAction}
                        >
                            Yes
                        </Button>
                        <Button
                            sx={{ color: "text.secondary" }}
                            onClick={handleClose}
                        >
                            No
                        </Button>
                    </DialogActions>
                </DialogContent>
            </Dialog>
        </>
    );
}
