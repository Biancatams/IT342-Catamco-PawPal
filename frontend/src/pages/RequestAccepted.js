import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import pawLogo from "../pawlogo.png";
import "../styles/Navbar.css";
import "../styles/OwnerDashboard.css";
import "../styles/RequestAccepted.css";
import LogoutModal from "../components/LogoutModal";

/* ── Per-status configuration ─────────────────────────────── */
const STATUS = {
  APPROVED: {
    chipCls:    "badge-available",
    chipLabel:  "Adopted",
    circleStyle: { background: "rgba(31,51,39,0.1)", border: "1.5px solid rgba(31,51,39,0.2)" },
    circleIconStroke: "var(--green)",
    circleIcon: "check",
    heroTitle:  "Congratulations!",
    heroSub:    "Your adoption request has been accepted!",
    heroSubColor: "var(--orange)",
    heroMsg:    "The pet owner has approved your request. You can now coordinate the next steps for bringing your new companion home.",
    boxStyle:   { background: "rgba(31,51,39,0.04)", border: "1px solid rgba(31,51,39,0.12)" },
    boxTitleColor: "var(--orange)",
    boxBulletColor: "var(--orange)",
    boxTitle:   "Next Steps",
    boxIcon:    "info",
    boxItems: [
      "Contact the owner using the information below",
      "Schedule a meet-and-greet with the pet",
      "Discuss adoption fees and requirements",
      "Prepare your home for your new companion",
    ],
    showOwner: true,
    backStyle: { background: "var(--orange)" },
    backHover: "var(--orange-hover)",
  },
  PENDING: {
    chipCls:    "badge-pending",
    chipLabel:  "Pending Review",
    circleStyle: { background: "rgba(199,105,83,0.1)", border: "1.5px solid rgba(199,105,83,0.25)" },
    circleIconStroke: "var(--orange)",
    circleIcon: "clock",
    heroTitle:  "Request Submitted",
    heroSub:    "Your application is under review.",
    heroSubColor: "var(--orange)",
    heroMsg:    "The pet owner is currently reviewing your adoption request. This usually takes 1–3 business days. You will be notified once a decision has been made.",
    boxStyle:   { background: "rgba(199,105,83,0.05)", border: "1px solid rgba(199,105,83,0.18)" },
    boxTitleColor: "var(--orange)",
    boxBulletColor: "var(--orange)",
    boxTitle:   "While You Wait",
    boxIcon:    "clock",
    boxItems: [
      "Make sure your profile information is up to date",
      "Read about caring for this type of pet",
      "Prepare questions for the owner",
      "Think about what supplies you'll need",
    ],
    showOwner: false,
    backStyle: { background: "var(--green)" },
    backHover: "#162a1f",
  },
  DECLINED: {
    chipCls:    "badge-adopted",
    chipLabel:  "Declined",
    circleStyle: { background: "rgba(220,38,38,0.08)", border: "1.5px solid rgba(220,38,38,0.2)" },
    circleIconStroke: "#dc2626",
    circleIcon: "x",
    heroTitle:  "Request Declined",
    heroSub:    "Your application was not approved this time.",
    heroSubColor: "#dc2626",
    heroMsg:    "Unfortunately, the pet owner did not approve your adoption request. Don't be discouraged — there are many other pets waiting for a loving home like yours.",
    boxStyle:   { background: "rgba(220,38,38,0.04)", border: "1px solid rgba(220,38,38,0.14)" },
    boxTitleColor: "#dc2626",
    boxBulletColor: "#dc2626",
    boxTitle:   "What You Can Do",
    boxIcon:    "info",
    boxItems: [
      "Browse other pets that may be a great match",
      "Consider updating your adopter profile with more details",
      "Reach out to other listings that interest you",
      "Don't give up — the right pet is out there for you",
    ],
    showOwner: false,
    backStyle: { background: "var(--green)" },
    backHover: "#162a1f",
  },
};

function CircleIcon({ type, stroke }) {
  if (type === "check") return (
    <svg viewBox="0 0 24 24" fill="none" stroke={stroke} strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 28, height: 28 }}>
      <polyline points="20 6 9 17 4 12"/>
    </svg>
  );
  if (type === "clock") return (
    <svg viewBox="0 0 24 24" fill="none" stroke={stroke} strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 28, height: 28 }}>
      <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
    </svg>
  );
  if (type === "x") return (
    <svg viewBox="0 0 24 24" fill="none" stroke={stroke} strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 28, height: 28 }}>
      <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
    </svg>
  );
  return null;
}

export default function RequestDetail() {
  const navigate = useNavigate();
  const location = useLocation();
  const [showLogout, setShowLogout] = useState(false);
  const [copied, setCopied] = useState(null);

  const pet    = location.state?.pet    || null;
  const owner  = location.state?.owner  || null;
  const status = location.state?.status || "APPROVED";
  const sd = STATUS[status] || STATUS.APPROVED;

  const confirmLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  const handleCopy = (text, field) => {
    navigator.clipboard.writeText(text);
    setCopied(field);
    setTimeout(() => setCopied(null), 2000);
  };

  return (
    <div className="od-page">
      {showLogout && <LogoutModal onConfirm={confirmLogout} onCancel={() => setShowLogout(false)} />}

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

        {/* Back */}
        <button className="vr-back-btn" onClick={() => navigate("/adopter/requests")} style={{ marginBottom: 20 }}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
          Back to My Requests
        </button>

        <div className="rac-layout">

          {/* ── LEFT: Pet Card ── */}
          {pet && (
            <div className="rac-pet-card">
              <div className="rac-pet-img-wrap">
                {pet.imageUrl ? (
                  <img src={`http://localhost:8080${pet.imageUrl}`} alt={pet.name} className="rac-pet-img" />
                ) : (
                  <div className="rac-pet-no-img">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#b89e8a" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 48, height: 48 }}>
                      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                    </svg>
                  </div>
                )}
              </div>
              <div className="rac-pet-info">
                <div className="rac-pet-header">
                  <h2 className="rac-pet-name">{pet.name}</h2>
                  <span className={`od-badge ${sd.chipCls}`} style={{ position: "static" }}>
                    {sd.chipLabel}
                  </span>
                </div>
                {pet.breed && <p className="rac-pet-breed">{pet.breed}</p>}
                <div className="rac-pet-meta">
                  {pet.age && (
                    <div className="rac-meta-row">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                      </svg>
                      {pet.age}
                    </div>
                  )}
                  {pet.type && (
                    <div className="rac-meta-row">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                      </svg>
                      {pet.type}
                    </div>
                  )}
                  {pet.location && (
                    <div className="rac-meta-row">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                      </svg>
                      {pet.location}
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* ── RIGHT ── */}
          <div className="rac-main-col">

            {/* Hero / Congrats Card */}
            <div className="rac-congrats-card">
              <div className="rac-check-circle" style={{ ...sd.circleStyle, width: 68, height: 68 }}>
                <CircleIcon type={sd.circleIcon} stroke={sd.circleIconStroke} />
              </div>

              <h1 className="rac-congrats-title">{sd.heroTitle}</h1>
              <p className="rac-congrats-sub" style={{ color: sd.heroSubColor }}>{sd.heroSub}</p>
              <p className="rac-congrats-msg">{sd.heroMsg}</p>

              <div className="rac-next-steps" style={sd.boxStyle}>
                <div className="rac-next-steps-title" style={{ color: sd.boxTitleColor }}>
                  <svg viewBox="0 0 24 24" fill="none" stroke={sd.boxTitleColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 15, height: 15, flexShrink: 0 }}>
                    <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                  {sd.boxTitle}
                </div>
                <ul className="rac-steps-list">
                  {sd.boxItems.map((item, i) => (
                    <li key={i} style={{ "--bullet-color": sd.boxBulletColor }}>{item}</li>
                  ))}
                </ul>
              </div>
            </div>

            {/* ── Owner Contact — APPROVED only ── */}
            {sd.showOwner && owner && (
              <div className="rac-owner-card">
                <h2 className="rac-owner-card-title">Owner Contact Information</h2>

                <div className="rac-owner-identity">
                  <div className="rac-owner-avatar" style={{ overflow: "hidden", padding: 0 }}>
                    {owner.profileImageUrl ? (
                      <img
                        src={`http://localhost:8080${owner.profileImageUrl}`}
                        alt={owner.fullName}
                        style={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: "50%" }}
                      />
                    ) : (
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                      </svg>
                    )}
                  </div>
                  <div>
                    <div className="rac-owner-name">{owner.fullName || "Pet Owner"}</div>
                    <div className="rac-owner-role">Pet Owner</div>
                  </div>
                </div>

                {/* Phone */}
                {owner.phoneNumber && (
                  <div className="rac-contact-row">
                    <div className="rac-contact-icon">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.4 2 2 0 0 1 3.6 1.22h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.8a16 16 0 0 0 5.29 5.29l.96-.96a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
                      </svg>
                    </div>
                    <div className="rac-contact-info">
                      <div className="rac-contact-label">Phone Number</div>
                      <div className="rac-contact-value">{owner.phoneNumber}</div>
                    </div>
                    <button className="rac-copy-btn" onClick={() => handleCopy(owner.phoneNumber, "phone")} title="Copy phone number">
                      {copied === "phone" ? (
                        <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="20 6 9 17 4 12"/>
                        </svg>
                      ) : (
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                        </svg>
                      )}
                    </button>
                  </div>
                )}

                {/* Email */}
                {owner.email && (
                  <div className="rac-contact-row">
                    <div className="rac-contact-icon">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>
                      </svg>
                    </div>
                    <div className="rac-contact-info">
                      <div className="rac-contact-label">Email Address</div>
                      <div className="rac-contact-value">{owner.email}</div>
                    </div>
                    <button className="rac-copy-btn" onClick={() => handleCopy(owner.email, "email")} title="Copy email">
                      {copied === "email" ? (
                        <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="20 6 9 17 4 12"/>
                        </svg>
                      ) : (
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                        </svg>
                      )}
                    </button>
                  </div>
                )}

                <button className="rac-back-btn" onClick={() => navigate("/adopter/dashboard")}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="15 18 9 12 15 6"/>
                  </svg>
                  Back to Browse
                </button>
              </div>
            )}

            {/* ── Important Reminders ── */}
            <div className="rac-reminders-card">
              <div className="rac-reminders-title">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                </svg>
                Important Reminders
              </div>
              <ul className="rac-reminders-list">
                <li>Always meet in a safe, public location first</li>
                <li>Ask about the pet's medical history and vaccinations</li>
                <li>Discuss any adoption fees or contracts</li>
                <li>Take time to ensure it's the right fit for both you and the pet</li>
                <li>Consider a trial period if the owner agrees</li>
              </ul>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}