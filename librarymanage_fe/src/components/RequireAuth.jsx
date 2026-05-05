import { useEffect, useState } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { getAuthSession, normalizeRoles, updateStoredSession } from "../api/authApi";
import { getCurrentUser } from "../api/userApi";

const RequireAuth = ({ children, allowedRoles }) => {
  const location = useLocation();
  const session = getAuthSession();
  const normalizedAllowedRoles = normalizeRoles(allowedRoles);
  const [roles, setRoles] = useState(normalizeRoles(session?.roles));
  const [loadingRoles, setLoadingRoles] = useState(
    Boolean(session?.token && normalizedAllowedRoles.length && !session?.roles?.length),
  );

  useEffect(() => {
    if (!session?.token || !normalizedAllowedRoles.length || session?.roles?.length) return;

    let ignore = false;

    getCurrentUser()
      .then((res) => {
        if (ignore) return;

        const currentRoles = normalizeRoles(res.data.roles);
        updateStoredSession({
          userId: res.data.userId,
          email: res.data.email,
          username: res.data.username,
          roles: currentRoles,
        });
        setRoles(currentRoles);
      })
      .catch((err) => {
        if (ignore) return;
        console.error(err);
        setRoles([]);
      })
      .finally(() => {
        if (!ignore) {
          setLoadingRoles(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, [normalizedAllowedRoles.length, session?.roles, session?.token]);

  if (!session?.token) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (loadingRoles) {
    return null;
  }

  if (normalizedAllowedRoles.length) {
    const currentRoles = normalizeRoles(roles?.length ? roles : session?.roles);
    const hasPermission = currentRoles.some((role) => normalizedAllowedRoles.includes(role));

    if (!hasPermission) {
      return <Navigate to="/" replace />;
    }
  }

  return children;
};

export default RequireAuth;
