// wrapper for a request using refresh token if jwt access token expired
//
async function secureFetch(url, options) {
    let jwtToken = localStorage.getItem("jwtToken");
    options.headers = {
        ...options.headers,
        Authorization: `${jwtToken}`
    };

    let response = await fetch(url, options);

    debugger
    if (response.status === 401) {
        try {
            const newToken = await refreshAccessToken();
            options.headers.Authorization = `${newToken}`;
            response = await fetch(url, options);
        } catch (err) {
            throw new Error("Token refresh failed");
        }
    }

    return response;
}

// keeps users logged in as long as refresh token is valid.
function refreshAccessToken() {
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) return Promise.reject("No refresh token available");

    debugger
    return fetch("/refresh-token", {
        method: "POST",
        headers: {
            Authorization: `${refreshToken}`,
            "Content-Type": "application/json"
        }
    })
        .then(async res => {
            if (!res.ok) {
                throw new Error("Failed to refresh access token");
            }
            const data = await res.json();
            localStorage.setItem("jwtToken", data.accessToken);
            return data.accessToken;
        });
}
