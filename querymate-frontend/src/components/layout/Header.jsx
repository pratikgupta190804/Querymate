// src/components/layout/Header.jsx
import { Plus } from "lucide-react";

const Header = ({ title }) => {
  return (
    <div className="h-16 flex items-center justify-between px-6 border-b bg-white shadow-sm">
      <h1 className="text-lg font-semibold capitalize">{title}</h1>
      <button className="flex items-center gap-2 text-sm text-blue-600 border border-blue-600 px-3 py-1 rounded hover:bg-blue-50">
        <Plus size={16} />
        Create Project
      </button>
    </div>
  );
};

export default Header;
