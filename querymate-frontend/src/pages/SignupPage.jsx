import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

function SignupPage() {
  const [fullName, setFullName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();
  const [error, setError] = useState("");

  const handleSignup = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const res = await api.post("/auth/register", {
        fullName,
        username,
        email,
        password,
      });
      console.log("Signup successful:", res.data);
      navigate("/"); // Redirect to login
    } catch (err) {
      setError("Signup failed. Please try again.");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <form onSubmit={handleSignup} className="bg-white p-8 rounded-lg shadow-md w-96">
        <h2 className="text-2xl font-bold mb-6 text-center text-blue-600">Signup</h2>
        {error && <p className="text-red-600 text-sm mb-2">{error}</p>}
        <input className="w-full border p-2 mb-3" placeholder="Full Name" value={fullName} onChange={(e) => setFullName(e.target.value)} />
        <input className="w-full border p-2 mb-3" placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} />
        <input className="w-full border p-2 mb-3" placeholder="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <input className="w-full border p-2 mb-3" placeholder="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <button className="bg-blue-600 text-white w-full py-2 rounded hover:bg-blue-700">Signup</button>
      </form>
    </div>
  );
}

export default SignupPage;
