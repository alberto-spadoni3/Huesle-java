import axios from "axios";

const BACKEND_HOST = process.env.BACKEND_HOST || "localhost";
const BASE_URL = "http://" + BACKEND_HOST + ":8080/api";

export default axios.create({
    baseURL: BASE_URL,
});

const axiosPrivate = axios.create({
    baseURL: BASE_URL,
    withCredentials: true,
});

axiosPrivate.defaults.headers.get["Content-Type"] = "application/json";
axiosPrivate.defaults.headers.put["Content-Type"] = "application/json";
axiosPrivate.defaults.headers.post["Content-Type"] = "application/json";

export { axiosPrivate };
