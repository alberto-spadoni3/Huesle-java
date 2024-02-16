import SaveIcon from "@mui/icons-material/Save";
import { TextField, Typography, Box, Button, Divider } from "@mui/material";
import BackButton from "./BackButton";
import useAxiosPrivate from "../hooks/useAxiosPrivate";
import { useState, useEffect } from "react";
import { useSnackbar } from "notistack";
import {
    BACKEND_UPDATE_EMAIL,
    BACKEND_UPDATE_PASSWORD_ENDPOINT,
} from "../api/backend_endpoints";
import UserPictureSelector from "./UserPictureSelector";
import useRefreshToken from "../hooks/useRefreshToken";
import useSocket from "../hooks/useSocket";

const EMAIL_REGEX = /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/;
const PASSWORD_REGEX =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$/;

const EditUserProfile = () => {
    const refresh = useRefreshToken();
    const axiosPrivate = useAxiosPrivate();
    const { enqueueSnackbar } = useSnackbar();

    const [email, setEmail] = useState("");
    const [validEmail, setValidEmail] = useState(false);
    const [emailUpdated, setEmailUpdated] = useState(false);

    const [oldPassword, setOldPassword] = useState("");

    const [password, setPassword] = useState("");
    const [validPassword, setValidPassword] = useState(false);

    const [matchPassword, setMatchPassword] = useState("");
    const [validMatchPassword, setValidMatchPassword] = useState(false);

    const { lostConnection } = useSocket();

    useEffect(() => {
        setValidEmail(EMAIL_REGEX.test(email));
    }, [email]);

    useEffect(() => {
        setValidPassword(PASSWORD_REGEX.test(password));
        setValidMatchPassword(password === matchPassword);
    }, [password, matchPassword]);

    const handleEditProfile = async (_e) => {
        const emailPresentAndValid = email.trim() !== "" && validEmail;
        const passwordPresentAndValid =
            password.trim() !== "" && validPassword && validMatchPassword;

        if (emailPresentAndValid) {
            try {
                const response = await axiosPrivate.post(
                    BACKEND_UPDATE_EMAIL,
                    JSON.stringify({ newEmail: email })
                );

                if (response.status === 200) {
                    console.log("Email updated");
                    setEmail("");
                    setEmailUpdated(true);
                    enqueueSnackbar(response.data.resultMessage, {
                        variant: "success",
                        autoHideDuration: 2500,
                    });
                }
            } catch (error) {
                if (!error?.response) {
                    console.log("No Server Response");
                    enqueueSnackbar("No Server Response", { variant: "info" });
                } else if (error.response?.status === 409) {
                    enqueueSnackbar(error.response?.data?.resultMessage, {
                        variant: "warning",
                        autoHideDuration: 2500,
                    });
                } else {
                    console.log("Email update failed");
                    enqueueSnackbar(error.response?.data?.resultMessage, {
                        variant: "error",
                        autoHideDuration: 2500,
                    });
                }
            }
        }

        if (passwordPresentAndValid) {
            try {
                const response = await axiosPrivate.post(
                    BACKEND_UPDATE_PASSWORD_ENDPOINT,
                    JSON.stringify({
                        oldPassword,
                        newPassword: password,
                    }),
                    { withCredentials: true }
                );

                if (response.status === 200) {
                    console.log("password updated");
                    enqueueSnackbar(response?.data?.resultMessage, {
                        variant: "success",
                        autoHideDuration: 2500,
                    });
                    setOldPassword("");
                    setPassword("");
                    setMatchPassword("");
                }
            } catch (error) {
                if (!error?.response) {
                    console.log("No Server Response");
                    enqueueSnackbar("No Server Response", {
                        variant: "info",
                        autoHideDuration: 2500,
                    });
                } else if (error.response?.status >= 400) {
                    enqueueSnackbar(error.response?.data?.resultMessage, {
                        variant: "error",
                        autoHideDuration: 2500,
                    });
                }
            }
        }
    };

    useEffect(() => {
        const refreshToken = async () => {
            if (emailUpdated) {
                await refresh();
                setEmailUpdated(false);
            }
        };
        refreshToken();
        // eslint-disable-next-line
    }, [emailUpdated]);

    return (
        <>
            <BackButton />
            <Box
                sx={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                }}
            >
                <UserPictureSelector />

                <Box
                    sx={{
                        width: "100%",
                        display: "flex",
                        justifyContent: "flex-start",
                        marginTop: 2,
                    }}
                >
                    <Box justifyContent="flex-start" width="inherit">
                        <Typography color="text.primary" variant="h5">
                            Email Address
                        </Typography>
                        <TextField
                            fullWidth
                            error={!!(!validEmail && email)}
                            id="editEmail"
                            label="New Email"
                            name="editEmail"
                            autoComplete="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            sx={{ mt: 1 }}
                        />

                        <Divider sx={{ m: 3, borderBottomWidth: "thick" }} />

                        <Typography color="text.primary" variant="h5">
                            Password
                        </Typography>
                        <TextField
                            fullWidth
                            name="oldPassword"
                            label="Old Password"
                            type="password"
                            id="oldPassword"
                            autoComplete="off"
                            value={oldPassword}
                            onChange={(e) => setOldPassword(e.target.value)}
                            sx={{ mt: 1 }}
                        />
                        <TextField
                            fullWidth
                            error={!!(!validPassword && password)}
                            name="newPassword"
                            label="New Password"
                            type="password"
                            id="newPassword"
                            autoComplete="off"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            sx={{ mt: 1 }}
                        />
                        <TextField
                            fullWidth
                            error={!!(!validMatchPassword && matchPassword)}
                            name="matchNewPassword"
                            label="Confirm New Password"
                            type="password"
                            id="matchNewPassword"
                            autoComplete="off"
                            value={matchPassword}
                            onChange={(e) => setMatchPassword(e.target.value)}
                            sx={{ mt: 1 }}
                        />
                    </Box>
                </Box>

                <Button
                    sx={{
                        width: "100%",
                        height: "50px",
                        margin: "24px 0 24px 0",
                    }}
                    variant="contained"
                    disabled={lostConnection}
                    startIcon={<SaveIcon />}
                    aria-label="Save Changes"
                    color="button"
                    onClick={handleEditProfile}
                >
                    Save changes
                </Button>
            </Box>
        </>
    );
};

export default EditUserProfile;
