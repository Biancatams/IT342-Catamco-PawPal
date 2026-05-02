import React, { useState, useEffect } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import axios from "axios";
import pawLogo from "../pawlogo.png";
import "../styles/Navbar.css";
import "../styles/OwnerDashboard.css";
import LogoutModal from "../components/LogoutModal";

export default function ViewRequests() {
  const navigate = useNavigate();
  const { petId } = useParams();
  const location = useLocation();
  const token = localStorage.getItem("token");
  const petFromState = location.state?.pet || null;

  const [pet, setPet] = useState(petFromState);
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionLoading, setActionLoading] = useState(null);
  const [showLogout, setShowLogout] = useState(false);

  // Decline reason modal state
  const [declineModal, setDeclineModal] = useState(null); // holds requestId when open
  const [declineReason, setDeclineReason] = useState("");
  const [declineError, setDeclineError] = useState("");

  useEffect(() => {
    fetchRequests();
    if (!pet) fetchPet();
  }, []);

  const fetchPet = async () => {
    try {
      const res = await axios.get(`http://localhost:8080/api/v1/pets/${petId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setPet(res.data.data);
    } catch {}
  };

  const fetchRequests = async () => {
    try {
      const res = await axios.get(
        `http://localhost:8080/api/v1/adoption-requests/pet/${petId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setRequests(res.data.data || []);
    } catch {
      setError("Failed to load requests.");
    } finally {
      setLoading(false);
    }
  };

  const openDeclineModal = (requestId) => {
    setDeclineReason("");
    setDeclineError("");
    setDeclineModal(requestId);
  };

  const handleDeclineConfirm = async () => {
    if (!declineReason.trim()) {
      setDeclineError("Please provide a reason for declining.");
      return;
    }
    setActionLoading(declineModal + "decline");
    try {
      await axios.put(
        `http://localhost:8080/api/v1/adoption-requests/${declineModal}/decline`,
        { declineReason: declineReason.trim() },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setDeclineModal(null);
      await fetchRequests();
    } catch {
      alert("Action failed. Please try again.");
    } finally {
      setActionLoading(null);
    }
  };

  const handleApprove = async (requestId) => {
    setActionLoading(requestId + "approve");
    try {
      await axios.put(
        `http://localhost:8080/api/v1/adoption-requests/${requestId}/approve`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      await fetchRequests();
    } catch {
      alert("Action failed. Please try again.");
    } finally {
      setActionLoading(null);
    }
  };

  const confirmLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  const statusLabel = (s) =>
    s === "AVAILABLE" ? "Available"
    : s === "PENDING" ? "Pending"
    : s === "UNDER_REVIEW" ? "Under Review"
    : s === "REJECTED" ? "Rejected"
    : "Adopted";

  const statusClass = (s) =>
    s === "AVAILABLE" ? "badge-available"
    : s === "PENDING" ? "badge-pending"
    : s === "UNDER_REVIEW" ? "badge-review"
    : s === "REJECTED" ? "badge-rejected"
    : "badge-adopted";

  const pendingCount = requests.filter((r) => r.status === "PENDING").length;

  return (
    <div className="od-page">
      {showLogout && (
        <LogoutModal onConfirm={confirmLogout} onCancel={() => setShowLogout(false)} />
      )}

      {/* ── Decline Reason Modal ── */}
      {declineModal && (
        <div style={{
          position: "fixed", inset: 0, zIndex: 1000,
          background: "rgba(0,0,0,0.35)", backdropFilter: "blur(3px)",
          display: "flex", alignItems: "center", justifyContent: "center",
          padding: 20,
        }}>
          <div style={{
            background: "white", borderRadius: 20, padding: 32,
            width: "100%", maxWidth: 460, boxShadow: "0 20px 60px rgba(0,0,0,0.15)",
          }}>
            {/* Icon */}
            <div style={{
              width: 52, height: 52, borderRadius: "50%",
              background: "rgba(220,38,38,0.08)", border: "1.5px solid rgba(220,38,38,0.2)",
              display: "flex", alignItems: "center", justifyContent: "center",
              marginBottom: 16,
            }}>
              <svg viewBox="0 0 24 24" fill="none" stroke="#dc2626" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 22, height: 22 }}>
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </div>

            <h2 style={{ fontSize: 20, fontWeight: 700, color: "var(--green)", marginBottom: 6 }}>
              Decline Adoption Request
            </h2>
            <p style={{ fontSize: 14, color: "var(--muted)", marginBottom: 20, lineHeight: 1.6 }}>
              Please let the adopter know why their request is being declined. This message will be visible to them.
            </p>

            <label style={{ display: "block", fontSize: 13, fontWeight: 600, color: "var(--green)", marginBottom: 8 }}>
              Reason for Declining <span style={{ color: "#dc2626" }}>*</span>
            </label>
            <textarea
              rows={4}
              value={declineReason}
              onChange={(e) => { setDeclineReason(e.target.value); setDeclineError(""); }}
              placeholder="e.g. We've already found a suitable adopter, the living situation doesn't match the pet's needs, etc."
              style={{
                width: "100%", borderRadius: 10, border: declineError ? "1.5px solid #dc2626" : "1.5px solid var(--border)",
                padding: "10px 14px", fontSize: 14, color: "var(--green)",
                resize: "vertical", fontFamily: "inherit", lineHeight: 1.6,
                outline: "none", boxSizing: "border-box",
                background: declineError ? "rgba(220,38,38,0.02)" : "white",
              }}
            />
            {declineError && (
              <p style={{ fontSize: 12, color: "#dc2626", marginTop: 6, marginBottom: 0 }}>{declineError}</p>
            )}

            <div style={{ display: "flex", gap: 10, marginTop: 20 }}>
              <button
                onClick={() => setDeclineModal(null)}
                style={{
                  flex: 1, padding: "11px 0", borderRadius: 10,
                  border: "1.5px solid var(--border)", background: "white",
                  color: "var(--muted)", fontWeight: 600, fontSize: 14, cursor: "pointer",
                }}
              >
                Cancel
              </button>
              <button
                onClick={handleDeclineConfirm}
                disabled={!!actionLoading}
                style={{
                  flex: 1, padding: "11px 0", borderRadius: 10,
                  border: "none", background: "#dc2626",
                  color: "white", fontWeight: 600, fontSize: 14, cursor: "pointer",
                  opacity: actionLoading ? 0.7 : 1,
                }}
              >
                {actionLoading ? "Declining..." : "Confirm Decline"}
              </button>
            </div>
          </div>
        </div>
      )}

      <nav className="navbar">
        <button className="navbar-brand" onClick={() => navigate("/")}>
          <img src={pawLogo} alt="PawPal logo" className="navbar-logo-img" />
          <span className="navbar-brand-text">PawPal</span>
        </button>
        <div className="navbar-links">
          <button className="navbar-link" onClick={() => navigate("/owner/dashboard")}>Home</button>
          <button className="navbar-link" onClick={() => navigate("/owner/profile")}>Profile</button>
          <button className="navbar-btn-outline" onClick={() => setShowLogout(true)}>Logout</button>
        </div>
      </nav>

      <div className="od-body">
        <div className="vr-header">
          <button className="vr-back-btn" onClick={() => navigate("/owner/dashboard")}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="15 18 9 12 15 6" />
            </svg>
            Back to Dashboard
          </button>
          <div>
            <h1 className="od-title">Adoption Requests</h1>
            <p className="od-subtitle">Review and manage adoption requests for your pets</p>
          </div>
        </div>

        <div className="vr-layout">
          <div className="vr-pet-panel">
            {pet ? (
              <>
                <div className="vr-pet-img-wrap">
                  {pet.imageUrl ? (
                    <img src={`http://localhost:8080${pet.imageUrl}`} alt={pet.name} className="vr-pet-img" />
                  ) : (
                    <div className="vr-pet-no-img"><span>🐾</span></div>
                  )}
                </div>
                <div className="vr-pet-info">
                  <div className="vr-pet-name-row">
                    <h2 className="vr-pet-name">{pet.name}</h2>
                    <span className={`od-badge ${statusClass(pet.status)}`}>
                      {statusLabel(pet.status)}
                    </span>
                  </div>
                  {pet.breed && <p className="vr-pet-breed">{pet.breed}</p>}
                  <div className="od-pet-meta" style={{ marginTop: 12 }}>
                    {pet.age && (
                      <div className="od-meta-row">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                        </svg>
                        <span>{pet.age}</span>
                      </div>
                    )}
                    <div className="od-meta-row">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                      </svg>
                      <span>{pet.type}</span>
                    </div>
                    {pet.location && (
                      <div className="od-meta-row">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                        </svg>
                        <span>{pet.location}</span>
                      </div>
                    )}
                  </div>
                </div>
              </>
            ) : (
              <div className="od-loading"><div className="od-spinner" /></div>
            )}
          </div>

          <div className="vr-requests-panel">
            <div className="vr-requests-header">
              <h3 className="vr-requests-title">Adoption Requests</h3>
              {pendingCount > 0 && (
                <span className="vr-active-badge">{pendingCount} Active</span>
              )}
            </div>

            {loading ? (
              <div className="od-loading"><div className="od-spinner" /></div>
            ) : error ? (
              <div className="od-error">{error}</div>
            ) : requests.length === 0 ? (
              <div className="vr-empty">
                <span>🐾</span>
                <p style={{ fontWeight: 700, color: "var(--green)", fontSize: 15 }}>
                  No paw-tential adopters yet!
                </p>
              </div>
            ) : (
              <div className="vr-requests-list">
                {requests.map((req) => {
                  const initials = (req.adopterName || "?")
                    .split(" ")
                    .map((n) => n[0])
                    .join("")
                    .toUpperCase()
                    .slice(0, 2);
                  const adopterImg = req.adopter?.profileImageUrl;
                  return (
                    <div key={req.id} className={`vr-request-card ${req.status !== "PENDING" ? "vr-request-resolved" : ""}`}>
                      <div className="vr-req-top">
                        {adopterImg ? (
                          <img
                            src={`http://localhost:8080${adopterImg}`}
                            alt={req.adopterName}
                            className="vr-req-avatar-img"
                          />
                        ) : (
                          <div className="vr-req-avatar">{initials}</div>
                        )}
                        <div className="vr-req-info">
                          <div className="vr-req-name">{req.adopterName || "Adopter"}</div>
                          <div className="vr-req-reason-label">Adoption Application</div>
                        </div>
                        {req.status !== "PENDING" && (
                          <span className={`vr-status-chip ${req.status === "APPROVED" ? "chip-approved" : "chip-declined"}`}>
                            {req.status === "APPROVED" ? "Approved" : "Declined"}
                          </span>
                        )}
                      </div>

                      <div className="vr-req-body">
                        {req.contactInfo && (
                          <div className="vr-req-field">
                            <div className="vr-req-field-label">Contact Information</div>
                            <div className="vr-req-contact-box">
                              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.4 2 2 0 0 1 3.6 1.22h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.8a16 16 0 0 0 5.29 5.29l.96-.96a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
                              </svg>
                              {req.contactInfo}
                            </div>
                          </div>
                        )}

                        <div className="vr-req-field">
                          <div className="vr-req-field-label">Reason for Adoption</div>
                          <div className="vr-req-field-value">
                            {req.reason || req.message || "No reason provided."}
                          </div>
                        </div>

                        {req.noteToOwner && (
                          <div className="vr-req-field">
                            <div className="vr-req-field-label">Note to Owner</div>
                            <div className="vr-req-note-box">"{req.noteToOwner}"</div>
                          </div>
                        )}

                        {/* Show decline reason if declined */}
                        {req.status === "DECLINED" && req.declineReason && (
                          <div className="vr-req-field">
                            <div className="vr-req-field-label" style={{ color: "#dc2626" }}>Your Decline Reason</div>
                            <div style={{
                              background: "rgba(220,38,38,0.05)", border: "1px solid rgba(220,38,38,0.18)",
                              borderRadius: 8, padding: "10px 14px", fontSize: 13,
                              color: "#b91c1c", lineHeight: 1.6,
                            }}>
                              {req.declineReason}
                            </div>
                          </div>
                        )}

                        {req.createdAt && (
                          <div className="vr-req-date">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
                            </svg>
                            Submitted {new Date(req.createdAt).toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" })}
                          </div>
                        )}
                      </div>

                      {req.status === "PENDING" && (
                        <div className="vr-req-footer">
                          <div className="vr-req-actions">
                            <button
                              className="vr-btn-decline"
                              disabled={!!actionLoading}
                              onClick={() => openDeclineModal(req.id)}
                            >
                              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                              </svg>
                              Decline
                            </button>
                            <button
                              className="vr-btn-accept"
                              disabled={!!actionLoading}
                              onClick={() => handleApprove(req.id)}
                            >
                              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                                <polyline points="20 6 9 17 4 12"/>
                              </svg>
                              {actionLoading === req.id + "approve" ? "..." : "Accept Request"}
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}