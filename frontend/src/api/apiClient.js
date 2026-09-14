const BASE_URL = "/api";

/**
 * Fetch wrapper to attach the JWT, parse JSON, and throw errors
 */
async function request(path, { method = "GET", token, body } = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (response.status === 401) {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
  localStorage.removeItem("isAdmin");
  localStorage.setItem("sessionExpired", "true");
  window.location.href = "/login";
  throw new Error("Your session expired. Please log in again."); 
  //if user's token is expired, stop here, do not pass go, go directly to login
}

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request to ${path} failed with status ${response.status}`);
  }

  if (response.status === 204) {
    return null;
  }
  
  return response.json();
}

export const api = {
  get: (path, token) => request(path, { token }),
  post: (path, token, body) => request(path, { method: "POST", token, body }),
  put: (path, token, body) => request(path, { method: "PUT", token, body }),
  patch: (path, token, body) => request(path, { method: "PATCH", token, body }),
  del: (path, token) => request(path, { method: "DELETE", token }),
};
