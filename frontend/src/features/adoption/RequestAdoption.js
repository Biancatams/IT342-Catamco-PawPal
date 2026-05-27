import React, { useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import axios from "axios";
import pawLogo from "../../pawlogo.png";
import "../../shared/styles/Navbar.css";
import "../../shared/styles/OwnerDashboard.css";
import LogoutModal from "../../shared/components/LogoutModal";

export default function RequestAdoption() {
  const navigate = useNavigate();
  const { petId } = useParams();
  const location = useLocation();
  const token = localStorage.getItem("token");
  const user = JSON.parse(localStorage.getItem("user") || "{}");
  const pet = location.state?.pet || null;

  const [form, setForm] = useState({
    adopterName: user.fullName || "",
    contactInfo: user.phoneNumber || user.phone || "",
    reason: "",
    noteToOwner: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const [showLogout, setShowLogout] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async () => {
    if (!form.adopterName || !form.contactInfo || !form.reason) {
      setError("Please fill in all required fields.");
      return;
    }
    setError("");
    setLoading(true);
    try {
      await axios.post(
        "http://localhost:8080/api/v1/adoption-requests",
        {
          petId: petId || pet?.id,
          adopterName: form.adopterName,
          contactInfo: form.contactInfo,
          reason: form.reason,
          noteToOwner: form.noteToOwner || null,
        },
        { headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" } }
      );
      setSuccess(true);
    } catch (err) {
      const msg = err.response?.data?.error?.message || err.response?.data?.message;
      setError(msg || "Failed to submit request. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const confirmLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  // ── Success State ─────────────────────────────────────────────────────────
  if (success) {
    return (
      <div className="od-page">
        <nav className="navbar">
          <button className="navbar-brand" onClick={() => navigate("/")}>
            <img src={pawLogo} alt="PawPal logo" className="navbar-logo-img" />
            <span className="navbar-brand-text">PawPal</span>
          </button>
          <div className="navbar-links">
            <button className="navbar-link" onClick={() => navigate("/adopter/dashboard")}>Browse</button>
            <button className="navbar-link" onClick={() => navigate("/adopter/requests")}>My Requests</button>
            <button className="navbar-link" onClick={() => navigate("/adopter/profile")}>Profile</button>
            <button className="navbar-btn-outline" onClick={() => { localStorage.removeItem("token"); localStorage.removeItem("user"); navigate("/"); }}>Logout</button>
          </div>
        </nav>
        <div className="od-body" style={{ display: "flex", alignItems: "center", justifyContent: "center", minHeight: "60vh" }}>
          <div className="ra-success-card">
            <div className="ra-success-icon">🐾</div>
            <h2 className="ra-success-title">Request Submitted!</h2>
            <p className="ra-success-msg">
              Your adoption request for <strong>{pet?.name || "this pet"}</strong> has been sent to the owner.
              You'll be notified once they review your application.
            </p>
            <div className="ra-success-actions">
              <button className="pp-btn-submit" onClick={() => navigate("/adopter/requests")}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
                </svg>
                View My Requests
              </button>
              <button className="pp-btn-cancel" onClick={() => navigate("/adopter/dashboard")}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
                </svg>
                Back to Browse
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // ── Form ──────────────────────────────────────────────────────────────────
  return (
    <div className="od-page">
      {showLogout && (
        <LogoutModal onConfirm={confirmLogout} onCancel={() => setShowLogout(false)} />
      )}

      <nav className="navbar">
        <button className="navbar-brand" onClick={() => navigate("/")}>
          <img src={pawLogo} alt="PawPal logo" className="navbar-logo-img" />
          <span className="navbar-brand-text">PawPal</span>
        </button>
        <div className="navbar-links">
          <button className="navbar-link" onClick={() => navigate("/adopter/dashboard")}>Browse</button>
          <button className="navbar-link" onClick={() => navigate("/adopter/requests")}>My Requests</button>
          <button className="navbar-link" onClick={() => navigate("/adopter/profile")}>Profile</button>
          <button className="navbar-btn-outline" onClick={() => setShowLogout(true)}>Logout</button>
        </div>
      </nav>

      <div className="od-body">
        <button className="vr-back-btn" onClick={() => navigate(-1)} style={{ marginBottom: 8 }}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="15 18 9 12 15 6" />
          </svg>
          Back
        </button>

        <div className="pp-page-header">
          <h1 className="pp-page-title">Request Adoption</h1>
          <p className="pp-page-sub">Tell the owner why you'd be a great match for {pet?.name || "this pet"}</p>
        </div>

        {error && <div className="od-error" style={{ marginBottom: 20 }}>{error}</div>}

        <div className="ra-layout">
          {/* LEFT: Form */}
          <div className="pp-section ra-form-col">
            <h2 className="pp-section-title">Your Application</h2>

            <div className="pp-field">
              <label className="pp-label">Your Name <span className="pp-required">*</span></label>
              <input
                className="pp-input"
                name="adopterName"
                value={form.adopterName}
                onChange={handleChange}
                placeholder="Your full name"
              />
            </div>

            <div className="pp-field">
              <label className="pp-label">Contact Information <span className="pp-required">*</span></label>
              <input
                className="pp-input"
                name="contactInfo"
                value={form.contactInfo}
                onChange={handleChange}
                placeholder="Phone number or email the owner can reach you at"
              />
              <p style={{ fontSize: 11, color: "var(--muted)", marginTop: 4 }}>
                This will only be shared with the owner if your request is approved.
              </p>
            </div>

            <div className="pp-field">
              <label className="pp-label">Why do you want to adopt {pet?.name || "this pet"}? <span className="pp-required">*</span></label>
              <textarea
                className="pp-textarea"
                name="reason"
                value={form.reason}
                onChange={handleChange}
                placeholder="Tell the owner about your living situation, experience with pets, and why you'd be a great home..."
                maxLength={1000}
                style={{ minHeight: 140 }}
              />
              <div className="pp-char-count">{form.reason.length}/1000 characters</div>
            </div>

            <div className="pp-field">
              <label className="pp-label">Note to Owner <span style={{ fontSize: 12, color: "var(--muted)", fontWeight: 400 }}>(optional)</span></label>
              <textarea
                className="pp-textarea"
                name="noteToOwner"
                value={form.noteToOwner}
                onChange={handleChange}
                placeholder="Any additional message you'd like to share with the pet owner..."
                maxLength={500}
                style={{ minHeight: 90 }}
              />
              <div className="pp-char-count">{form.noteToOwner.length}/500 characters</div>
            </div>

            <div className="pp-tips-card" style={{ marginTop: 4 }}>
              <div className="pp-tips-title">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 14, height: 14 }}>
                  <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                Tips for a Great Application
              </div>
              <ul className="pp-tips-list">
                <li>Mention your living situation (house, apartment, yard)</li>
                <li>Share your experience with pets</li>
                <li>Describe your daily routine and how the pet fits in</li>
                <li>Be honest and genuine — owners appreciate authenticity</li>
              </ul>
            </div>
          </div>

          {/* RIGHT: Pet Summary */}
          {pet && (
            <div className="ra-pet-summary">
              <div className="pp-section">
                <h2 className="pp-section-title">Applying For</h2>
                <div className="ra-pet-img-wrap">
                  {pet.imageUrl ? (
                    <img src={`${process.env.REACT_APP_API_URL || "http://localhost:8080"}${pet.imageUrl}`} alt={pet.name} className="ra-pet-img" />
                  ) : (
                    <div className="ra-pet-no-img"><span>🐾</span></div>
                  )}
                </div>
                <h3 className="ra-pet-name">{pet.name}</h3>
                {pet.breed && <p className="ra-pet-breed">{pet.breed}</p>}
                <div className="ra-pet-chips">
                  {pet.age && (
                    <div className="pd-meta-chip">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                      </svg>
                      {pet.age}
                    </div>
                  )}
                  <div className="pd-meta-chip">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                    </svg>
                    {pet.type}
                  </div>
                  {pet.location && (
                    <div className="pd-meta-chip">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                      </svg>
                      {pet.location}
                    </div>
                  )}
                </div>

                {/* Traits preview */}
                {pet.characteristics?.length > 0 && (
                  <div style={{ marginTop: 14 }}>
                    <div style={{ fontSize: 11, fontWeight: 700, color: "var(--muted)", letterSpacing: 1, textTransform: "uppercase", marginBottom: 8 }}>Personality</div>
                    <div className="pd-traits-wrap">
                      {pet.characteristics.map((t) => (
                        <span key={t} className="pd-trait-tag" style={{ fontSize: 11 }}>{t}</span>
                      ))}
                    </div>
                  </div>
                )}

                <div style={{ marginTop: 16, padding: "12px 14px", background: "var(--cream)", borderRadius: 10, border: "1px solid var(--border)" }}>
                  <div style={{ fontSize: 11, fontWeight: 700, color: "var(--muted)", letterSpacing: 1, textTransform: "uppercase", marginBottom: 4 }}>What happens next?</div>
                  <p style={{ fontSize: 12, color: "var(--muted)", lineHeight: 1.7 }}>
                    After you submit, the owner will review your application and get back to you. You can track the status in <strong>My Requests</strong>.
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Submit */}
        <div className="pp-actions" style={{ marginTop: 28 }}>
          <button className="pp-btn-submit" onClick={handleSubmit} disabled={loading}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            {loading ? "Submitting..." : "Submit Adoption Request"}
          </button>
          <button className="pp-btn-cancel" onClick={() => navigate(-1)} disabled={loading}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}