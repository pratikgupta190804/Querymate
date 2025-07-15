import { useEffect, useState } from "react";
import { fetchProjects, deleteProject } from "../api/projectService";
import ProjectForm from "./ProjectForm";

const ProjectSection = () => {
  const [projects, setProjects] = useState([]);
  const [editingProject, setEditingProject] = useState(null);

  const loadProjects = async () => {
    try {
      const res = await fetchProjects();
      setProjects(res.data);
    } catch (err) {
      console.error("Failed to fetch projects", err);
    }
  };

  const handleDelete = async (projectId) => {
    if (window.confirm("Delete this project?")) {
      await deleteProject(projectId);
      loadProjects();
    }
  };

  const handleEdit = (project) => {
    setEditingProject(project);
  };

  const handleAdd = () => {
    setEditingProject({});
  };

  const handleCloseForm = () => {
    setEditingProject(null);
  };

  useEffect(() => {
    loadProjects();
  }, []);

  return (
    <div className="relative">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold">Your Projects</h2>
        <button
          onClick={handleAdd}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Add Project
        </button>
      </div>

      {/* Form Modal Popup */}
      {editingProject && (
        <ProjectForm
          initialData={
            Object.keys(editingProject).length ? editingProject : null
          }
          onSuccess={() => {
            setEditingProject(null);
            loadProjects();
          }}
          onCancel={handleCloseForm}
        />
      )}

      {/* Projects List */}
      <div className="grid gap-4 mt-4">
        {projects.map((project) => (
          <div key={project.projectId} className="border p-4 rounded shadow">
            <h3 className="text-lg font-medium">{project.projectName}</h3>
            <p className="text-sm text-gray-600">{project.description}</p>
            <p className="text-xs mt-1">
              DB: {project.dbType} ({project.dbHost}:{project.dbPort})
            </p>
            <div className="mt-2 flex gap-2">
              <button
                className="text-sm text-blue-600 hover:underline"
                onClick={() => handleEdit(project)}
              >
                Edit
              </button>
              <button
                className="text-sm text-red-600 hover:underline"
                onClick={() => handleDelete(project.projectId)}
              >
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ProjectSection;
