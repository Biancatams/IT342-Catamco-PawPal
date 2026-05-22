import React from "react";
import { useNavigate } from "react-router-dom";
import pawLogo from "../../pawlogo.png";
import "../../shared/styles/Navbar.css";
import "./Auth.css";

export default function Banned() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  return (
    <div className="auth-page">
      <nav className="navbar">
        <button className="navbar-brand" onClick={() => navigate("/")}>
          <img src={pawLogo} alt="PawPal logo" className="navbar-logo-img" />
          <span className="navbar-brand-text">PawPal</span>
        </button>
      </nav>

      <div className="auth-container">
        <div style={{ textAlign: "center", maxWidth: 480, margin: "0 auto", padding: "60px 24px" }}>
          <div style={{ width: 72, height: 72, borderRadius: "50%", background: "#fee2e2", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 24px" }}>
            <svg viewBox="0 0 24 24" fill="none" stroke="#dc2626" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 36, height: 36 }}>
              <circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
            </svg>
          </div>

          <h2 style={{ fontSize: 24, fontWeight: 700, color: "var(--dark)", marginBottom: 12 }}>
            Account Suspended
          </h2>

          <p style={{ fontSize: 15, color: "var(--muted)", lineHeight: 1.7, marginBottom: 32 }}>
            Your account has been suspended due to a violation of PawPal's community rules and 
            guidelines. We take the safety of our platform seriously and have restricted access 
            to your account accordingly.
          </p>

          <button
            onClick={handleLogout}
            style={{ background: "#ef4444", color: "white", border: "none", borderRadius: 10, padding: "12px 32px", fontWeight: 600, fontSize: 15, cursor: "pointer" }}
          >
            Back to Home
          </button>
        </div>
      </div>
    </div>
  );
}