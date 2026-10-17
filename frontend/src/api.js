import axios from "axios";

const BASE_URL =
    import.meta.env.VITE_API_URL || "http://localhost:8081";

const api = axios.create({
    baseURL: BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

const refreshClient = axios.create({
    baseURL: BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

let refreshPromise = null;

function clearSession() {
    localStorage.removeItem("Token");
    localStorage.removeItem("RefreshToken");
    localStorage.removeItem("UserEmail");
    localStorage.removeItem("UserRole");
    localStorage.removeItem("UserId");
    localStorage.removeItem("FullName");

    window.location.reload();
}

async function refreshAccessToken() {
    const refreshToken = localStorage.getItem("RefreshToken");

    if (!refreshToken) {
        throw new Error("Refresh token is missing.");
    }

    const response = await refreshClient.post("/auth/refresh", {
        refreshToken: refreshToken,
    });

    const newToken = response.data && response.data.token;

    if (!newToken) {
        throw new Error("New access token was not returned.");
    }

    localStorage.setItem("Token", newToken);
    return newToken;
}

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
                config.headers.Authorization = "Bearer " + token;
            }
        }

        return config;
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
    (response) => response,
    async(error) => {
        const originalRequest = error.config;
        const status =
            error.response && error.response.status;

        const url = originalRequest ?
            originalRequest.url || "" :
            "";

        const isAuthRequest =
            url.startsWith("/auth/login") ||
            url.startsWith("/auth/register") ||
            url.startsWith("/auth/refresh");

        if (
            status !== 401 ||
            !originalRequest ||
            originalRequest._retry ||
            isAuthRequest
        ) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        try {
            if (!refreshPromise) {
                refreshPromise = refreshAccessToken().finally(() => {
                    refreshPromise = null;
                });
            }

            const newToken = await refreshPromise;

            originalRequest.headers = originalRequest.headers || {};
            originalRequest.headers.Authorization = "Bearer " + newToken;

            return api(originalRequest);
        } catch (refreshError) {
            clearSession();
            return Promise.reject(refreshError);
        }
    }
);

export default api;