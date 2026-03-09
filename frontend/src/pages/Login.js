import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/Navbar.css";
import "../styles/Auth.css";

export default function Login() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [showPass, setShowPass] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError("");
  };

  const handleSubmit = async () => {
    if (!form.email || !form.password) {
      setError("Please enter your email and password."); return;
    }
    setLoading(true);
    try {
      const res = await axios.post("http://localhost:8080/api/v1/auth/login", form);
      if (res.data.success) {
        localStorage.setItem("token", res.data.data.accessToken);
        localStorage.setItem("user", JSON.stringify(res.data.data.user));
        navigate("/home");
      } else {
        setError(res.data.error?.message || "Login failed.");
      }
    } catch (err) {
      setError(err.response?.data?.error?.message || "Invalid email or password.");
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
          <button className="navbar-btn-primary" onClick={() => navigate("/login")}>Login</button>
          <button className="navbar-btn-outline" onClick={() => navigate("/register")}>Sign Up</button>
        </div>
      </nav>

      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-header">
            <div className="auth-icon">🐾</div>
            <h1 className="auth-title">Welcome back</h1>
            <p className="auth-subtitle">Sign in to continue your adoption journey.</p>
          </div>

          {error && <div className="auth-error">{error}</div>}

          <div className="auth-form">
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input className="form-input" name="email" type="email"
                value={form.email} onChange={handleChange}
                placeholder="Enter your email" />
            </div>

            <div className="form-group">
              <div className="form-label-row">
                <label className="form-label">Password</label>
                <button className="forgot-link" type="button">Forgot password?</button>
              </div>
              <div className="input-wrapper">
                <input className="form-input" name="password"
                  type={showPass ? "text" : "password"}
                  value={form.password} onChange={handleChange}
                  onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
                  placeholder="Enter your password" />
                <button className="toggle-password" type="button"
                  onClick={() => setShowPass(!showPass)}>
                  {showPass ? "🙈" : "👁️"}
                </button>
              </div>
            </div>

            <button className="submit-btn" onClick={handleSubmit} disabled={loading}>
              {loading ? "Signing in..." : "Log In"}
            </button>

            <p className="auth-footer-text">
              New to PawPal?{" "}
              <button className="auth-link" onClick={() => navigate("/register")}>Sign Up</button>
            </p>
          </div>

          <div className="auth-illustration">🐱 🐶</div>
        </div>
      </div>
    </div>
  );
}