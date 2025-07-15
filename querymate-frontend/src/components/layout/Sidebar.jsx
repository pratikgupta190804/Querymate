import { Link, useNavigate } from "react-router-dom";
import { User, Database, MessageSquare, LogOut } from "lucide-react";
import { useContext } from "react";
import { UserContext } from "../../context/UserContext";

const Sidebar = ({ active, setActive }) => {
  const { setToken, setUser } = useContext(UserContext);
  const navigate = useNavigate();

  const menuItems = [
    { label: "Profile", icon: <User size={18} />, key: "profile" },
    { label: "Projects", icon: <Database size={18} />, key: "projects" },
    { label: "Chat", icon: <MessageSquare size={18} />, key: "chat" },
  ];

  const handleLogout = () => {
    localStorage.removeItem("token");
    setToken(null);
    setUser(null);
    navigate("/");
  };

  return (
    <div className="w-64 bg-white border-r h-screen flex flex-col justify-between">
      {/* Top Menu */}
      <div>
        <div className="p-4 text-xl font-bold text-blue-600">QueryMate</div>
        <div className="space-y-1 px-4">
          {menuItems.map((item) => (
            <button
              key={item.key}
              onClick={() => setActive(item.key)}
              className={`flex items-center gap-2 w-full text-left px-3 py-2 rounded-md text-sm hover:bg-blue-100 ${
                active === item.key
                  ? "bg-blue-100 text-blue-700 font-medium"
                  : "text-gray-700"
              }`}
            >
              {item.icon}
              {item.label}
            </button>
          ))}
        </div>
      </div>

      {/* Logout Button */}
      <div className="p-4 border-t">
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 text-sm text-red-600 hover:text-red-800"
        >
          <LogOut size={18} />
          Logout
        </button>
      </div>
    </div>
  );
};

export default Sidebar;
