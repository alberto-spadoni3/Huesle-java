import {
    Box,
    Button,
    Fade,
    styled,
    TableCell,
    TableContainer,
    TableRow,
    TableHead,
    Table,
    TableBody,
    Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import SearchIcon from "@mui/icons-material/Search";
import { BACKEND_GET_MATCHES_ENDPOINT } from "../api/backend_endpoints";
import useAuth from "../hooks/useAuth";
import useGameData from "../hooks/useGameData";
import useSocket from "../hooks/useSocket";
import BottomBar from "./BottomBar";
import useAxiosPrivate from "../hooks/useAxiosPrivate";
import PlayersStatusList from "./PlayersStatusList";

const Dashboard = () => {
    const navigate = useNavigate();
    const axiosPrivate = useAxiosPrivate();
    const { auth } = useAuth();
    const { socket, registerHandler, allPlayersStatus } = useSocket();
    const { GameStates } = useGameData();
    const [loading, setLoading] = useState(false);
    const [activeMatches, setActiveMatches] = useState([]);
    const [completedMatches, setCompletedMatches] = useState([]);
    const [currentOpponentsStatus, setCurrentOpponentsStatus] = useState([]);
    const [opponentsProfilePics, setOpponentsProfilePics] = useState([]);

    const ActiveMatchesCard = styled(Box)(({ theme }) => ({
        width: "100%",
        backgroundColor: theme.palette.background.paper,
        borderRadius: 5,
        marginTop: "3px",
        border: "3px solid",
        borderColor: theme.palette.background.paper,
    }));

    async function updateMatches() {
        try {
            const temp_rows = [];
            const temp_endedrows = [];
            const opponentsSet = new Set();
            const response = await axiosPrivate.get(
                BACKEND_GET_MATCHES_ENDPOINT
            );
            const { pending, matches } = response.data;
            if (pending)
                temp_rows.push(createData(null, "Searching...", "Waiting"));
            matches.sort((a, b) => {
                if (
                    a.matchStatus.matchState === GameStates.PLAYING &&
                    b.matchStatus.matchState === GameStates.PLAYING
                ) {
                    return a.matchStatus.nextPlayer === auth.username ? -1 : 1;
                } else
                    return a.matchStatus.matchState === GameStates.PLAYING
                        ? -1
                        : 1;
            });
            matches.forEach((match) => {
                const opponent = match.matchStatus.players.find(
                    (p) => p.username !== auth.username
                );

                // generate a set of opponents taken from the played and playing matches
                if (!opponentsSet.has(opponent?.username))
                    setOpponentsProfilePics((prevState) => {
                        prevState.push({
                            username: opponent?.username,
                            picId: opponent.profilePictureID,
                        });
                        return prevState;
                    });
                opponentsSet.add(opponent?.username);

                match.matchStatus.matchState === GameStates.PLAYING
                    ? temp_rows.push(
                          createData(
                              match._id,
                              opponent.username,
                              match.matchStatus
                          )
                      )
                    : temp_endedrows.push(
                          createData(
                              match._id,
                              opponent.username,
                              match.matchStatus
                          )
                      );
            });

            const opponentsStatusTemp = [];
            opponentsSet.forEach((username) => {
                const existingStatus = allPlayersStatus.find(
                    (o) => o.username === username
                )?.status;

                opponentsStatusTemp.push({
                    username,
                    status: existingStatus ? existingStatus : "offline",
                });
            });
            console.log(opponentsStatusTemp);
            setCurrentOpponentsStatus(opponentsStatusTemp);
            setActiveMatches(temp_rows);
            setCompletedMatches(temp_endedrows);
        } catch (error) {
            console.log(error);
        }
    }

    useEffect(() => {
        setLoading(true);
        updateMatches().then(() => setLoading(false));
        if (socket) registerHandler((_error, _message) => updateMatches());
        // eslint-disable-next-line
    }, [socket]);

    function createData(id, name, matchStatus) {
        return { id, name, matchStatus };
    }

    function generateRow(index, row_id, row_name, row_status) {
        let button_label;
        let button_type = "outlined";
        let button_bgcolor = "";
        let button_borderColor = "background.border";
        switch (row_status.matchState) {
            case GameStates.PLAYING:
                if (row_status.nextPlayer === auth.username) {
                    button_label = "It's your turn!";
                    button_type = "contained";
                    button_bgcolor = "button.main";
                } else {
                    button_label = "Opponent's turn...";
                }
                button_borderColor = "button.main";
                break;
            case GameStates.WINNER:
                button_label =
                    row_status.nextPlayer === auth.username
                        ? "You won!"
                        : "You Lost...";

                break;
            case GameStates.DRAW:
                button_label = "Draw";
                break;
            default:
                button_label = "Waiting";
                break;
        }
        return (
            <TableRow key={index} onClick={() => openSelectedMatch(row_id)}>
                <TableCell component="th" scope="row" align="center">
                    <Typography color="text.primary" variant="subtitle1">
                        {row_name}
                    </Typography>
                </TableCell>
                <TableCell component="th" scope="row" align="center">
                    <Button
                        variant={button_type}
                        aria-label={button_label}
                        sx={{
                            width: "70%",
                            height: "50%",
                            fontSize: "65%",
                            backgroundColor: button_bgcolor,
                            borderColor: button_borderColor,
                            color: "text.secondary",
                            "&:hover": {
                                borderColor: button_borderColor,
                                backgroundColor: button_bgcolor,
                            },
                        }}
                    >
                        {button_label}
                    </Button>
                </TableCell>
            </TableRow>
        );
    }

    async function openSelectedMatch(matchId) {
        if (!matchId) {
            console.log("Invalid match ID");
            return;
        }

        try {
            localStorage.setItem("matchId", matchId);
            navigate("/match-details");
        } catch (error) {
            console.log(error);
        }
    }

    return (
        <>
            <Fade in={!loading} style={{ transitionDelay: "30ms" }}>
                <Box
                    sx={{
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                    }}
                >
                    <Button
                        sx={{
                            width: "100%",
                            height: "50px",
                            marginTop: 3,
                            marginBottom: 2,
                        }}
                        variant="contained"
                        startIcon={<SearchIcon />}
                        aria-label="Search Match"
                        color="button"
                        onClick={() => navigate("/searchMatch")}
                    >
                        Search Match
                    </Button>

                    <Typography
                        color="text.primary"
                        variant="h6"
                        align="center"
                    >
                        Active Matches
                    </Typography>
                    <ActiveMatchesCard>
                        {activeMatches.length > 0 ? (
                            <TableContainer>
                                <Table
                                    sx={{
                                        minWidth: 250,
                                    }}
                                    aria-label="Active Matches table"
                                >
                                    <TableHead>
                                        <TableRow
                                            sx={{
                                                bgcolor: "background.border",
                                            }}
                                        >
                                            <TableCell size="small">
                                                <Typography
                                                    color="text.primary"
                                                    align="center"
                                                    variant="subtitle1"
                                                >
                                                    Match against
                                                </Typography>
                                            </TableCell>
                                            <TableCell
                                                align="center"
                                                size="small"
                                            >
                                                <Typography
                                                    color="text.primary"
                                                    variant="subtitle1"
                                                >
                                                    Status
                                                </Typography>
                                            </TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {activeMatches.map((row, index) =>
                                            generateRow(
                                                index,
                                                row.id,
                                                row.name,
                                                row.matchStatus
                                            )
                                        )}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        ) : (
                            <Typography variant="h7" marginLeft={1}>
                                No active matches for now
                            </Typography>
                        )}
                    </ActiveMatchesCard>

                    <Box style={{ height: "16px" }}></Box>

                    <Typography
                        color="text.primary"
                        variant="h6"
                        align="center"
                    >
                        Completed Matches
                    </Typography>
                    <ActiveMatchesCard>
                        {completedMatches.length > 0 ? (
                            <TableContainer>
                                <Table
                                    sx={{
                                        minWidth: 250,
                                    }}
                                    aria-label="Completed Matches table"
                                >
                                    <TableHead>
                                        <TableRow
                                            sx={{
                                                bgcolor: "background.border",
                                            }}
                                        >
                                            <TableCell size="small">
                                                <Typography
                                                    color="text.primary"
                                                    align="center"
                                                    variant="subtitle1"
                                                >
                                                    Match against
                                                </Typography>
                                            </TableCell>
                                            <TableCell
                                                align="center"
                                                size="small"
                                            >
                                                <Typography
                                                    color="text.primary"
                                                    variant="subtitle1"
                                                >
                                                    Result
                                                </Typography>
                                            </TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {completedMatches.map((row, index) =>
                                            generateRow(
                                                index,
                                                row.id,
                                                row.name,
                                                row.matchStatus
                                            )
                                        )}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        ) : (
                            <Typography variant="h7" marginLeft={1}>
                                No completed matches for now
                            </Typography>
                        )}
                    </ActiveMatchesCard>

                    <Box style={{ height: "16px" }}></Box>

                    <Typography
                        color="text.primary"
                        variant="h6"
                        align="center"
                    >
                        Opponents status
                    </Typography>
                    {currentOpponentsStatus.length > 0 ? (
                        <PlayersStatusList
                            playersStatusList={currentOpponentsStatus}
                            playersProfilePic={opponentsProfilePics}
                        />
                    ) : (
                        <Typography variant="h7" marginLeft={1}>
                            You don't have any opponents... for now!
                        </Typography>
                    )}

                    <BottomBar />
                </Box>
            </Fade>
        </>
    );
};

export default Dashboard;
