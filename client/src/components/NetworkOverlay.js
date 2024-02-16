import React, { useState, useEffect } from "react";
import { Backdrop, Box, Stack, Typography } from "@mui/material";
import WifiOffRoundedIcon from "@mui/icons-material/WifiOffRounded";

const NetworkOverlay = () => {
    const [isOffline, setIsOffline] = useState(false);

    useEffect(() => {
        const handleOnline = () => setIsOffline(false);
        const handleOffline = () => setIsOffline(true);

        window.addEventListener("online", handleOnline);
        window.addEventListener("offline", handleOffline);

        return () => {
            window.removeEventListener("online", handleOnline);
            window.removeEventListener("offline", handleOffline);
        };
    }, []);

    return (
        <Backdrop
            sx={{
                color: "text.primary",
                zIndex: (theme) => theme.zIndex.drawer + 1,
            }}
            open={isOffline}
        >
            <Box
                sx={{
                    backgroundColor: "background.overlay",
                    border: "2px solid",
                    borderRadius: "10px",
                    borderColor: "text.secondary",
                }}
            >
                <Stack alignItems={"center"} sx={{ px: "11px", py: "6px" }}>
                    <WifiOffRoundedIcon sx={{ fontSize: 60 }} color="error" />
                    <Typography variant="h5">Connection lost!</Typography>
                    <Typography variant="body1">
                        Wait until the connection is working again...
                    </Typography>
                </Stack>
            </Box>
        </Backdrop>
    );
};

export default NetworkOverlay;
