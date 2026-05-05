import { useEffect, useState } from "react";
import { getAuthSession, normalizeRoles, updateStoredSession } from "../api/authApi";
import { getCurrentUser } from "../api/userApi";
import AdminLayout from "./AdminLayout";
import UserLayout from "./UserLayout";

const adminRoles = ["ADMIN", "LIBRARIAN"];

function AppShell({ children }) {
  const session = getAuthSession();
  const [roles, setRoles] = useState(normalizeRoles(session?.roles));
  const canUseAdminLayout = roles.some((role) => adminRoles.includes(role));

  useEffect(() => {
    if (roles.length || !session?.token) return;

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
      });

    return () => {
      ignore = true;
    };
  }, [roles.length, session?.token]);

  if (canUseAdminLayout) {
    return <AdminLayout>{children}</AdminLayout>;
  }

  return <UserLayout>{children}</UserLayout>;
}

export default AppShell;
