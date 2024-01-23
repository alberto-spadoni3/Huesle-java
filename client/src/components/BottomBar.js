import { Box, Stack, Typography, IconButton } from "@mui/material";
import { useState } from "react";
import MenuRoundedIcon from "@mui/icons-material/MenuRounded";
import DashboardMenu from "./DashboardMenu";
import useAuth from "../hooks/useAuth";
import UserPicture from "./UserPicture";
import { useNavigate } from "react-router-dom";

const BottomBar = () => {
    const [anchorElement, setAnchorElement] = useState(null);
    const { auth } = useAuth();
    const open = Boolean(anchorElement);
    const navigate = useNavigate();

    const handleMenuOpening = (event) => {
        setAnchorElement(event.currentTarget);
    };

    return (
        <>
            <Box
                width="100%"
                sx={{
                    display: "flex",
                    justifyContent: "center",
                    flexDirection: "column",
                    alignItems: "center",
                    marginTop: "40px",
                    marginBottom: "25px",
                }}
            >
                <Stack
                    direction="row"
                    justifyContent="space-around"
                    width="80%"
                    spacing={3}
                >
                    <IconButton
                        onClick={(e) => navigate("/user/profile")}
                        aria-label="Open menu"
                        sx={{ padding: 0 }}
                    >
                        <Stack align={"column"} alignItems={"center"}>
                            <UserPicture
                                size="50px"
                                userPic={auth.profilePicID}
                            />

                            <Typography
                                color="text.primary"
                                fontSize={"14px"}
                                mt={0.3}
                                width={"inherit"}
                            >
                                {auth.username}'s Profile
                            </Typography>
                        </Stack>
                    </IconButton>

                    <IconButton
                        onClick={(e) => handleMenuOpening(e)}
                        aria-label="Open menu"
                        sx={{ padding: 0 }}
                    >
                        <Stack align={"column"} alignItems={"center"}>
                            <MenuRoundedIcon
                                sx={{
                                    fontSize: 50,
                                    border: "3px solid",
                                    borderColor: "palette.text.secondary",
                                    borderRadius: "50%",
                                    padding: "3px",
                                }}
                            />
                            <Typography
                                color="text.primary"
                                fontSize={"14px"}
                                mt={0.3}
                            >
                                Main Menu
                            </Typography>
                        </Stack>
                    </IconButton>
                </Stack>
                <DashboardMenu
                    anchorEl={anchorElement}
                    setAnchorEl={setAnchorElement}
                    open={open}
                />
            </Box>
        </>
    );
};

export default BottomBar;
