import {useNavigate} from "react-router-dom";
import axios from "../api/axios";
import useAuth from "./useAuth";
import useSocket from "./useSocket";
import {BACKEND_LOGOUT_ENDPOINT} from "../api/backend_endpoints";

const useLogout = () => {
    const navigate = useNavigate();
    const { setAuth } = useAuth();
    const { closeSocket } = useSocket();

    return async () => {
        try {
            await axios.get(BACKEND_LOGOUT_ENDPOINT, {
                withCredentials: true,
            });
            closeSocket();
            setAuth({});
            navigate("/");
        } catch (error) {
            console.error(error);
        }
    };
};

export default useLogout;
