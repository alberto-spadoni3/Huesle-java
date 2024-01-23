import { Outlet } from "react-router-dom";
import { useState, useEffect } from "react";
import useRefreshToken from "../hooks/useRefreshToken";
import useAuth from "../hooks/useAuth";
import Loading from "./Loading";

const PersistLogin = () => {
    const [isLoading, setIsLoading] = useState(true);
    const [isMounted, setIsMounted] = useState(true);
    const refresh = useRefreshToken();
    const { auth, persist } = useAuth();

    useEffect(() => {
        const verifyRefreshToken = async () => {
            try {
                await refresh();
            } catch (err) {
                console.error(err);
            } finally {
                isMounted && setIsLoading(false);
            }
        };

        !auth?.accessToken && persist
            ? verifyRefreshToken()
            : setIsLoading(false);

        return () => setIsMounted(false);
    }, []);

    return <>{!persist ? <Outlet /> : isLoading ? <Loading /> : <Outlet />}</>;
};

export default PersistLogin;
