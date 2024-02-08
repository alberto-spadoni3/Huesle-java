import { forwardRef } from "react";
import Button from "@mui/material/Button";
import LinearProgress from "@mui/material/LinearProgress";
import TextField from "@mui/material/TextField";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogContentText from "@mui/material/DialogContentText";
import DialogTitle from "@mui/material/DialogTitle";
import Slide from "@mui/material/Slide";
import { BACKEND_JOIN_PRIVATE_MATCH_ENDPOINT } from "../api/backend_endpoints";
import { useState } from "react";
import { useSnackbar } from "notistack";
import { useNavigate } from "react-router-dom";
import useAxiosPrivate from "../hooks/useAxiosPrivate";

const Transition = forwardRef(function Transition(props, ref) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export default function JoinPrivateMatchDialog({ open, setOpen }) {
    const [searchingOpen, setSearchingOpen] = useState(false);

    const [matchAccessCode, setMatchAccessCode] = useState("");
    const { enqueueSnackbar } = useSnackbar();
    const axiosPrivate = useAxiosPrivate();
    const navigate = useNavigate();

    const handleSearch = async (event) => {
        event.preventDefault();

        if (matchAccessCode.length !== 5) {
            enqueueSnackbar("Secret Code not valid", {
                variant: "warning",
                autoHideDuration: 2500,
            });
            return;
        }

        handleCodeClose();
        setSearchingOpen(true);
        try {
            const response = await axiosPrivate.post(
                BACKEND_JOIN_PRIVATE_MATCH_ENDPOINT,
                JSON.stringify({ matchAccessCode })
            );
            if (response) {
                navigate("/dashboard", { replace: true });
            }
        } catch (error) {
            enqueueSnackbar("No match found with that secret code", {
                variant: "warning",
                autoHideDuration: 2500,
            });
        }
        handleSearchClose();
    };

    const handleCodeClose = () => {
        setOpen(false);
    };

    const handleSearchClose = () => {
        setSearchingOpen(false);
        setMatchAccessCode("");
    };

    return (
        <div>
            <Dialog
                open={open}
                fullWidth
                maxWidth="xs"
                TransitionComponent={Transition}
                keepMounted
                onClose={handleCodeClose}
                aria-describedby="alert-dialog-slide-description"
            >
                <DialogTitle>{"Private Match Search"}</DialogTitle>
                <DialogContent>
                    <DialogContentText
                        id="alert-dialog-slide-description"
                        textAlign="center"
                    >
                        Insert the access code and press Search
                    </DialogContentText>
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="matchAccessCode"
                        label="Match Access Code"
                        name="matchAccessCode"
                        onChange={(e) => setMatchAccessCode(e.target.value)}
                        value={matchAccessCode}
                        autoComplete="off"
                    />
                </DialogContent>
                <DialogActions>
                    <Button
                        sx={{ color: "text.secondary" }}
                        onClick={handleCodeClose}
                    >
                        Cancel
                    </Button>
                    <Button
                        sx={{ color: "text.secondary" }}
                        onClick={handleSearch}
                    >
                        Search
                    </Button>
                </DialogActions>
            </Dialog>
            <Dialog
                open={searchingOpen}
                TransitionComponent={Transition}
                keepMounted
                onClose={handleSearchClose}
                aria-describedby="alert-dialog-slide-description"
            >
                <DialogTitle>{"Private match search"}</DialogTitle>
                <DialogContent>
                    <DialogContentText
                        id="alert-dialog-slide-description"
                        textAlign="center"
                    >
                        Secret Code: {matchAccessCode}
                    </DialogContentText>
                    <LinearProgress sx={{ m: 1.5 }} color="inherit" />
                </DialogContent>
                <DialogActions>
                    <Button
                        sx={{ color: "text.secondary" }}
                        onClick={handleSearchClose}
                    >
                        Cancel
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
