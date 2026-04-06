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
  const [showLogout, setShowLogout] = useState(false); // ← ADDED

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

  const handleAction = async (requestId, action) => {
    setActionLoading(requestId + action);
    try {
      await axios.put(
        `http://localhost:8080/api/v1/adoption-requests/${requestId}/${action}`,
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

  const confirmLogout = () => { // ← CHANGED (was handleLogout, now opens modal)
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  const statusLabel = (s) =>
    s === "AVAILABLE" ? "Available" : s === "PENDING" ? "Pending" : "Adopted";
  const statusClass = (s) =>
    s === "AVAILABLE"
      ? "badge-available"
      : s === "PENDING"
      ? "badge-pending"
      : "badge-adopted";

  const pendingCount = requests.filter((r) => r.status === "PENDING").length;

  return (
    <div className="od-page">

      {/* LOGOUT MODAL ← ADDED */}
      {showLogout && (
        <LogoutModal
          onConfirm={confirmLogout}
          onCancel={() => setShowLogout(false)}
        />
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
          {/* LEFT: Pet Card */}
          <div className="vr-pet-panel">
            {pet ? (
              <>
                <div className="vr-pet-img-wrap">
                  {pet.imageUrl ? (
                    <img
                      src={`http://localhost:8080${pet.imageUrl}`}
                      alt={pet.name}
                      className="vr-pet-img"
                    />
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

          {/* RIGHT: Requests List */}
          <div className="vr-requests-panel">
            <div className="vr-requests-header">
              <h3 className="vr-requests-title">Pending Requests</h3>
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
                  return (
                    <div key={req.id} className={`vr-request-card ${req.status !== "PENDING" ? "vr-request-resolved" : ""}`}>
                      <div className="vr-req-top">
                        <div className="vr-req-avatar">{initials}</div>
                        <div className="vr-req-info">
                          <div className="vr-req-name">{req.adopterName || "Adopter"}</div>
                          <div className="vr-req-reason-label">Reason for Adoption</div>
                        </div>
                        {req.status !== "PENDING" && (
                          <span className={`vr-status-chip ${req.status === "APPROVED" ? "chip-approved" : "chip-declined"}`}>
                            {req.status === "APPROVED" ? "Approved" : "Declined"}
                          </span>
                        )}
                      </div>
                      <p className="vr-req-message">
                        {req.message || req.reason || "No message provided."}
                      </p>
                      {req.status === "PENDING" && (
                        <div className="vr-req-actions">
                          <button
                            className="vr-btn-decline"
                            disabled={!!actionLoading}
                            onClick={() => handleAction(req.id, "decline")}
                          >
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                            </svg>
                            {actionLoading === req.id + "decline" ? "..." : "Decline"}
                          </button>
                          <button
                            className="vr-btn-accept"
                            disabled={!!actionLoading}
                            onClick={() => handleAction(req.id, "approve")}
                          >
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                              <polyline points="20 6 9 17 4 12"/>
                            </svg>
                            {actionLoading === req.id + "approve" ? "..." : "Accept Request"}
                          </button>
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