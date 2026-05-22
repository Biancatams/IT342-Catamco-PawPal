import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import pawLogo from "../../pawlogo.png";
import "../../shared/styles/Navbar.css";
import "./OwnerDashboard.css";
import LogoutModal from "../../shared/components/LogoutModal";

export default function OwnerDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "{}");
  const token = localStorage.getItem("token");
  const [pets, setPets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filter, setFilter] = useState("ALL");
  const [deleteModal, setDeleteModal] = useState({ open: false, petId: null, petName: "" });
  const [deleting, setDeleting] = useState(false);
  const [showLogout, setShowLogout] = useState(false);

  useEffect(() => {
    fetchMyPets();
  }, []);

  const fetchMyPets = async () => {
    try {
      const res = await axios.get("https://it342-catamco-pawpal-production.up.railway.app/api/v1/pets/my", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setPets(res.data.data?.pets || []);
    } catch (err) {
      setError("Failed to load your pets.");
    } finally {
      setLoading(false);
    }
  };

  const confirmDelete = (petId, petName) => {
    setDeleteModal({ open: true, petId, petName });
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await axios.delete(`https://it342-catamco-pawpal-production.up.railway.app/api/v1/pets/${deleteModal.petId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setPets((prev) => prev.filter((p) => p.id !== deleteModal.petId));
      setDeleteModal({ open: false, petId: null, petName: "" });
    } catch {
      setError("Failed to delete pet. Please try again.");
    } finally {
      setDeleting(false);
    }
  };

  const confirmLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  const counts = {
    ALL: pets.length,
    AVAILABLE: pets.filter((p) => p.status === "AVAILABLE").length,
    UNDER_REVIEW: pets.filter((p) => p.status === "UNDER_REVIEW").length,
    PENDING: pets.filter((p) => p.status === "PENDING").length,
    REJECTED: pets.filter((p) => p.status === "REJECTED").length,
    ADOPTED: pets.filter((p) => p.status === "ADOPTED").length,
  };

  const filtered = filter === "ALL" ? pets : pets.filter((p) => p.status === filter);

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

  const firstName = user.fullName?.split(" ")[0] || "there";

  const TABS = [
    { key: "ALL", label: "All" },
    { key: "UNDER_REVIEW", label: "Pending Review" },
    { key: "AVAILABLE", label: "Available" },
    { key: "REJECTED", label: "Rejected" },
    { key: "ADOPTED", label: "Adopted" },
  ];

  return (
    <div className="od-page">

      {showLogout && (
        <LogoutModal
          onConfirm={confirmLogout}
          onCancel={() => setShowLogout(false)}
        />
      )}

      {deleteModal.open && (
        <div className="od-modal-overlay">
          <div className="od-modal">
            <div className="od-modal-icon">🗑️</div>
            <h3 className="od-modal-title">Delete Listing?</h3>
            <p className="od-modal-msg">
              Are you sure you want to delete <strong>{deleteModal.petName}</strong>'s listing?
              This action cannot be undone.
            </p>
            <div className="od-modal-actions">
              <button
                className="od-modal-cancel"
                onClick={() => setDeleteModal({ open: false, petId: null, petName: "" })}
                disabled={deleting}
              >
                No, Keep It
              </button>
              <button
                className="od-modal-confirm"
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? "Deleting..." : "Yes, Delete"}
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
          <button className="navbar-link active" onClick={() => navigate("/owner/dashboard")}>Home</button>
          <button className="navbar-link" onClick={() => navigate("/owner/profile")}>Profile</button>
          <button className="navbar-btn-outline" onClick={() => setShowLogout(true)}>Logout</button>
        </div>
      </nav>

      <div className="od-body">
        <div className="od-header">
          <div>
            <h1 className="od-title">My Posted Pets</h1>
            <p className="od-subtitle">
              Hello, {firstName} — manage your pet listings and adoption requests.
            </p>
          </div>
          <button className="od-post-btn" onClick={() => navigate("/owner/post-pet")}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            Post a Pet
          </button>
        </div>

        <div className="od-stats">
          <div className="od-stat-card">
            <div className="od-stat-icon od-stat-total">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
              </svg>
            </div>
            <div className="od-stat-info">
              <div className="od-stat-label">Total Pets</div>
              <div className="od-stat-num">{counts.ALL}</div>
            </div>
          </div>
          <div className="od-stat-card">
            <div className="od-stat-icon od-stat-available">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
            </div>
            <div className="od-stat-info">
              <div className="od-stat-label">Available</div>
              <div className="od-stat-num">{counts.AVAILABLE}</div>
            </div>
          </div>
          <div className="od-stat-card">
            <div className="od-stat-icon od-stat-pending">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
            </div>
            <div className="od-stat-info">
              <div className="od-stat-label">Pending Requests</div>
              <div className="od-stat-num">{counts.PENDING}</div>
            </div>
          </div>
        </div>

        <div className="ad-filter-tabs" style={{ marginBottom: 24 }}>
          {TABS.map((t) => (
            <button
              key={t.key}
              className={`ad-filter-tab ${filter === t.key ? "ad-tab-active" : ""}`}
              onClick={() => setFilter(t.key)}
            >
              {t.label}
              {counts[t.key] > 0 && (
                <span style={{
                  marginLeft: 6, fontSize: 11, fontWeight: 700,
                  background: filter === t.key ? "rgba(255,255,255,0.25)" : "var(--border)",
                  color: filter === t.key ? "white" : "var(--muted)",
                  padding: "1px 7px", borderRadius: 100,
                }}>
                  {counts[t.key]}
                </span>
              )}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="od-loading">
            <div className="od-spinner" />
            <p>Loading your pets...</p>
          </div>
        ) : error ? (
          <div className="od-error">{error}</div>
        ) : filtered.length === 0 ? (
          <div className="od-empty">
            <div className="od-empty-icon">🐾</div>
            <h3>{filter === "ALL" ? "No pets posted yet" : `No ${statusLabel(filter).toLowerCase()} listings`}</h3>
            <p>{filter === "ALL" ? "Start by posting your first pet for adoption." : "Nothing here right now."}</p>
            {filter === "ALL" && (
              <button className="od-post-btn" onClick={() => navigate("/owner/post-pet")}>
                Post Your First Pet
              </button>
            )}
          </div>
        ) : (
          <div className="od-grid">
            {filtered.map((pet) => (
              <div key={pet.id} className="od-pet-card">
                <div className="od-pet-img-wrap">
                  {pet.imageUrl ? (
                    <img src={`https://it342-catamco-pawpal-production.up.railway.app${pet.imageUrl}`} alt={pet.name} className="od-pet-img" />
                  ) : (
                    <div className="od-pet-no-img"><span>🐾</span></div>
                  )}
                  <span className={`od-badge ${statusClass(pet.status)}`}>
                    {statusLabel(pet.status)}
                  </span>
                  {pet.status !== "ADOPTED" && (
                    <button
                      className="od-trash-btn"
                      onClick={(e) => { e.stopPropagation(); confirmDelete(pet.id, pet.name); }}
                      title="Delete listing"
                    >
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="3 6 5 6 21 6" />
                        <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                        <path d="M10 11v6M14 11v6" />
                        <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                      </svg>
                    </button>
                  )}
                </div>

                <div className="od-pet-body">
                  <h3 className="od-pet-name">{pet.name}</h3>
                  {pet.breed && <p className="od-pet-breed">{pet.breed}</p>}
                  {pet.status === "REJECTED" && pet.adminNote && (
                    <div style={{
                      display: "flex", alignItems: "flex-start", gap: 7,
                      background: "rgba(220,38,38,0.05)", border: "1px solid rgba(220,38,38,0.15)",
                      borderRadius: 8, padding: "8px 12px", marginTop: 6, marginBottom: 6,
                      fontSize: 12, color: "#b91c1c", fontStyle: "italic"
                    }}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 13, height: 13, flexShrink: 0, marginTop: 1 }}>
                        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                      </svg>
                      Reason: "{pet.adminNote}"
                    </div>
                  )}

                  <div className="od-pet-meta">
                    <div className="od-meta-row">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" />
                        <polyline points="12 6 12 12 16 14" />
                      </svg>
                      <span>{pet.age || "Unknown age"}</span>
                    </div>
                    <div className="od-meta-row">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
                      </svg>
                      <span>{pet.type}</span>
                    </div>
                    {pet.location && (
                      <div className="od-meta-row">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                          <circle cx="12" cy="10" r="3" />
                        </svg>
                        <span>{pet.location}</span>
                      </div>
                    )}
                  </div>

                  {pet.status !== "ADOPTED" && pet.status !== "REJECTED" ? (
                    <>
                      <div className="od-requests-row">
                        <div className="od-req-left">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                            <circle cx="9" cy="7" r="4" />
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                          </svg>
                          <div>
                            <div className="od-req-label">Adoption Requests</div>
                            <div className="od-req-sub">
                              {pet.requestCount > 0
                                ? `${pet.requestCount} pending request${pet.requestCount > 1 ? "s" : ""}`
                                : "No pending requests"}
                            </div>
                          </div>
                        </div>
                        {pet.requestCount > 0 && (
                          <span className="od-req-bubble">{pet.requestCount}</span>
                        )}
                      </div>

                      <div className="od-card-actions">
                        <button
                          className="od-btn-view"
                          onClick={() => navigate(`/owner/requests/${pet.id}`, { state: { pet } })}
                        >
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                            <circle cx="12" cy="12" r="3" />
                          </svg>
                          View Requests
                        </button>
                        <button
                          className="od-btn-edit"
                          onClick={() => navigate(`/owner/edit-pet/${pet.id}`, { state: { pet } })}
                        >
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                          </svg>
                          Edit
                        </button>
                      </div>
                    </>
                  ) : pet.status === "REJECTED" ? (
                    <div className="od-adopted-banner" style={{ background: "rgba(220,38,38,0.06)", color: "#dc2626", border: "1px solid rgba(220,38,38,0.15)" }}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
                      </svg>
                      Listing Rejected
                    </div>
                  ) : (
                    <div className="od-adopted-banner">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                      Adopted
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}