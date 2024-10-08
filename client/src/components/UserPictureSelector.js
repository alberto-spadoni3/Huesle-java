import ArrowLeftRoundedIcon from "@mui/icons-material/ArrowLeftRounded";
import ArrowRightRoundedIcon from "@mui/icons-material/ArrowRightRounded";
import { Stack, IconButton, Skeleton, Button } from "@mui/material";
import { useState } from "react";
import useAxiosPrivate from "../hooks/useAxiosPrivate";
import { BACKEND_UPDATE_USER_PIC_ENDPOINT } from "../api/backend_endpoints";
import { useSnackbar } from "notistack";
import UserPicture from "./UserPicture";
import useAuth from "../hooks/useAuth";
import useSocket from "../hooks/useSocket";

const USER_PICS_NUMBER = 10;

const UserPictureSelector = ({ size }) => {
    const { enqueueSnackbar } = useSnackbar();
    const axiosPrivate = useAxiosPrivate();
    const { auth, setAuth } = useAuth();
    const { lostConnection } = useSocket();

    const [picSelector, setPicSelector] = useState(
        auth.profilePicID ? auth.profilePicID : 0
    );

    const [currentUserPic, setCurrentUserPic] = useState(auth.profilePicID);

    const changePic = (up = false) => {
        let newPicID = up ? picSelector + 1 : picSelector - 1;
        if (newPicID < 0 || newPicID >= USER_PICS_NUMBER) return;
        setPicSelector(newPicID);
    };

    const updateUserPicture = async () => {
        try {
            const response = await axiosPrivate.put(
                BACKEND_UPDATE_USER_PIC_ENDPOINT,
                JSON.stringify({ profilePicID: picSelector })
            );

            if (response.status === 200) {
                setCurrentUserPic(picSelector);
                enqueueSnackbar(response.data?.resultMessage, {
                    variant: "success",
                    autoHideDuration: 2500,
                });
                setAuth({ ...auth, profilePicID: picSelector });
            }
        } catch (error) {
            console.log(error);
        }
    };

    return (
        <>
            <Stack direction="row" alignItems="center">
                {picSelector !== -1 ? (
                    <>
                        <IconButton
                            aria-label="select left image"
                            disabled={picSelector === 0}
                            onClick={() => changePic()}
                        >
                            <ArrowLeftRoundedIcon fontSize="large" />
                        </IconButton>
                        <UserPicture size={200} userPic={picSelector} />
                        <IconButton
                            aria-label="select right image"
                            disabled={picSelector === USER_PICS_NUMBER - 1}
                            onClick={() => changePic(true)}
                        >
                            <ArrowRightRoundedIcon fontSize="large" />
                        </IconButton>
                    </>
                ) : (
                    <>
                        <Skeleton
                            variant="circular"
                            width={12}
                            height={12}
                            sx={{ margin: 3 }}
                        />
                        <Skeleton
                            variant="circular"
                            width={size}
                            height={size}
                        />
                        <Skeleton
                            variant="circular"
                            width={12}
                            height={12}
                            sx={{ margin: 3 }}
                        />
                    </>
                )}
            </Stack>
            <Button
                variant="outlined"
                disabled={currentUserPic === picSelector || lostConnection}
                sx={{
                    mt: 1.5,
                    mb: 1.5,
                    borderColor: "button.main",
                    color: "text.secondary",
                }}
                onClick={updateUserPicture}
            >
                Update picture
            </Button>
        </>
    );
};

export default UserPictureSelector;
