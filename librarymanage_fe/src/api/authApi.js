import axios from "axios";

const API_URL = "http://localhost:8080/api/auth";
const AUTH_STORAGE_KEY = "library_auth";

export const normalizeRoles = (roles) => {
  if (!Array.isArray(roles)) return [];

  return roles
    .map((role) => {
      if (typeof role === "string") return role;
      if (role && typeof role === "object") {
        return role.name || role.roleName || role.authority || role.code || "";
      }
      return "";
    })
    .map((role) => String(role).trim().toUpperCase().replace(/^ROLE_/, ""))
    .filter(Boolean);
};

export const setAuthToken = (token, tokenType = "Bearer") => {
  if (token) {
    axios.defaults.headers.common.Authorization = `${tokenType} ${token}`;
  } else {
    delete axios.defaults.headers.common.Authorization;
  }
};

export const getAuthSession = () => {
  const rawSession = localStorage.getItem(AUTH_STORAGE_KEY);
  if (!rawSession) return null;

  try {
    return JSON.parse(rawSession);
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    return null;
  }
};

export const saveAuthSession = (session) => {
  const normalizedSession = {
    ...session,
    roles: normalizeRoles(session?.roles),
  };
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(normalizedSession));
  setAuthToken(normalizedSession.token, normalizedSession.tokenType);
};

export const updateStoredSession = (patch) => {
  const currentSession = getAuthSession() || {};
  saveAuthSession({
    ...currentSession,
    ...patch,
  });
};

export const initializeAuth = () => {
  const session = getAuthSession();
  if (session?.token) {
    setAuthToken(session.token, session.tokenType);
  }
};

export const clearAuthSession = () => {
  localStorage.removeItem(AUTH_STORAGE_KEY);
  setAuthToken(null);
};

export const login = (data) => axios.post(`${API_URL}/login`, data);

export const register = (data) => axios.post(`${API_URL}/register`, data);
