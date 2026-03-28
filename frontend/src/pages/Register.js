import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import axios from "axios";
import { useGoogleLogin } from "@react-oauth/google";
import pawLogo from "../pawlogo.png";
import loginDog from "../loginregister dog.png";
import "../styles/Navbar.css";
import "../styles/Auth.css";

export default function Register() {
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    password: "",
    confirmPassword: "",
    role: "ADOPTER",
  });
  const [showPass, setShowPass] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError("");
  };

  const handleNavigateByRole = (user) => {
    if (user.role === "PET_OWNER") {
      navigate("/owner/dashboard");
    } else if (user.role === "ADOPTER") {
      navigate("/adopter/dashboard");
    } else {
      navigate("/home");
    }
  };

  const handleSubmit = async () => {
    if (!form.fullName || !form.email || !form.password || !form.confirmPassword) {
      setError("Please fill in all fields.");
      return;
    }
    if (form.password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }
    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    setLoading(true);
    try {
      const res = await axios.post("http://localhost:8080/api/v1/auth/register", form);
      if (res.data.success) {
        localStorage.setItem("token", res.data.data.accessToken);
        localStorage.setItem("user", JSON.stringify(res.data.data.user));
        handleNavigateByRole(res.data.data.user);
      } else {
        setError(res.data.error?.message || "Registration failed.");
      }
    } catch (err) {
      setError(err.response?.data?.error?.message || "Something went wrong.");
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleRegister = useGoogleLogin({
    onSuccess: async (tokenResponse) => {
      if (!form.role) {
        setError("Please select a role before continuing with Google.");
        return;
      }
      try {
        const res = await axios.post(
          "http://localhost:8080/api/v1/auth/google-register",
          { token: tokenResponse.access_token, role: form.role }
        );
        if (res.data.success) {
          localStorage.setItem("token", res.data.data.accessToken);
          localStorage.setItem("user", JSON.stringify(res.data.data.user));
          handleNavigateByRole(res.data.data.user);
        } else {
          setError(res.data.error?.message || "Google registration failed.");
        }
      } catch (err) {
        console.log("FULL ERROR:", err);
        console.log("RESPONSE:", err.response);
        console.log("RESPONSE DATA:", err.response?.data);
        setError(
          err.response?.data?.error?.message ||
          "Something went wrong with Google registration."
        );
      }
    },
    onError: () => setError("Google sign-up failed. Please try again."),
  });

  return (
    <div className="auth-page">
      <nav className="navbar">
        <button className="navbar-brand" onClick={() => navigate("/")}>
          <img src={pawLogo} alt="PawPal logo" className="navbar-logo-img" />
          <span className="navbar-brand-text">PawPal</span>
        </button>
        <div className="navbar-links">
          <button
            className={`navbar-link${location.pathname === "/" ? " active" : ""}`}
            onClick={() => navigate("/")}
          >
            Home
          </button>
          <button
            className={`navbar-btn-outline${location.pathname === "/login" ? " active" : ""}`}
            onClick={() => navigate("/login")}
          >
            Login
          </button>
          <button
            className={`navbar-btn-primary${location.pathname === "/register" ? " active" : ""}`}
            onClick={() => navigate("/register")}
          >
            Sign Up
          </button>
        </div>
      </nav>

      <div className="auth-container">
        <div className="auth-split">

          {/* Left panel */}
          <div className="auth-panel-left">
            <div className="auth-panel-eyebrow">Join PawPal</div>
            <h2 className="auth-panel-title">Find or give a<br /><em>forever home.</em></h2>
            <p className="auth-panel-body">
              Whether you're looking to adopt a pet or find a loving home for one,
              PawPal makes the process simple, safe, and free.
            </p>
            <div className="auth-panel-tags">
              <span className="auth-tag">Free to Use</span>
              <span className="auth-tag">Admin Verified</span>
              <span className="auth-tag">Email Notifications</span>
            </div>
          </div>

          {/* Right card */}
          <div className="auth-card">
            <div className="auth-card-header">
              <h1 className="auth-card-title">Create Account</h1>
              <p className="auth-card-sub">Join the PawPal community today.</p>
            </div>

            {error && <div className="auth-error">{error}</div>}

            <div className="auth-form">
              <div className="form-group">
                <label className="form-label">Full Name</label>
                <input
                  className="form-input"
                  name="fullName"
                  value={form.fullName}
                  onChange={handleChange}
                  placeholder="Your full name"
                />
              </div>

              <div className="form-group">
                <label className="form-label">Email Address</label>
                <input
                  className="form-input"
                  name="email"
                  type="email"
                  value={form.email}
                  onChange={handleChange}
                  placeholder="you@example.com"
                />
              </div>

              <div className="form-group">
                <label className="form-label">Password</label>
                <div className="input-wrapper">
                  <input
                    className="form-input"
                    name="password"
                    type={showPass ? "text" : "password"}
                    value={form.password}
                    onChange={handleChange}
                    placeholder="Min. 8 characters"
                  />
                  <button className="toggle-password" type="button" onClick={() => setShowPass(!showPass)}>
                    {showPass ? (
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                        <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                        <line x1="1" y1="1" x2="23" y2="23"/>
                      </svg>
                    ) : (
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                        <circle cx="12" cy="12" r="3"/>
                      </svg>
                    )}
                  </button>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Confirm Password</label>
                <div className="input-wrapper">
                  <input
                    className="form-input"
                    name="confirmPassword"
                    type={showConfirm ? "text" : "password"}
                    value={form.confirmPassword}
                    onChange={handleChange}
                    placeholder="Repeat your password"
                  />
                  <button className="toggle-password" type="button" onClick={() => setShowConfirm(!showConfirm)}>
                    {showConfirm ? (
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                        <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                        <line x1="1" y1="1" x2="23" y2="23"/>
                      </svg>
                    ) : (
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                        <circle cx="12" cy="12" r="3"/>
                      </svg>
                    )}
                  </button>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">I want to...</label>
                <div className="role-buttons">
                  <button
                    type="button"
                    className={`role-btn ${form.role === "ADOPTER" ? "role-btn-active" : "role-btn-inactive"}`}
                    onClick={() => setForm({ ...form, role: "ADOPTER" })}
                  >
                    Adopt a Pet
                  </button>
                  <button
                    type="button"
                    className={`role-btn ${form.role === "PET_OWNER" ? "role-btn-active" : "role-btn-inactive"}`}
                    onClick={() => setForm({ ...form, role: "PET_OWNER" })}
                  >
                    Rehome a Pet
                  </button>
                </div>
              </div>

              <button className="submit-btn" onClick={handleSubmit} disabled={loading}>
                {loading ? "Creating account..." : "Create Account"}
              </button>

              <div className="auth-divider"><span>or</span></div>

              <button className="google-btn" type="button" onClick={() => handleGoogleRegister()}>
                <span className="google-icon"></span>
                Continue with Google
              </button>

              <p className="auth-footer-text">
                Already have an account?{" "}
                <button className="auth-link" onClick={() => navigate("/login")}>
                  Sign in
                </button>
              </p>
            </div>
          </div>

        </div>
      </div>
      <div className="auth-bottom-dog">
        <img src={loginDog} alt="" className="auth-dog-peek" />
      </div>
    </div>
  );
}