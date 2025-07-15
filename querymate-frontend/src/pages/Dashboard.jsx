// src/pages/Dashboard.jsx
import { useState, useEffect, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { UserContext } from "../context/UserContext";
import Sidebar from "../components/layout/Sidebar";
import Header from "../components/layout/Header";
import ProfilePage from "./ProfilePage";
import ProjectSection from "./ProjectSection";
import ChatSection from "./ChatSection";
import { fetchProjects } from "../api/projectService"; // 🆕 for dropdown

function Dashboard() {
  const [activeSection, setActiveSection] = useState("profile");
  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState(null);

  const { user, token } = useContext(UserContext);
  const navigate = useNavigate();

  useEffect(() => {
    if (!token) navigate("/login");
  }, [token, navigate]);

  useEffect(() => {
    const loadProjects = async () => {
      try {
        const res = await fetchProjects();
        setProjects(res.data);
      } catch (err) {
        console.error("Failed to load projects for chat", err);
      }
    };

    if (activeSection === "chat") loadProjects();
  }, [activeSection]);

  return (
    <div className="flex h-screen">
      <Sidebar active={activeSection} setActive={setActiveSection} />

      <div className="flex-1 flex flex-col">
        <Header title={activeSection} />

        <main className="p-6 overflow-y-auto flex-1">
          {activeSection === "profile" && <ProfilePage />}
          {activeSection === "projects" && <ProjectSection />}

          {activeSection === "chat" && (
            <div>
              <label className="block mb-2 font-medium">Select a project</label>
              <select
                value={selectedProjectId || ""}
                onChange={(e) => setSelectedProjectId(e.target.value)}
                className="border px-4 py-2 rounded mb-4"
              >
                <option value="" disabled>
                  -- Choose a project --
                </option>
                {projects.map((proj) => (
                  <option key={proj.projectId} value={proj.projectId}>
                    {proj.projectName}
                  </option>
                ))}
              </select>

              {selectedProjectId ? (
                <ChatSection projectId={selectedProjectId} />
              ) : (
                <p className="text-gray-600 text-sm">No project selected.</p>
              )}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

export default Dashboard;
