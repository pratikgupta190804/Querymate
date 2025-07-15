import { useContext, useEffect, useState } from "react";
import { UserContext } from "../context/UserContext";
import axios from "../api/axios";
import { Dialog, DialogTrigger, DialogContent } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

function ProfilePage() {
  const { user, setUser, token } = useContext(UserContext);
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    username: "",
  });

  useEffect(() => {
    if (user) {
      setForm({
        fullName: user.fullName,
        email: user.email,
        username: user.username,
      });
    }
  }, [user]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    try {
      const res = await axios.put(`/api/users/${user.userId}`, form, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setUser(res.data);
      alert("Profile updated successfully");
    } catch (err) {
      console.error("Failed to update profile", err);
    }
  };

  return (
    <div className="max-w-xl mx-auto space-y-4">
      <h2 className="text-2xl font-bold">My Profile</h2>
      <div className="space-y-1">
        <p><strong>Full Name:</strong> {user?.fullName}</p>
        <p><strong>Username:</strong> {user?.username}</p>
        <p><strong>Email:</strong> {user?.email}</p>
        <p><strong>Joined At:</strong> {user?.createdAt?.split("T")[0]}</p>
      </div>

      <Dialog>
        <DialogTrigger asChild>
          <Button className="mt-4">Update Profile</Button>
        </DialogTrigger>
        <DialogContent className="space-y-4">
          <h3 className="text-lg font-semibold">Update Your Profile</h3>
          <input
            className="w-full border p-2 rounded"
            name="fullName"
            value={form.fullName}
            onChange={handleChange}
            placeholder="Full Name"
          />
          <input
            className="w-full border p-2 rounded"
            name="username"
            value={form.username}
            onChange={handleChange}
            placeholder="Username"
          />
          <input
            className="w-full border p-2 rounded"
            name="email"
            value={form.email}
            onChange={handleChange}
            placeholder="Email"
          />
          <Button className="w-full" onClick={handleSave}>Save Changes</Button>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default ProfilePage;
