import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import ListItemText from "@mui/material/ListItemText";
import ListItemAvatar from "@mui/material/ListItemAvatar";
import UserPicture from "./UserPicture";
import useSocket from "../hooks/useSocket";
import { useState, useEffect } from "react";

const PlayersStatusList = ({ playersStatusList, playersProfilePic }) => {
    const [opponentsStatus, setOpponentsStatus] = useState(playersStatusList);
    const { PlayerStatus, allPlayersStatus } = useSocket();

    const extractStatus = (username) => {
        return opponentsStatus.find((p) => p.username === username)?.status;
    };

    const getPlayerWithStatus = (username) => {
        const onlineEmoji = " 🟢";
        const playingEmoji = " 🎮";
        const offlineEmoji = " 🔴";

        if (extractStatus(username) === PlayerStatus.ONLINE)
            return username + onlineEmoji;
        else if (extractStatus(username) === PlayerStatus.PLAYING)
            return username + playingEmoji;
        else return username + offlineEmoji;
    };

    useEffect(() => {
        if (allPlayersStatus.length > 0) {
            allPlayersStatus.forEach(({ username, status }) => {
                const possibleOpponent = playersStatusList.find(
                    (opponent) => opponent.username === username
                );
                if (possibleOpponent) possibleOpponent.status = status;
            });
            setOpponentsStatus([...playersStatusList]);
        }
        // eslint-disable-next-line
    }, [allPlayersStatus]);

    // useEffect(() => {
    //     setOpponentsStatus([...playersStatusList]);
    // }, [playersStatusList]);

    return (
        <List
            dense
            sx={{ width: "50%", maxWidth: 360, bgcolor: "background.paper" }}
        >
            {opponentsStatus.map((value) => {
                const labelId = `list-label-${value?.username}`;
                return (
                    <ListItem key={value?.username}>
                        <ListItemAvatar>
                            <UserPicture
                                size={32}
                                userPic={
                                    playersProfilePic.find(
                                        (p) => p.username === value.username
                                    )?.picId
                                }
                            />
                        </ListItemAvatar>
                        <ListItemText
                            id={labelId}
                            primary={getPlayerWithStatus(value?.username)}
                        />
                    </ListItem>
                );
            })}
        </List>
    );
};

export default PlayersStatusList;
