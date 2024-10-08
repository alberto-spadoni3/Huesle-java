import Menu from "@mui/material/Menu";
import MenuItem from "@mui/material/MenuItem";
import ListItemIcon from "@mui/material/ListItemIcon";
import Divider from "@mui/material/Divider";
import Typography from "@mui/material/Typography";
import QuestionMarkIcon from "@mui/icons-material/QuestionMark";
import Settings from "@mui/icons-material/Settings";
import Logout from "@mui/icons-material/Logout";
import useLogout from "../hooks/useLogout";
import { useNavigate } from "react-router-dom";
import PropTypes from "prop-types";
import useSocket from "../hooks/useSocket";

function StyledBadge(_props) {
    return null;
}

StyledBadge.propTypes = {
    overlap: PropTypes.string,
    variant: PropTypes.string,
    anchorOrigin: PropTypes.shape({
        horizontal: PropTypes.string,
        vertical: PropTypes.string,
    }),
};

export default function DashboardMenu({ anchorEl, setAnchorEl, open }) {
    const logout = useLogout();
    const navigate = useNavigate();
    const { lostConnection } = useSocket();

    const handleMenuClose = () => {
        setAnchorEl(null);
    };

    return (
        <Menu
            anchorEl={anchorEl}
            id="dashboard-menu"
            open={open}
            onClose={handleMenuClose}
            onClick={handleMenuClose}
            slotProps={{
                paper: {
                    elevation: 0,
                    sx: {
                        overflow: "visible",
                        filter: "drop-shadow(0px 2px 8px rgba(0,0,0,0.32))",
                        mt: "3px",
                        bgcolor: "background.dashboardMenu",
                        "& .MuiAvatar-root": {
                            width: 64,
                            height: 64,
                            ml: -0.5,
                            mr: 1,
                        },
                    },
                },
            }}
            anchorOrigin={{
                vertical: "top",
                horizontal: "center",
            }}
            transformOrigin={{
                vertical: "bottom",
                horizontal: "center",
            }}
        >
            <MenuItem onClick={(_e) => navigate("/rules")}>
                <ListItemIcon>
                    <QuestionMarkIcon fontSize="small" />
                </ListItemIcon>
                How To Play
            </MenuItem>
            <MenuItem onClick={(_e) => navigate("/settings")}>
                <ListItemIcon>
                    <Settings fontSize="small" />
                </ListItemIcon>
                Settings
            </MenuItem>
            <Divider />
            <MenuItem disabled={lostConnection} onClick={logout}>
                <ListItemIcon>
                    <Logout color="error" fontSize="small" />
                </ListItemIcon>
                <Typography variant="body1" color="error">
                    Logout
                </Typography>
            </MenuItem>
        </Menu>
    );
}
