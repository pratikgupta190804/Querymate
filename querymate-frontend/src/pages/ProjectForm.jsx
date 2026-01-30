import { useState, useEffect } from "react";
import { createProject, updateProject } from "../api/projectService";

const ProjectForm = ({ initialData = null, onSuccess, onCancel }) => {
  const [formData, setFormData] = useState({
    projectName: "",
    description: "",
    dbType: "PostgreSQL",
    connectionType: "local", // "local" or "cloud"
    cloudProvider: "",
    cloudConnectionString: "",
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
      // Clean up data based on connection type
      const submitData = { ...formData };

      if (submitData.connectionType === "cloud") {
        // For cloud connections, set local fields to null
        submitData.dbHost = null;
        submitData.dbPort = null;
        submitData.dbName = null;
      } else {
        // For local connections, set cloud fields to null
        submitData.cloudConnectionString = null;
        submitData.cloudProvider = null;
      }

      if (initialData?.projectId) {
        await updateProject(initialData.projectId, submitData);
      } else {
        await createProject(submitData);
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

          {/* Connection Type Selection */}
          <div>
            <label className="block text-sm font-medium mb-1">
              Connection Type
            </label>
            <select
              name="connectionType"
              value={formData.connectionType}
              onChange={handleChange}
              className="w-full border p-2 rounded"
              required
            >
              <option value="local">Local Database</option>
              <option value="cloud">Cloud Database</option>
            </select>
          </div>

          <select
            name="dbType"
            value={formData.dbType}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            required
          >
            <option value="PostgreSQL">PostgreSQL</option>
            <option value="MySQL">MySQL</option>
            <option value="MongoDB">MongoDB</option>
            <option value="SQLServer">SQL Server</option>
          </select>

          {/* Cloud Database Options */}
          {formData.connectionType === "cloud" && (
            <>
              <div>
                <label className="block text-sm font-medium mb-1">
                  Cloud Provider
                </label>
                <select
                  name="cloudProvider"
                  value={formData.cloudProvider}
                  onChange={handleChange}
                  className="w-full border p-2 rounded"
                  required
                >
                  <option value="">Select Provider</option>
                  <option value="AWS RDS">AWS RDS</option>
                  <option value="Azure SQL">Azure SQL Database</option>
                  <option value="Google Cloud SQL">Google Cloud SQL</option>
                  <option value="MongoDB Atlas">MongoDB Atlas</option>
                  <option value="Azure Cosmos DB">Azure Cosmos DB</option>
                  <option value="Amazon Aurora">Amazon Aurora</option>
                  <option value="Other">Other</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">
                  Connection String
                  <span className="text-xs text-gray-500 ml-2">
                    (Full JDBC URL or connection string)
                  </span>
                </label>
                <textarea
                  name="cloudConnectionString"
                  placeholder="e.g., jdbc:postgresql://your-instance.region.rds.amazonaws.com:5432/dbname"
                  value={formData.cloudConnectionString}
                  onChange={handleChange}
                  className="w-full border p-2 rounded h-20"
                  required
                />
              </div>

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
            </>
          )}

          {/* Local Database Options */}
          {formData.connectionType === "local" && (
            <>
              <input
                name="dbHost"
                placeholder="DB Host (e.g., localhost)"
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
            </>
          )}

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
