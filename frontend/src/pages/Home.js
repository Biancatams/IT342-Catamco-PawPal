import React from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Navbar.css";
import "../styles/Home.css";

export default function Home() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "{}");

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  return (
    <div className="home-page">
      <nav className="navbar">
        <button className="navbar-brand" onClick={() => navigate("/")}>
          <span className="navbar-logo">🐾</span>
          <span>PawPal</span>
        </button>
        <div className="navbar-links">
          <button className="navbar-btn-primary" onClick={handleLogout}>Logout</button>
        </div>
      </nav>

      <div className="home-content">
        <div className="welcome-card">
          <div className="welcome-icon">🐾</div>
          <h1 className="welcome-title">Welcome, {user.fullName || "User"}!</h1>
          <p className="welcome-role">
            You are logged in as <strong>{user.role}</strong>
          </p>
        </div>
      </div>
    </div>
  );
}
