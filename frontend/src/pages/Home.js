import React from "react";
import { useNavigate } from "react-router-dom";
import pawLogo from "../pawlogo.png";
import homeDog from "../homedog.png";
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

  const roleLabel =
    user.role === "ADOPTER"
      ? "Adopter"
      : user.role === "PET_OWNER"
      ? "Pet Owner"
      : user.role === "ADMIN"
      ? "Administrator"
      : user.role || "User";

  return (
    <div className="home-page">
      <nav className="navbar">
        <button className="navbar-brand" onClick={() => navigate("/")}>
          <img src={pawLogo} alt="PawPal logo" className="navbar-logo-img" />
          <span className="navbar-brand-text">PawPal</span>
        </button>
        <div className="navbar-links">
          <span className="navbar-user-label">
            {user.fullName || "User"} <span className="dot">·</span> <strong>{roleLabel}</strong>
          </span>
          <button className="navbar-btn-outline" onClick={handleLogout}>
            Log Out
          </button>
        </div>
      </nav>

      <div className="home-content">
        <div className="home-welcome-wrap">
          <span className="home-eyebrow">Dashboard</span>
          <h1 className="home-title">
            Welcome back,<br />
            <em>{user.fullName?.split(" ")[0] || "there"}.</em>
          </h1>
          <p className="home-subtitle">
            You're signed in as a <strong>{roleLabel}</strong>.
            Your dashboard features are coming soon.
          </p>

          <div className="home-cards">
            <div className="home-card">
              <div className="home-card-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
              </div>
              <div className="home-card-label">Browse Pets</div>
              <p className="home-card-desc">View all pets available for adoption.</p>
            </div>

            <div className="home-card">
              <div className="home-card-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
                </svg>
              </div>
              <div className="home-card-label">My Requests</div>
              <p className="home-card-desc">Track your adoption applications.</p>
            </div>

            <div className="home-card">
              <div className="home-card-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                </svg>
              </div>
              <div className="home-card-label">My Profile</div>
              <p className="home-card-desc">Manage your account details.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}