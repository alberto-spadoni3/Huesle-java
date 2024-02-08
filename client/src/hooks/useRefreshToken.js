import axios from "../api/axios";
import useAuth from "./useAuth";
import useSocket from "./useSocket";
import { BACKEND_REFRESH_TOKEN_ENDPOINT } from "../api/backend_endpoints";

const useRefreshToken = () => {
    const { setAuth } = useAuth();
    const { closeSocket } = useSocket();

    return async () => {
        // Before setting a new auth object, we close the current socket
        closeSocket();

        const response = await axios.get(BACKEND_REFRESH_TOKEN_ENDPOINT, {
            withCredentials: true,
        });

        await setAuth({
            username: response.data?.username,
            accessToken: response.data?.newAccessToken,
            profilePicID: response.data?.profilePicID,
            email: response.data?.email,
        });
        return response.data.newAccessToken;
    };
};

export default useRefreshToken;
