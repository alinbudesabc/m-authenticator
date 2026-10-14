
function buildCommonHeaders(xhr, extraHeaders = {}) {
    const headers = {
        'User-Agent': navigator.userAgent || "Unknown-UA",
        'X-Screen-Resolution': `${window.screen.width}x${window.screen.height}` || "Unknown-SR",
        'X-Timezone': Intl.DateTimeFormat().resolvedOptions().timeZone || "Unknown-TZ",
        ...extraHeaders
    };

    //in case the type of request is ajax call/XMLHttpRequest we set headers differently
    if (xhr && typeof xhr.setRequestHeader === "function") {
        for (const key in headers) {
            xhr.setRequestHeader(key, headers[key]);
        }
    }

    return headers;
}


// function buildCommonHeaders(xhr) {
//
//     let userAgent = "Unknown-UA";
//     try {
//         userAgent = navigator.userAgent || "Unknown-UA";
//     } catch (e) {
//         console.warn("User-Agent not available", e);
//     }
//     const screenResolution = `${window.screen.width}x${window.screen.height}`;
//     const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
//
//     xhr.setRequestHeader('User-Agent', userAgent);
//     xhr.setRequestHeader('X-Screen-Resolution', screenResolution);
//     xhr.setRequestHeader('X-Timezone', timezone);
// }
