import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import pawLogo from "../../pawlogo.png";
import "../../shared/styles/Navbar.css";
import "../../shared/styles/OwnerDashboard.css";
import "./AdminDashboard.css";
import LogoutModal from "../../shared/components/LogoutModal";

const REJECT_REASONS = [
  "Inappropriate or offensive content",
  "Fake or misleading listing",
  "Poor quality or unclear photo",
  "Incomplete or missing information",
  "Suspected animal abuse or neglect",
  "Duplicate listing",
  "Others...",
];

export default function AdminDashboard() {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");
  const user = JSON.parse(localStorage.getItem("user") || "{}");

  const [allPets, setAllPets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("UNDER_REVIEW");
  const [showLogout, setShowLogout] = useState(false);
  const [actionLoading, setActionLoading] = useState(null);

  const [rejectModal, setRejectModal] = useState({ open: false, petId: null, petName: "" });
  const [selectedReason, setSelectedReason] = useState("");
  const [customReason, setCustomReason] = useState("");
  const [rejecting, setRejecting] = useState(false);

  const [approveModal, setApproveModal] = useState({ open: false, petId: null, petName: "" });
  const [approving, setApproving] = useState(false);

  useEffect(() => { fetchAll(); }, []);

  const fetchAll = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/v1/pets/admin/all", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setAllPets(res.data.data?.pets || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const confirmLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  const handleApprove = async () => {
    setApproving(true);
    try {
      await axios.put(
        `http://localhost:8080/api/v1/pets/admin/${approveModal.petId}/approve`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setApproveModal({ open: false, petId: null, petName: "" });
      await fetchAll();
    } catch (err) {
      alert("Failed to approve. Please try again.");
    } finally {
      setApproving(false);
    }
  };

  const handleReject = async () => {
    const reason = selectedReason === "Others..." ? customReason.trim() : selectedReason;
    if (!reason) { alert("Please select or enter a reason."); return; }
    setRejecting(true);
    try {
      await axios.put(
        `http://localhost:8080/api/v1/pets/admin/${rejectModal.petId}/reject`,
        { reason },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setRejectModal({ open: false, petId: null, petName: "" });
      setSelectedReason("");
      setCustomReason("");
      await fetchAll();
    } catch (err) {
      alert("Failed to reject. Please try again.");
    } finally {
      setRejecting(false);
    }
  };

  const filtered = filter === "ALL" ? allPets : allPets.filter((p) => p.status === filter);

  const counts = {
    ALL: allPets.length,
    UNDER_REVIEW: allPets.filter((p) => p.status === "UNDER_REVIEW").length,
    AVAILABLE: allPets.filter((p) => p.status === "AVAILABLE").length,
    REJECTED: allPets.filter((p) => p.status === "REJECTED").length,
    ADOPTED: allPets.filter((p) => p.status === "ADOPTED").length,
  };

  const statusLabel = (s) =>
    s === "AVAILABLE" ? "Approved" : s === "UNDER_REVIEW" ? "Under Review"
    : s === "REJECTED" ? "Rejected" : s === "ADOPTED" ? "Adopted" : s;

  const statusClass = (s) =>
    s === "AVAILABLE" ? "badge-available" : s === "UNDER_REVIEW" ? "badge-review"
    : s === "REJECTED" ? "badge-rejected" : "badge-adopted";

  return (
    <div className="od-page">
      {showLogout && <LogoutModal onConfirm={confirmLogout} onCancel={() => setShowLogout(false)} />}

      {approveModal.open && (
        <div className="od-modal-overlay">
          <div className="od-modal">
            <div className="od-modal-icon ad-icon-approve">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="20 6 9 17 4 12"/>
                </svg>
            </div>
            <h3 className="od-modal-title">Approve Listing?</h3>
            <p className="od-modal-msg">
              Are you sure you want to approve <strong>{approveModal.petName}</strong>'s listing?
              It will become visible to all adopters.
            </p>
            <div className="od-modal-actions">
              <button className="od-modal-cancel" onClick={() => setApproveModal({ open: false, petId: null, petName: "" })} disabled={approving}>
                Cancel
              </button>
              <button className="od-modal-confirm" style={{ background: "#16a34a" }} onClick={handleApprove} disabled={approving}>
                {approving ? "Approving..." : "Yes, Approve"}
              </button>
            </div>
          </div>
        </div>
      )}

      {rejectModal.open && (
        <div className="od-modal-overlay">
          <div className="od-modal admin-reject-modal">
            <div className="od-modal-icon ad-icon-reject">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
                </svg>
            </div>
            <h3 className="od-modal-title">Reject Listing</h3>
            <p className="od-modal-msg">
              Select a reason for rejecting <strong>{rejectModal.petName}</strong>'s listing.
              The owner will be notified.
            </p>
            <div className="admin-reject-reasons">
              {REJECT_REASONS.map((r) => (
                <button
                  key={r}
                  className={`admin-reason-btn ${selectedReason === r ? "admin-reason-active" : ""}`}
                  onClick={() => { setSelectedReason(r); setCustomReason(""); }}
                >
                  {selectedReason === r && (
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 13, height: 13 }}>
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                  )}
                  {r}
                </button>
              ))}
            </div>
            {selectedReason === "Others..." && (
              <textarea
                className="admin-custom-reason"
                placeholder="Enter your reason here..."
                value={customReason}
                onChange={(e) => setCustomReason(e.target.value)}
                maxLength={300}
              />
            )}
            <div className="od-modal-actions" style={{ marginTop: 20 }}>
              <button className="od-modal-cancel" onClick={() => { setRejectModal({ open: false, petId: null, petName: "" }); setSelectedReason(""); setCustomReason(""); }} disabled={rejecting}>
                Cancel
              </button>
              <button className="od-modal-confirm" onClick={handleReject} disabled={rejecting}>
                {rejecting ? "Rejecting..." : "Reject Listing"}
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
          <span className="admin-nav-badge">Admin Panel</span>
          <button className="navbar-btn-outline" onClick={() => setShowLogout(true)}>Logout</button>
        </div>
      </nav>

      <div className="od-body">
        <div className="od-header">
          <div>
            <h1 className="od-title">Listing Review Dashboard</h1>
            <p className="od-subtitle">Review and manage pet listing submissions from owners.</p>
          </div>
        </div>

        <div className="od-stats" style={{ gridTemplateColumns: "repeat(4, 1fr)" }}>
          {[
            { key: "UNDER_REVIEW", label: "Pending Review", iconCls: "od-stat-pending", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg> },
            { key: "AVAILABLE", label: "Approved", iconCls: "od-stat-available", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg> },
            { key: "REJECTED", label: "Rejected", iconCls: "od-stat-rejected", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg> },
            { key: "ALL", label: "Total Listings", iconCls: "od-stat-total", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg> },
          ].map(({ key, label, iconCls, icon }) => (
            <div key={key} className="od-stat-card" onClick={() => setFilter(key)}
              style={{ cursor: "pointer", border: filter === key ? "1.5px solid var(--green)" : "1px solid var(--border)", background: filter === key ? "var(--orange)" : "white", transition: "all 0.2s" }}>
              <div className={`od-stat-icon ${iconCls}`} style={filter === key ? { background: "rgba(255,255,255,0.15)" } : {}}>
                {icon}
              </div>
              <div>
                <div className="od-stat-label" style={filter === key ? { color: "rgba(255,255,255,0.7)" } : {}}>{label}</div>
                <div className="od-stat-num" style={filter === key ? { color: "white" } : {}}>{counts[key]}</div>
              </div>
            </div>
          ))}
        </div>

        <div className="ad-filter-tabs" style={{ marginBottom: 24 }}>
          {["UNDER_REVIEW", "AVAILABLE", "REJECTED", "ALL"].map((t) => (
            <button key={t} className={`ad-filter-tab ${filter === t ? "ad-tab-active" : ""}`} onClick={() => setFilter(t)}>
              {t === "ALL" ? "All" : t === "UNDER_REVIEW" ? "Pending Review" : t === "AVAILABLE" ? "Approved" : "Rejected"}
              {counts[t] > 0 && (
                <span style={{ marginLeft: 6, fontSize: 11, fontWeight: 700, background: filter === t ? "rgba(255,255,255,0.25)" : "var(--border)", color: filter === t ? "white" : "var(--muted)", padding: "1px 7px", borderRadius: 100 }}>
                  {counts[t]}
                </span>
              )}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="od-loading"><div className="od-spinner" /><p>Loading listings...</p></div>
        ) : filtered.length === 0 ? (
          <div className="od-empty">
            <div className="od-empty-icon">🐾</div>
            <h3>No listings here</h3>
            <p>Nothing to review right now.</p>
          </div>
        ) : (
          <div className="admin-listings-list">
            {filtered.map((pet) => (
              <div key={pet.id} className="admin-listing-card">
                <div className="admin-listing-img-wrap">
                  {pet.imageUrl ? (
                    <img src={`http://localhost:8080${pet.imageUrl}`} alt={pet.name} className="admin-listing-img" />
                  ) : (
                    <div className="admin-listing-no-img"><span>🐾</span></div>
                  )}
                </div>

                <div className="admin-listing-info">
                  <div className="admin-listing-top">
                    <div>
                      <h3 className="admin-listing-name">{pet.name}</h3>
                      <p className="admin-listing-meta">{pet.type}{pet.breed ? ` · ${pet.breed}` : ""} · {pet.age}</p>
                    </div>
                    <span className={`od-badge ${statusClass(pet.status)}`} style={{ position: "static" }}>
                      {statusLabel(pet.status)}
                    </span>
                  </div>

                  <div className="admin-listing-details">
                    <div className="admin-detail-chip">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                      </svg>
                      {pet.owner?.fullName || "Unknown Owner"}
                    </div>
                    <div className="admin-detail-chip">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                      </svg>
                      {pet.location}
                    </div>
                    {pet.createdAt && (
                      <div className="admin-detail-chip">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
                        </svg>
                        Submitted {new Date(pet.createdAt).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
                      </div>
                    )}
                  </div>

                  {pet.description && (
                    <p className="admin-listing-desc">{pet.description.length > 120 ? pet.description.slice(0, 120) + "..." : pet.description}</p>
                  )}

                  {pet.status === "REJECTED" && pet.adminNote && (
                    <div className="admin-reject-note">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 13, height: 13, flexShrink: 0 }}>
                        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                      </svg>
                      Rejection reason: "{pet.adminNote}"
                    </div>
                  )}
                </div>

                {pet.status === "UNDER_REVIEW" && (
                  <div className="admin-listing-actions">
                    <button className="admin-btn-reject" onClick={() => setRejectModal({ open: true, petId: pet.id, petName: pet.name })}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                        <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                      </svg>
                      Reject
                    </button>
                    <button className="admin-btn-approve" onClick={() => setApproveModal({ open: true, petId: pet.id, petName: pet.name })}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="20 6 9 17 4 12"/>
                      </svg>
                      Approve
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}