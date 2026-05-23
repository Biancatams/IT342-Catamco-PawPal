import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import axios from "axios";
import { useGoogleLogin } from "@react-oauth/google";
import pawLogo from "../../pawlogo.png";
import loginDog from "../../loginregister dog.png";
import "../../shared/styles/Navbar.css";
import "./Auth.css";

export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: "", password: "" });
  const [showPass, setShowPass] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError("");
  };

  const handleNavigateByRole = async (user, token) => {
  if (user.role === "ADMIN") {
    navigate("/admin/dashboard");
    return;
  }
  try {
    const verifRes = await axios.get("${process.env.REACT_APP_API_URL}/api/v1/verification/my", {
      headers: { Authorization: `Bearer ${token}` },
    });
    const status = verifRes.data.data?.status;
    if (status === "APPROVED") {
      if (user.role === "PET_OWNER") navigate("/owner/dashboard");
      else navigate("/adopter/dashboard");
    } else {
      navigate("/verification/status");
    }
  } catch {
    navigate("/verification/status");
  }
};

  const fetchAndSaveFullUser = async (token) => {
    const meRes = await axios.get("${process.env.REACT_APP_API_URL}/api/v1/users/me", {
      headers: { Authorization: `Bearer ${token}` },
    });
    localStorage.setItem("user", JSON.stringify(meRes.data.data));
    return meRes.data.data;
  };

  const handleSubmit = async () => {
    if (!form.email || !form.password) {
      setError("Please enter your email and password.");
      return;
    }
    setLoading(true);
    try {
      const res = await axios.post("${process.env.REACT_APP_API_URL}/api/v1/auth/login", form);
      if (res.data.success) {
        const token = res.data.data.accessToken;
        if (res.data.data.user?.isBanned) {
          navigate("/banned");
          return;
        }
        localStorage.setItem("token", token);
        const fullUser = await fetchAndSaveFullUser(token);
        handleNavigateByRole(fullUser, token);
      } else {
        setError(res.data.error?.message || "Login failed.");
      }
    } catch (err) {
      setError(err.response?.data?.error?.message || "Invalid email or password.");
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = useGoogleLogin({
    onSuccess: async (tokenResponse) => {
      try {
        const res = await axios.post(
          "${process.env.REACT_APP_API_URL}/api/v1/auth/google-login",
          { token: tokenResponse.access_token }
        );
        if (res.data.success) {
          const token = res.data.data.accessToken;
          if (res.data.data.user?.isBanned) {
            navigate("/banned");
            return;
          }
          localStorage.setItem("token", token);
          const fullUser = await fetchAndSaveFullUser(token);
          handleNavigateByRole(fullUser, token);
        } else {
          setError(res.data.error?.message || "Google login failed.");
        }
      } catch (err) {
        setError(
          err.response?.data?.error?.message ||
          "No account found. Please register first."
        );
      }
    },
    onError: () => setError("Google login failed. Please try again."),
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

          {/* LEFT */}
          <div className="auth-panel-left">
            <div className="auth-panel-eyebrow">Welcome Back</div>
            <h2 className="auth-panel-title">
              Sign in to<br />
              <em>PawPal</em>
            </h2>
            <p className="auth-panel-body">
              Continue your adoption journey. Every pet you help find a home
              makes a real difference.
            </p>
            <div className="auth-panel-tags">
              <span className="auth-tag">Safe &amp; Secure</span>
              <span className="auth-tag">Free Platform</span>
              <span className="auth-tag">Web &amp; Mobile</span>
            </div>
          </div>

          {/* RIGHT CARD */}
          <div className="auth-card">
            <div className="auth-card-header">
              <h1 className="auth-card-title">Sign In</h1>
              <p className="auth-card-sub">Enter your credentials to continue.</p>
            </div>

            {error && <div className="auth-error">{error}</div>}

            <div className="auth-form">
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
                <div className="form-label-row">
                  <label className="form-label">Password</label>
                  
                </div>
                <div className="input-wrapper">
                  <input
                    className="form-input"
                    name="password"
                    type={showPass ? "text" : "password"}
                    value={form.password}
                    onChange={handleChange}
                    onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
                    placeholder="Enter your password"
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

              <button className="submit-btn" onClick={handleSubmit} disabled={loading}>
                {loading ? "Signing in..." : "Log In"}
              </button>

              <div className="auth-divider"><span>or</span></div>

              <button className="google-btn" type="button" onClick={() => handleGoogleLogin()}>
                <span className="google-icon"></span>
                Continue with Google
              </button>

              <p className="auth-footer-text">
                New to PawPal?{" "}
                <button className="auth-link" onClick={() => navigate("/register")}>
                  Create an account
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