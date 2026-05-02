import React from "react";
import { useNavigate, useLocation } from "react-router-dom";
import pawLogo from "../../pawlogo.png";
import homeDog from "../../homedog.png";
import "../../shared/styles/Navbar.css";
import "./Landing.css";

export default function Landing() {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <div className="landing-page">

      {/* NAVBAR */}
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

      {/* HERO */}
      <section className="hero-section">
        <div className="hero-content">
          <span className="hero-eyebrow">Pet Adoption Platform</span>
          <h1 className="hero-title">
            Give a pet a<br />
            <em>forever</em> home.
          </h1>
          <p className="hero-body">
            PawPal connects caring adopters with pets in need of a loving home.
            Whether you're looking to adopt or rehome a pet, our platform makes
            the process simple, safe, and organized.
          </p>
          <div className="hero-actions">
            <button className="btn-primary" onClick={() => navigate("/register")}>
              Get Started
            </button>
            <button className="btn-ghost" onClick={() => navigate("/login")}>
              Sign In
            </button>
          </div>
        </div>

        <div className="hero-visual">
          <div className="hero-image-block">
            <div className="hero-image-overlay" />
            {/* Cute pet illustration placeholder — swap with your actual image */}
            <img src={homeDog} alt="Dog" className="hero-dog-img" />
            <div className="hero-image-text">
              <div className="hero-image-label">Every pet deserves<br />a loving home.</div>
              <div className="hero-image-sub">Connecting owners and adopters since 2026</div>
            </div>
          </div>
        </div>
      </section>

      {/* MARQUEE */}
      <div className="marquee-strip">
        <div className="marquee-inner">
          {[...Array(2)].map((_, i) => (
            <span key={i}>
              <span className="marquee-item">Safe Adoption Process</span>
              <span className="marquee-item"><strong>·</strong></span>
              <span className="marquee-item">Verified Listings</span>
              <span className="marquee-item"><strong>·</strong></span>
              <span className="marquee-item">Email Notifications</span>
              <span className="marquee-item"><strong>·</strong></span>
              <span className="marquee-item">Free Platform</span>
              <span className="marquee-item"><strong>·</strong></span>
              <span className="marquee-item">Web & Mobile</span>
              <span className="marquee-item"><strong>·</strong></span>
              <span className="marquee-item">Google Maps Integration</span>
              <span className="marquee-item"><strong>·</strong></span>
            </span>
          ))}
        </div>
      </div>

      {/* HOW IT WORKS */}
      <section className="how-section">
        <div className="section-header">
          <div>
            <span className="section-eyebrow">How it Works</span>
            <h2 className="section-title">A simple process<br />from start to finish</h2>
          </div>
          <p className="section-body">
            PawPal streamlines the entire adoption journey — from finding a pet
            to getting approved — in just a few steps.
          </p>
        </div>

        <div className="steps-grid">
          <div className="step-card">
            <span className="step-num">Step 01</span>
            <div className="step-icon-wrap">
              <svg viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            </div>
            <div className="step-title">Create an Account</div>
            <p className="step-desc">
              Register as an Adopter or Pet Owner. Sign up with email or continue with Google.
            </p>
          </div>

          <div className="step-card">
            <span className="step-num">Step 02</span>
            <div className="step-icon-wrap">
              <svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            </div>
            <div className="step-title">Browse or Post Pets</div>
            <p className="step-desc">
              Adopters browse available pets. Pet owners post listings with photos, details, and location.
            </p>
          </div>

          <div className="step-card">
            <span className="step-num">Step 03</span>
            <div className="step-icon-wrap">
              <svg viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <div className="step-title">Get Approved & Connect</div>
            <p className="step-desc">
              Submit an adoption request. Once approved, you'll receive the owner's contact details by email.
            </p>
          </div>
        </div>
      </section>

      {/* ROLES */}
      <section className="roles-section">
        <div className="roles-inner">
          <div className="section-header">
            <div>
              <span className="section-eyebrow">Who is PawPal for?</span>
              <h2 className="section-title">Two ways to use<br />the platform</h2>
            </div>
            <p className="section-body">
              PawPal serves both sides of pet adoption —
              those looking to welcome a new pet, and those
              looking to find a safe home for one.
            </p>
          </div>

          <div className="roles-grid">
            <div className="role-card">
              <span className="role-tag">For Adopters</span>
              <div className="role-title">Find Your New Companion</div>
              <p className="role-desc">
                Browse verified pet listings and submit adoption requests
                directly through the platform.
              </p>
              <ul className="role-features">
                {["Browse all available pets", "Filter by pet type", "Submit adoption requests", "Get notified on request status"].map((f, i) => (
                  <li key={i}>
                    <span className="check-circle">
                      <svg viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
                    </span>
                    {f}
                  </li>
                ))}
              </ul>
            </div>

            <div className="role-card">
              <span className="role-tag">For Pet Owners</span>
              <div className="role-title">Rehome with Confidence</div>
              <p className="role-desc">
                List your pet and manage adoption requests from your
                dashboard. You decide who gives your pet a home.
              </p>
              <ul className="role-features">
                {["Post pets with photos", "Manage adoption requests", "Accept or decline applicants", "Email notifications on activity"].map((f, i) => (
                  <li key={i}>
                    <span className="check-circle">
                      <svg viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
                    </span>
                    {f}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="cta-section">
        <div className="cta-left">
          <span className="section-eyebrow">Get Started Today</span>
          <h2 className="section-title">Ready to make<br />a difference?</h2>
          <p className="section-body">
            Join PawPal and be part of a community that believes
            every pet deserves a loving, permanent home.
          </p>
        </div>

        <div className="cta-card">
          <div className="cta-card-title">Create a free account</div>
          <p className="cta-card-sub">No fees. No complications. Just a simple way to adopt or rehome a pet.</p>
          <ul className="cta-checklist">
            {[
              "Register in under 2 minutes",
              "Available on web and mobile",
              "Secure JWT authentication",
              "Email notifications included",
            ].map((item, i) => (
              <li key={i}>
                <span className="check-dot">
                  <svg viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
                </span>
                {item}
              </li>
            ))}
          </ul>
          <button className="btn-cta" onClick={() => navigate("/register")}>
            Create an Account
          </button>
          <p className="cta-note">
            Already have an account?{" "}
            <button
              style={{ background: 'none', border: 'none', color: '#C76953', fontWeight: 600, cursor: 'pointer', fontFamily: 'DM Sans, sans-serif', fontSize: '12px' }}
              onClick={() => navigate("/login")}
            >
              Sign in here
            </button>
          </p>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="footer">
        <div className="footer-brand">
          <img src={pawLogo} alt="PawPal" style={{ width: 22, height: 22, objectFit: 'contain', marginRight: 8, verticalAlign: 'middle', borderRadius: 4 }} />
          PawPal
        </div>
        <p className="footer-copy">© 2026 PawPal. All rights reserved.</p>
      </footer>

    </div>
  );
}