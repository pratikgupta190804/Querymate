import { createContext, useState, useEffect } from "react";
import axios from "../api/axios";

export const UserContext = createContext();

export const UserProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem("token") || null);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // 🔄 Fetch user details on mount or when token changes
  useEffect(() => {
    const fetchUser = async () => {
      if (token) {
        try {
          const res = await axios.get("/users/me", {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          });
          setUser(res.data);
        } catch (err) {
          console.error("Error fetching user:", err);
          logout(); // 🔐 clear token and user on error
        }
      }
      setLoading(false);
    };

    fetchUser();
  }, [token]);

  // 🔐 Logout function
  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem("token");
  };

  return (
    <UserContext.Provider
      value={{
        token,
        setToken,
        user,
        setUser,
        loading,
        logout, // ✅ expose logout
      }}
    >
      {children}
    </UserContext.Provider>
  );
};
