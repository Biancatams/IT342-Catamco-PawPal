import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/Navbar.css";
import "../styles/Auth.css";

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: "", email: "", password: "",
    confirmPassword: "", role: "ADOPTER",
  });
  const [showPass, setShowPass] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError("");
  };

  const handleSubmit = async () => {
    if (!form.fullName || !form.email || !form.password || !form.confirmPassword) {
      setError("Please fill in all fields."); return;
    }
    if (form.password.length < 8) {
      setError("Password must be at least 8 characters."); return;
    }
    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match."); return;
    }
    setLoading(true);
    try {
      const res = await axios.post("http://localhost:8080/api/v1/auth/register", form);
      if (res.data.success) {
        localStorage.setItem("token", res.data.data.accessToken);
        localStorage.setItem("user", JSON.stringify(res.data.data.user));
        navigate("/home");
      } else {
        setError(res.data.error?.message || "Registration failed.");
      }
    } catch (err) {
      setError(err.response?.data?.error?.message || "Something went wrong.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <nav className="navbar">
        <button className="navbar-brand" onClick={() => navigate("/")}>
          <span className="navbar-logo">🐾</span>
          <span>PawPal</span>
        </button>
        <div className="navbar-links">
          <button className="navbar-link" onClick={() => navigate("/login")}>Login</button>
          <button className="navbar-btn-primary" onClick={() => navigate("/register")}>Sign Up</button>
        </div>
      </nav>

      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-header">
            <div className="auth-icon">🐾</div>
            <h1 className="auth-title">Create your account</h1>
            <p className="auth-subtitle">Join PawPal and start your pet adoption journey.</p>
          </div>

          {error && <div className="auth-error">{error}</div>}

          <div className="auth-form">
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input className="form-input" name="fullName"
                value={form.fullName} onChange={handleChange}
                placeholder="Enter your full name" />
            </div>

            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input className="form-input" name="email" type="email"
                value={form.email} onChange={handleChange}
                placeholder="Enter your email" />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <div className="input-wrapper">
                <input className="form-input" name="password"
                  type={showPass ? "text" : "password"}
                  value={form.password} onChange={handleChange}
                  placeholder="Create a password" />
                <button className="toggle-password" type="button"
                  onClick={() => setShowPass(!showPass)}>
                  {showPass ? "🙈" : "👁️"}
                </button>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Confirm Password</label>
              <div className="input-wrapper">
                <input className="form-input" name="confirmPassword"
                  type={showConfirm ? "text" : "password"}
                  value={form.confirmPassword} onChange={handleChange}
                  placeholder="Confirm your password" />
                <button className="toggle-password" type="button"
                  onClick={() => setShowConfirm(!showConfirm)}>
                  {showConfirm ? "🙈" : "👁️"}
                </button>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">I am here to...</label>
              <div className="role-buttons">
                <button type="button"
                  className={`role-btn ${form.role === "ADOPTER" ? "role-btn-active" : "role-btn-inactive"}`}
                  onClick={() => setForm({ ...form, role: "ADOPTER" })}>
                  🐾 I want to Adopt
                </button>
                <button type="button"
                  className={`role-btn ${form.role === "PET_OWNER" ? "role-btn-active" : "role-btn-inactive"}`}
                  onClick={() => setForm({ ...form, role: "PET_OWNER" })}>
                  🏠 I'm rehoming a Pet
                </button>
              </div>
            </div>

            <button className="submit-btn" onClick={handleSubmit} disabled={loading}>
              {loading ? "Creating account..." : "Create Account"}
            </button>

            <p className="auth-footer-text">
              Already have an account?{" "}
              <button className="auth-link" onClick={() => navigate("/login")}>Log in</button>
            </p>
          </div>

          <div className="auth-illustration">🐱 🐶</div>
        </div>
      </div>
    </div>
  );
}