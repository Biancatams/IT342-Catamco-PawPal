import React from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Navbar.css";
import "../styles/Landing.css";

export default function Landing() {
  const navigate = useNavigate();
  return (
    <div className="landing-page">

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

      <section className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">
            Find your perfect<br />
            <span>pet companion</span>
          </h1>
          <p className="hero-subtitle">
            Connect with loving pets looking for their forever homes.
            Whether you're looking to adopt or find a home for a pet,
            PawPal makes it easy and safe.
          </p>
          <div className="hero-buttons">
            <button className="btn-primary" onClick={() => navigate("/register")}>
              🐾 Get Started
            </button>
            <button className="btn-dark" onClick={() => navigate("/login")}>
              👤 Login
            </button>
          </div>
        </div>
        <div className="hero-illustration">
          <div className="hero-circle">🐱🐶</div>
        </div>
      </section>

      <section className="about-section">
        <div className="about-inner">
          <h2 className="about-title">What is PawPal?</h2>
          <p className="about-desc">
            PawPal is a simple and organized pet adoption platform. Whether
            you're looking to adopt a pet or find a loving home for one,
            PawPal connects pet owners and adopters in a safe and easy way.
          </p>
          <div className="feature-grid">
            {[
              { icon: "🐾", title: "Post a Pet", desc: "List your pet for adoption with photos and details." },
              { icon: "🔍", title: "Browse & Adopt", desc: "Find pets near you and send adoption requests." },
              { icon: "💬", title: "Connect", desc: "Approved adopters receive owner contact info to meet the pet." },
            ].map((item, i) => (
              <div className="feature-card" key={i}>
                <div className="feature-icon">{item.icon}</div>
                <div className="feature-title">{item.title}</div>
                <div className="feature-desc">{item.desc}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <footer className="footer">
        <p className="footer-brand">🐾 PawPal</p>
        <p className="footer-copy">© 2024 PawPal. All rights reserved.</p>
      </footer>

    </div>
  );
}