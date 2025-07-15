import { useState, useEffect } from "react";
import { createProject, updateProject } from "../api/projectService";

const ProjectForm = ({ initialData = null, onSuccess, onCancel }) => {
  const [formData, setFormData] = useState({
    projectName: "",
    description: "",
    dbType: "PostgreSQL",
    dbHost: "",
    dbPort: "",
    dbUsername: "",
    dbPassword: "",
    dbName: "",
  });

  useEffect(() => {
    if (initialData) {
      setFormData(initialData);
    }
  }, [initialData]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (initialData?.projectId) {
        await updateProject(initialData.projectId, formData);
      } else {
        await createProject(formData);
      }
      onSuccess();
    } catch (err) {
      console.error("Error saving project:", err);
    }
  };

  return (
    // BACKDROP
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      {/* MODAL */}
      <div className="bg-white p-6 rounded-lg w-full max-w-lg shadow-lg relative">
        <h2 className="text-xl font-semibold mb-4">
          {initialData?.projectId ? "Update Project" : "Create Project"}
        </h2>

        <form onSubmit={handleSubmit} className="space-y-3">
          <input
            name="projectName"
            placeholder="Project Name"
            value={formData.projectName}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            required
          />
          <input
            name="description"
            placeholder="Description"
            value={formData.description}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            required
          />
          <select
            name="dbType"
            value={formData.dbType}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            required
          >
            <option value="PostgreSQL">PostgreSQL</option>
            <option value="MySQL">MySQL</option>
          </select>
          <input
            name="dbHost"
            placeholder="DB Host"
            value={formData.dbHost}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            required
          />
          <input
            name="dbPort"
            placeholder="DB Port"
            value={formData.dbPort}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            required
          />
          <input
            name="dbUsername"
            placeholder="DB Username"
            value={formData.dbUsername}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            required
          />
          <input
            name="dbPassword"
            type="password"
            placeholder="DB Password"
            value={formData.dbPassword}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            required
          />
          <input
            name="dbName"
            placeholder="DB Name"
            value={formData.dbName}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            required
          />

          <div className="flex justify-end gap-2 pt-4">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
            >
              {initialData?.projectId ? "Update" : "Create"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ProjectForm;
