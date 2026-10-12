import axios from "axios";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || "http://localhost:8081",
    headers: {
        "Content-Type": "application/json",
    },
});

api.interceptors.request.use(
    (config) => {
        const url = config.url || "";
        const needsToken =
            url.startsWith("/submissions") ||
            url.startsWith("/users") ||
            url === "/progress/my";

        if (needsToken) {
            const token = localStorage.getItem("Token");

            if (token) {
                config.headers = config.headers || {};
                config.headers.Authorization = `Bearer ${token}`;
            }
        }

        return config;
    },
    (error) => Promise.reject(error)
);

export default api;