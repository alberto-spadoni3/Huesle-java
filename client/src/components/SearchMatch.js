import { Box, Button, Fade, Typography } from "@mui/material";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import SearchIcon from "@mui/icons-material/Search";
import PeopleOutlineRoundedIcon from "@mui/icons-material/PeopleOutlineRounded";
import EmojiPeopleRoundedIcon from "@mui/icons-material/EmojiPeopleRounded";
import SearchPrivateMatchDialog from "./SearchPrivateMatchDialog";
import JoinPrivateMatchDialog from "./JoinPrivateMatchDialog";
import BackButton from "./BackButton";
import { BACKEND_SEARCH_MATCH_ENDPOINT } from "../api/backend_endpoints";
import { useSnackbar } from "notistack";
import BottomBar from "./BottomBar";
import useAxiosPrivate from "../hooks/useAxiosPrivate";

const SearchMatch = () => {
    const navigate = useNavigate();
    const axiosPrivate = useAxiosPrivate();
    const { enqueueSnackbar } = useSnackbar();

    const [creatingPrivateMatch, setCreatingPrivateMatch] = useState(false);
    const [searchPrivateOpen, setSearchPrivateOpen] = useState(false);
    const [secretCode, setSecretCode] = useState("");
    const [joinPrivateOpen, setJoinPrivateOpen] = useState(false);

    const generatePublicMatch = async () => {
        try {
            const response = await axiosPrivate.post(
                BACKEND_SEARCH_MATCH_ENDPOINT,
                JSON.stringify({ secret: false })
            );

            enqueueSnackbar(response.data?.resultMessage, {
                variant: "info",
                autoHideDuration: 2500,
            });

            navigate("/dashboard", { replace: true });

        } catch (error) {
            console.log(error);
            if (error.response?.status === 400) {
                enqueueSnackbar(error.response?.data, {
                    variant: "warning",
                    autoHideDuration: 2500,
                });
            } else console.log(error);
        }
    };

    const generatePrivateMatch = async () => {
        setCreatingPrivateMatch(true);
        try {
            const response = await axiosPrivate.post(
                BACKEND_SEARCH_MATCH_ENDPOINT,
                JSON.stringify({ secret: true })
            );
            setSecretCode(response.data?.matchAccessCode);
            setSearchPrivateOpen(true);
        } catch (error) {
            enqueueSnackbar("Error in comunicating with Server", {
                variant: "error",
                autoHideDuration: 2500,
            });
            setSearchPrivateOpen(false);
            setCreatingPrivateMatch(false);
        }
    };

    return (
        <>
            <BackButton />
            <Fade in={true}>
                <Box
                    sx={{
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                    }}
                >
                    <Typography
                        variant="h6"
                        color="text.primary"
                        sx={{
                            textAlign: "center",
                            marginTop: 1,
                        }}
                    >
                        Public Matches
                    </Typography>

                    <Button
                        sx={{
                            width: "100%",
                            height: "50px",
                            marginTop: 2,
                            marginBottom: 2,
                        }}
                        color="button"
                        variant="contained"
                        startIcon={<SearchIcon />}
                        aria-label="Search Match"
                        onClick={(_e) => generatePublicMatch()}
                    >
                        Search Match
                    </Button>

                    <Typography
                        variant="h6"
                        color="text.primary"
                        sx={{
                            textAlign: "center",
                            marginTop: 2,
                        }}
                    >
                        Private Matches
                    </Typography>
                    {!creatingPrivateMatch ? (
                        <Button
                            sx={{ width: "100%", height: "50px", marginTop: 2 }}
                            variant="contained"
                            startIcon={<EmojiPeopleRoundedIcon />}
                            aria-label="Create Match"
                            onClick={() => generatePrivateMatch()}
                            color="button"
                        >
                            Create Match
                        </Button>
                    ) : (
                        <Button
                            sx={{
                                width: "100%",
                                height: "50px",
                                marginTop: 2,
                                borderColor: "background.border",
                            }}
                            variant="outlined"
                            disableRipple={true}
                            startIcon={<EmojiPeopleRoundedIcon />}
                            aria-label="Join Match"
                            color="button"
                        >
                            Generating Match...
                        </Button>
                    )}
                    <SearchPrivateMatchDialog
                        connectOpen={searchPrivateOpen}
                        setConnectOpen={setSearchPrivateOpen}
                        secretCode={secretCode}
                        setSearchOver={setCreatingPrivateMatch}
                    />

                    <Button
                        sx={{
                            width: "100%",
                            height: "50px",
                            marginTop: 2,
                        }}
                        variant="contained"
                        startIcon={<PeopleOutlineRoundedIcon />}
                        aria-label="Join Match"
                        onClick={(_e) => setJoinPrivateOpen(true)}
                        color="button"
                    >
                        Join Match
                    </Button>
                    <JoinPrivateMatchDialog
                        open={joinPrivateOpen}
                        setOpen={setJoinPrivateOpen}
                    />
                    {/* FOOTER */}
                    <BottomBar></BottomBar>
                </Box>
            </Fade>
        </>
    );
};

export default SearchMatch;
