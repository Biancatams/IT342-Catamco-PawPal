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

const REJECT_VERIF_REASONS = [
  "ID image is blurry or unreadable",
  "ID appears to be expired",
  "ID does not match provided name",
  "Invalid or unacceptable ID type",
  "Incomplete submission",
  "Suspected fake or tampered ID",
  "Others...",
];

export default function AdminDashboard() {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");

  const [allPets, setAllPets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("UNDER_REVIEW");
  const [showLogout, setShowLogout] = useState(false);

  const [users, setUsers] = useState([]);
  const [reports, setReports] = useState([]);
  const [usersLoading, setUsersLoading] = useState(false);
  const [reportsLoading, setReportsLoading] = useState(false);

  const [rejectModal, setRejectModal] = useState({ open: false, petId: null, petName: "" });
  const [selectedReasons, setSelectedReasons] = useState([]);
  const [customReason, setCustomReason] = useState("");
  const [rejecting, setRejecting] = useState(false);

  const [approveModal, setApproveModal] = useState({ open: false, petId: null, petName: "" });
  const [approving, setApproving] = useState(false);

  const [activeSection, setActiveSection] = useState("PETS");
  const [verifications, setVerifications] = useState([]);
  const [verifLoading, setVerifLoading] = useState(false);
  const [verifActionLoading, setVerifActionLoading] = useState(null);
  const [rejectVerifModal, setRejectVerifModal] = useState({ open: false, id: null, name: "" });
  const [rejectVerifReasons, setRejectVerifReasons] = useState([]);
  const [verifCustomReason, setVerifCustomReason] = useState("");
  const [rejectingVerif, setRejectingVerif] = useState(false);
  const [verifStatusFilter, setVerifStatusFilter] = useState("PENDING");
  
  const [viewVerifModal, setViewVerifModal] = useState({ open: false, data: null });
  const [banModal, setBanModal] = useState({ open: false, userId: null, userName: "" });
  const [banning, setBanning] = useState(false);

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

  const fetchVerifications = async () => {
    setVerifLoading(true);
    try {
      const res = await axios.get("http://localhost:8080/api/v1/verification/all", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setVerifications(res.data.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setVerifLoading(false);
    }
  };

  const fetchUsers = async () => {
  setUsersLoading(true);
  try {
    const res = await axios.get("http://localhost:8080/api/v1/users/all", {
      headers: { Authorization: `Bearer ${token}` },
    });
    setUsers(res.data.data || []);
  } catch (err) { console.error(err); }
  finally { setUsersLoading(false); }
};

const fetchReports = async () => {
  setReportsLoading(true);
  try {
    const res = await axios.get("http://localhost:8080/api/v1/reports/all", {
      headers: { Authorization: `Bearer ${token}` },
    });
    setReports(res.data.data || []);
  } catch (err) { console.error(err); }
  finally { setReportsLoading(false); }
};

const handleBanUser = async () => {
  setBanning(true);
  try {
    await axios.put(`http://localhost:8080/api/v1/users/${banModal.userId}/ban`, {}, {
      headers: { Authorization: `Bearer ${token}` },
    });
    setBanModal({ open: false, userId: null, userName: "" });
    await fetchUsers();
    if (activeSection === "REPORTS") await fetchReports();
  } catch {
    alert("Failed to ban user.");
  } finally {
    setBanning(false);
  }
};

  const handleApproveVerif = async (id) => {
    setVerifActionLoading(id);
    try {
      await axios.put(`http://localhost:8080/api/v1/verification/${id}/approve`, {}, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setViewVerifModal({ open: false, data: null });
      await fetchVerifications();
    } catch { alert("Failed to approve."); }
    finally { setVerifActionLoading(null); }
  };

  const handleRejectVerif = async () => {
    const reasons = [
      ...rejectVerifReasons.filter(r => r !== "Others..."),
      ...(rejectVerifReasons.includes("Others...") && verifCustomReason.trim() ? [verifCustomReason.trim()] : [])
    ];
    if (reasons.length === 0) { alert("Please select or enter a reason."); return; }
    setRejectingVerif(true);
    try {
      await axios.put(
        `http://localhost:8080/api/v1/verification/${rejectVerifModal.id}/reject`,
        { adminComment: reasons.join("; ") },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setRejectVerifModal({ open: false, id: null, name: "" });
      setViewVerifModal({ open: false, data: null });
      setRejectVerifReasons([]);
      setVerifCustomReason("");
      await fetchVerifications();
    } catch { alert("Failed to reject."); }
    finally { setRejectingVerif(false); }
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
    } catch {
      alert("Failed to approve. Please try again.");
    } finally {
      setApproving(false);
    }
  };

  const handleReject = async () => {
    const reasons = [
      ...selectedReasons.filter(r => r !== "Others..."),
      ...(selectedReasons.includes("Others...") && customReason.trim() ? [customReason.trim()] : [])
    ];
    if (reasons.length === 0) { alert("Please select or enter a reason."); return; }
    setRejecting(true);
    try {
      await axios.put(
        `http://localhost:8080/api/v1/pets/admin/${rejectModal.petId}/reject`,
        { reason: reasons.join("; ") },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setRejectModal({ open: false, petId: null, petName: "" });
      setSelectedReasons([]);
      setCustomReason("");
      await fetchAll();
    } catch {
      alert("Failed to reject. Please try again.");
    } finally {
      setRejecting(false);
    }
  };

  const filtered = filter === "ALL"
    ? [...allPets].sort((a, b) => {
        const order = { UNDER_REVIEW: 0, AVAILABLE: 1, REJECTED: 2 };
        return (order[a.status] ?? 3) - (order[b.status] ?? 3);
      })
    : allPets.filter((p) => p.status === filter);

  const counts = {
    ALL: allPets.length,
    UNDER_REVIEW: allPets.filter((p) => p.status === "UNDER_REVIEW").length,
    AVAILABLE: allPets.filter((p) => p.status === "AVAILABLE").length,
    REJECTED: allPets.filter((p) => p.status === "REJECTED").length,
  };

  const sortedVerifs = (list) => {
    const order = { PENDING: 0, APPROVED: 1, REJECTED: 2 };
    return [...list].sort((a, b) => (order[a.status] ?? 3) - (order[b.status] ?? 3));
  };

  const filteredVerifs = verifStatusFilter === "ALL"
    ? sortedVerifs(verifications)
    : verifications.filter((v) => v.status === verifStatusFilter);

  const verifCounts = {
    ALL: verifications.length,
    PENDING: verifications.filter(v => v.status === "PENDING").length,
    APPROVED: verifications.filter(v => v.status === "APPROVED").length,
    REJECTED: verifications.filter(v => v.status === "REJECTED").length,
  };

  const statusLabel = (s) =>
    s === "AVAILABLE" ? "Approved" : s === "UNDER_REVIEW" ? "Under Review"
    : s === "REJECTED" ? "Rejected" : s === "ADOPTED" ? "Adopted" : s;

  const statusClass = (s) =>
    s === "AVAILABLE" ? "badge-available" : s === "UNDER_REVIEW" ? "badge-review"
    : s === "REJECTED" ? "badge-rejected" : "badge-adopted";

  const toggleReason = (reason, list, setList) => {
    setList(prev => prev.includes(reason) ? prev.filter(r => r !== reason) : [...prev, reason]);
  };

  return (
    <div className="od-page">
      {showLogout && <LogoutModal onConfirm={confirmLogout} onCancel={() => setShowLogout(false)} />}
        {/* Ban User Modal */}
        {banModal.open && (
          <div className="od-modal-overlay">
            <div className="od-modal">
              <div className="od-modal-icon ad-icon-reject">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
                </svg>
              </div>
              <h3 className="od-modal-title">Ban User?</h3>
              <p className="od-modal-msg">
                Are you sure you want to ban <strong>{banModal.userName}</strong>? They will no longer be able to access PawPal.
              </p>
              <div className="od-modal-actions">
                <button className="od-modal-cancel" onClick={() => setBanModal({ open: false, userId: null, userName: "" })} disabled={banning}>Cancel</button>
                <button className="od-modal-confirm" style={{ background: "#ef4444" }} onClick={handleBanUser} disabled={banning}>
                  {banning ? "Banning..." : "Yes, Ban User"}
                </button>
              </div>
            </div>
          </div>
        )}
      {/* Approve Pet Modal */}
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
            </p>
            <div className="od-modal-actions">
              <button className="od-modal-cancel" onClick={() => setApproveModal({ open: false, petId: null, petName: "" })} disabled={approving}>Cancel</button>
              <button className="od-modal-confirm" style={{ background: "#16a34a" }} onClick={handleApprove} disabled={approving}>
                {approving ? "Approving..." : "Yes, Approve"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Reject Pet Modal */}
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
              Select one or more reasons for rejecting <strong>{rejectModal.petName}</strong>'s listing.
            </p>
            <div className="admin-reject-reasons">
              {REJECT_REASONS.map((r) => {
                const selected = selectedReasons.includes(r);
                return (
                  <button key={r} className={`admin-reason-btn ${selected ? "admin-reason-active" : ""}`}
                    onClick={() => toggleReason(r, selectedReasons, setSelectedReasons)}>
                    {selected && (
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 13, height: 13 }}><polyline points="20 6 9 17 4 12"/></svg>
                    )}
                    {r}
                  </button>
                );
              })}
            </div>
            {selectedReasons.includes("Others...") && (
              <textarea className="admin-custom-reason" placeholder="Enter your reason here..." value={customReason} onChange={(e) => setCustomReason(e.target.value)} maxLength={300} />
            )}
            <div className="od-modal-actions" style={{ marginTop: 20 }}>
              <button className="od-modal-cancel" onClick={() => { setRejectModal({ open: false, petId: null, petName: "" }); setSelectedReasons([]); setCustomReason(""); }} disabled={rejecting}>Cancel</button>
              <button className="od-modal-confirm" onClick={handleReject} disabled={rejecting}>
                {rejecting ? "Rejecting..." : "Reject Listing"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Reject Verification Modal */}
      {rejectVerifModal.open && (
        <div className="od-modal-overlay">
          <div className="od-modal admin-reject-modal">
            <div className="od-modal-icon ad-icon-reject">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
              </svg>
            </div>
            <h3 className="od-modal-title">Reject Verification</h3>
            <p className="od-modal-msg">
              Select one or more reasons for rejecting <strong>{rejectVerifModal.name}</strong>.
            </p>
            <div className="admin-reject-reasons">
              {REJECT_VERIF_REASONS.map((r) => {
                const selected = rejectVerifReasons.includes(r);
                return (
                  <button key={r} className={`admin-reason-btn ${selected ? "admin-reason-active" : ""}`}
                    onClick={() => toggleReason(r, rejectVerifReasons, setRejectVerifReasons)}>
                    {selected && (
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 13, height: 13 }}><polyline points="20 6 9 17 4 12"/></svg>
                    )}
                    {r}
                  </button>
                );
              })}
            </div>
            {rejectVerifReasons.includes("Others...") && (
              <textarea className="admin-custom-reason" placeholder="Enter your reason here..." value={verifCustomReason} onChange={(e) => setVerifCustomReason(e.target.value)} maxLength={300} />
            )}
            <div className="od-modal-actions" style={{ marginTop: 20 }}>
              <button className="od-modal-cancel" onClick={() => { setRejectVerifModal({ open: false, id: null, name: "" }); setRejectVerifReasons([]); setVerifCustomReason(""); }} disabled={rejectingVerif}>Cancel</button>
              <button className="od-modal-confirm" onClick={handleRejectVerif} disabled={rejectingVerif}>
                {rejectingVerif ? "Rejecting..." : "Reject"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* VIEW VERIFICATION DETAILS MODAL - UPDATED UI */}
      {viewVerifModal.open && viewVerifModal.data && (
        <div className="od-modal-overlay" onClick={() => setViewVerifModal({ open: false, data: null })}>
          <div className="od-modal" style={{ maxWidth: 700, padding: 30, width: "90%" }} onClick={(e) => e.stopPropagation()}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 24 }}>
              <div style={{ display: "flex", gap: 20, alignItems: "center" }}>
                
                {/* Profile Image */}
                {viewVerifModal.data.user?.profileImageUrl ? (
                  <img src={`http://localhost:8080${viewVerifModal.data.user.profileImageUrl}`} alt="" style={{ width: 80, height: 80, borderRadius: "50%", objectFit: "cover", border: "2px solid var(--border)" }} />
                ) : (
                  <div style={{ width: 80, height: 80, borderRadius: "50%", background: "var(--cream)", border: "2px solid var(--border)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 32 }}>👤</div>
                )}
                
                {/* User Details with Icons */}
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  
                  <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 2 }}>
                    <h3 style={{ margin: 0, fontSize: 20, color: "var(--dark)" }}>{viewVerifModal.data.user?.fullName}</h3>
                    <span style={{ fontSize: 11, fontWeight: 700, color: "white", background: viewVerifModal.data.user?.role === "ADOPTER" ? "var(--green)" : "var(--orange)", padding: "3px 10px", borderRadius: 100 }}>
                      {viewVerifModal.data.user?.role === "ADOPTER" ? "Adopter" : "Pet Owner"}
                    </span>
                  </div>

                  <div style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 14, color: "var(--muted)" }}>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 15, height: 15 }}><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                    {viewVerifModal.data.user?.email}
                  </div>
                  
                  <div style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 14, color: "var(--muted)" }}>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 15, height: 15 }}><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.4 2 2 0 0 1 3.6 1.22h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.8a16 16 0 0 0 5.29 5.29l.96-.96a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
                    {viewVerifModal.data.user?.phoneNumber || "No phone provided"}
                  </div>

                  <div style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 14, color: "var(--muted)" }}>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 15, height: 15 }}><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                    {viewVerifModal.data.user?.address || "Location not provided"}
                  </div>
                  
                </div>
              </div>
              <button onClick={() => setViewVerifModal({ open: false, data: null })} style={{ background: "none", border: "none", cursor: "pointer", color: "var(--muted)", fontSize: 24 }}>✕</button>
            </div>

            <div style={{ background: "var(--cream)", border: "1px solid var(--border)", borderRadius: 10, padding: "14px", fontSize: 14, color: "var(--dark)", marginBottom: 16 }}>
              <strong>Reason for joining:</strong> {viewVerifModal.data.reason || "None provided"}
            </div>

            <div style={{ fontSize: 13, fontWeight: 600, color: "var(--muted)", marginBottom: 8 }}>Attached ID Document</div>
            
            <div style={{ background: "#f8f9fa", border: "1px solid var(--border)", borderRadius: 10, padding: 10, textAlign: "center" }}>
              <img 
                src={`http://localhost:8080${viewVerifModal.data.idImageUrl}`} 
                alt="ID Document" 
                style={{ width: "100%", maxHeight: "50vh", objectFit: "contain", borderRadius: 6 }} 
              />
            </div>

            {viewVerifModal.data.status === "PENDING" && (
              <div style={{ display: "flex", gap: 12, marginTop: 24 }}>
                <button 
                  className="admin-btn-reject" 
                  style={{ flex: 1, height: 48, fontSize: 14 }} 
                  onClick={() => { setViewVerifModal({ open: false, data: null }); setRejectVerifModal({ open: true, id: viewVerifModal.data.id, name: viewVerifModal.data.user?.fullName }); }}
                >
                  ✕ Reject Verification
                </button>
                <button 
                  className="admin-btn-approve" 
                  style={{ flex: 1, height: 48, fontSize: 14 }} 
                  onClick={() => handleApproveVerif(viewVerifModal.data.id)} 
                  disabled={verifActionLoading === viewVerifModal.data.id}
                >
                  {verifActionLoading === viewVerifModal.data.id ? "Approving..." : "✓ Approve Verification"}
                </button>
              </div>
            )}
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
            <h1 className="od-title">
              {activeSection === "PETS" ? "Listing Review Dashboard" 
                : activeSection === "VERIFICATIONS" ? "User Verification Requests"
                : activeSection === "USERS" ? "Manage Users"
                : "Reports"}
            </h1>
            <p className="od-subtitle">
              {activeSection === "PETS" ? "Review and manage pet listing submissions from owners."
                : activeSection === "VERIFICATIONS" ? "Approve or reject identity verification requests from users."
                : activeSection === "USERS" ? "View and manage all registered users."
                : "View and act on user reports."}
            </p>
          </div>
        </div>

        {activeSection === "PETS" && (
          <div className="od-stats" style={{ gridTemplateColumns: "repeat(4, 1fr)" }}>
            {[
              { key: "UNDER_REVIEW", label: "Pending Review", iconCls: "od-stat-pending", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg> },
              { key: "AVAILABLE", label: "Approved", iconCls: "od-stat-available", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg> },
              { key: "REJECTED", label: "Rejected", iconCls: "od-stat-rejected", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg> },
              { key: "ALL", label: "Total Listings", iconCls: "od-stat-total", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg> },
            ].map(({ key, label, iconCls, icon }) => (
              <div key={key} className="od-stat-card" onClick={() => setFilter(key)}
                style={{ cursor: "pointer", border: filter === key ? "1.5px solid var(--green)" : "1px solid var(--border)", background: filter === key ? "var(--orange)" : "white", transition: "all 0.2s" }}>
                <div className={`od-stat-icon ${iconCls}`} style={filter === key ? { background: "rgba(255,255,255,0.15)" } : {}}>{icon}</div>
                <div>
                  <div className="od-stat-label" style={filter === key ? { color: "rgba(255,255,255,0.7)" } : {}}>{label}</div>
                  <div className="od-stat-num" style={filter === key ? { color: "white" } : {}}>{counts[key]}</div>
                </div>
              </div>
            ))}
          </div>
        )}

        {activeSection === "VERIFICATIONS" && (
          <div className="od-stats" style={{ gridTemplateColumns: "repeat(4, 1fr)" }}>
            {[
              { key: "ALL", label: "Total Requests", iconCls: "od-stat-total", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg> },
              { key: "PENDING", label: "Pending", iconCls: "od-stat-pending", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg> },
              { key: "APPROVED", label: "Approved", iconCls: "od-stat-available", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg> },
              { key: "REJECTED", label: "Rejected", iconCls: "od-stat-rejected", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg> },
            ].map(({ key, label, iconCls, icon }) => (
              <div key={key} className="od-stat-card" onClick={() => setVerifStatusFilter(key)}
                style={{ cursor: "pointer", border: verifStatusFilter === key ? "1.5px solid var(--green)" : "1px solid var(--border)", background: verifStatusFilter === key ? "var(--orange)" : "white", transition: "all 0.2s" }}>
                <div className={`od-stat-icon ${iconCls}`} style={verifStatusFilter === key ? { background: "rgba(255,255,255,0.15)" } : {}}>{icon}</div>
                <div>
                  <div className="od-stat-label" style={verifStatusFilter === key ? { color: "rgba(255,255,255,0.7)" } : {}}>{label}</div>
                  <div className="od-stat-num" style={verifStatusFilter === key ? { color: "white" } : {}}>{verifCounts[key]}</div>
                </div>
              </div>
            ))}
          </div>
        )}

        <div style={{ display: "flex", gap: 10, marginBottom: 16, marginTop: 8 }}>
          <button className={`ad-filter-tab ${activeSection === "PETS" ? "ad-tab-active" : ""}`} onClick={() => setActiveSection("PETS")}>
            🐾 Pet Listings
          </button>
          <button
            className={`ad-filter-tab ${activeSection === "VERIFICATIONS" ? "ad-tab-active" : ""}`}
            onClick={() => { setActiveSection("VERIFICATIONS"); fetchVerifications(); }}
          >
            🪪 Verifications
            {verifications.filter(v => v.status === "PENDING").length > 0 && (
              <span style={{ marginLeft: 6, fontSize: 11, fontWeight: 700, background: activeSection === "VERIFICATIONS" ? "rgba(255,255,255,0.25)" : "var(--border)", color: activeSection === "VERIFICATIONS" ? "white" : "var(--muted)", padding: "1px 7px", borderRadius: 100 }}>
                {verifications.filter(v => v.status === "PENDING").length}
              </span>
            )}
          </button>

          <button
            className={`ad-filter-tab ${activeSection === "USERS" ? "ad-tab-active" : ""}`}
            onClick={() => { setActiveSection("USERS"); fetchUsers(); }}
          >
            👥 Users
          </button>
          <button
            className={`ad-filter-tab ${activeSection === "REPORTS" ? "ad-tab-active" : ""}`}
            onClick={() => { setActiveSection("REPORTS"); fetchReports(); }}
          >
            🚩 Reports
            {reports.filter(r => r.status === "PENDING").length > 0 && (
              <span style={{ marginLeft: 6, fontSize: 11, fontWeight: 700, background: "var(--border)", color: "var(--muted)", padding: "1px 7px", borderRadius: 100 }}>
                {reports.filter(r => r.status === "PENDING").length}
              </span>
            )}
          </button>
        </div>

        {activeSection === "PETS" && (
          <>
            <div className="ad-filter-tabs" style={{ marginBottom: 24 }}>
              {["ALL", "UNDER_REVIEW", "AVAILABLE", "REJECTED"].map((t) => (
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
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                          {pet.owner?.fullName || "Unknown Owner"}
                        </div>
                        <div className="admin-detail-chip">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                          {pet.location}
                        </div>
                        {pet.createdAt && (
                          <div className="admin-detail-chip">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
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
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                          Reject
                        </button>
                        <button className="admin-btn-approve" onClick={() => setApproveModal({ open: true, petId: pet.id, petName: pet.name })}>
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                          Approve
                        </button>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        {activeSection === "VERIFICATIONS" && (
          <>
            <div className="ad-filter-tabs" style={{ marginBottom: 24 }}>
              {["ALL", "PENDING", "APPROVED", "REJECTED"].map((t) => (
                <button key={t} className={`ad-filter-tab ${verifStatusFilter === t ? "ad-tab-active" : ""}`}
                  onClick={() => setVerifStatusFilter(t)}>
                  {t === "ALL" ? "All" : t.charAt(0) + t.slice(1).toLowerCase()}
                  {verifCounts[t] > 0 && (
                    <span style={{ marginLeft: 6, fontSize: 11, fontWeight: 700, background: verifStatusFilter === t ? "rgba(255,255,255,0.25)" : "var(--border)", color: verifStatusFilter === t ? "white" : "var(--muted)", padding: "1px 7px", borderRadius: 100 }}>
                      {verifCounts[t]}
                    </span>
                  )}
                </button>
              ))}
            </div>

            {verifLoading ? (
              <div className="od-loading"><div className="od-spinner" /><p>Loading verifications...</p></div>
            ) : filteredVerifs.length === 0 ? (
              <div className="od-empty">
                <div className="od-empty-icon">🪪</div>
                <h3>No verification requests</h3>
                <p>Nothing to review right now.</p>
              </div>
            ) : (
              <div className="admin-listings-list">
                {filteredVerifs.map((v) => {
                  return (
                    <div key={v.id} className="admin-listing-card" style={{ flexDirection: "column", padding: 20, gap: 0 }}>
                      <div
                        style={{ display: "flex", alignItems: "center", gap: 14, cursor: "pointer" }}
                        onClick={() => setViewVerifModal({ open: true, data: v })}
                      >
                        {v.user?.profileImageUrl ? (
                          <img src={`http://localhost:8080${v.user.profileImageUrl}`} alt="" style={{ width: 52, height: 52, borderRadius: "50%", objectFit: "cover", border: "2px solid var(--border)", flexShrink: 0 }} />
                        ) : (
                          <div style={{ width: 52, height: 52, borderRadius: "50%", background: "var(--cream)", border: "2px solid var(--border)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 22, flexShrink: 0 }}>👤</div>
                        )}
                        <div style={{ flex: 1 }}>
                          <div style={{ fontWeight: 700, fontSize: 16, color: "var(--dark)" }}>{v.user?.fullName}</div>
                          <div style={{ fontSize: 13, color: "var(--muted)" }}>{v.user?.email}</div>
                          <div style={{ display: "flex", alignItems: "center", gap: 6, marginTop: 3 }}>
                            <span style={{ fontSize: 11, fontWeight: 700, color: "white", background: v.user?.role === "ADOPTER" ? "var(--green)" : "var(--orange)", padding: "2px 8px", borderRadius: 100 }}>
                              {v.user?.role === "ADOPTER" ? "Adopter" : "Pet Owner"}
                            </span>
                          </div>
                        </div>
                        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                          <span className={`od-badge ${v.status === "APPROVED" ? "badge-available" : v.status === "REJECTED" ? "badge-rejected" : "badge-review"}`} style={{ position: "static" }}>
                            {v.status}
                          </span>
                          <span style={{ fontSize: 12, color: "var(--muted)", textDecoration: "underline", marginLeft: 4 }}>
                            View details
                          </span>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </>
        )}
        {activeSection === "USERS" && (
  <div className="admin-listings-list">
    {usersLoading ? (
      <div className="od-loading"><div className="od-spinner" /><p>Loading users...</p></div>
    ) : users.length === 0 ? (
      <div className="od-empty"><div className="od-empty-icon">👥</div><h3>No users found</h3></div>
    ) : users.map((u) => (
      <div key={u.id} className="admin-listing-card" style={{ padding: 16, alignItems: "center" }}>
        <div style={{ flex: 1 }}>
          <div style={{ fontWeight: 700, fontSize: 15, color: "var(--dark)" }}>{u.fullName}</div>
          <div style={{ fontSize: 13, color: "var(--muted)" }}>{u.email}</div>
          <div style={{ display: "flex", gap: 8, marginTop: 4 }}>
            <span style={{ fontSize: 11, fontWeight: 700, color: "white", background: u.role === "ADOPTER" ? "var(--green)" : "var(--orange)", padding: "2px 8px", borderRadius: 100 }}>{u.role}</span>
              {u.isBanned && <span style={{ fontSize: 11, fontWeight: 700, color: "white", background: "#ef4444", padding: "2px 8px", borderRadius: 100 }}>BANNED</span>}          </div>
        </div>
        {!u.isBanned && u.role !== "ADMIN" && (
          <button onClick={() => setBanModal({ open: true, userId: u.id, userName: u.fullName })}
            style={{ background: "#ef4444", color: "white", border: "none", borderRadius: 8, padding: "8px 16px", cursor: "pointer", fontWeight: 600, fontSize: 13 }}>
            Ban User
          </button>
        )}
        {u.isBanned && <span style={{ fontSize: 11, fontWeight: 700, color: "white", background: "#ef4444", padding: "2px 8px", borderRadius: 100 }}>BANNED</span>}
      </div>
    ))}
  </div>
)}
        {activeSection === "REPORTS" && (
          <div className="admin-listings-list">
            {reportsLoading ? (
              <div className="od-loading"><div className="od-spinner" /><p>Loading reports...</p></div>
            ) : reports.length === 0 ? (
              <div className="od-empty"><div className="od-empty-icon">🚩</div><h3>No reports yet</h3></div>
            ) : reports.map((r) => (
              <div key={r.id} className="admin-listing-card" style={{ padding: 16, flexDirection: "column", gap: 8 }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <div style={{ fontWeight: 700, fontSize: 15, color: "var(--dark)" }}>Report #{r.id}</div>
                  <span style={{ fontSize: 11, fontWeight: 700, color: "white", background: r.reportedUserBanned ? "#ef4444" : "#f97316", padding: "2px 8px", borderRadius: 100 }}>
                    {r.reportedUserBanned ? "BANNED" : "PENDING"}
                  </span>
                </div>
                <div style={{ fontSize: 13, color: "var(--muted)" }}><strong>Reporter:</strong> {r.reporterName} ({r.reporterEmail})</div>
                <div style={{ fontSize: 13, color: "var(--muted)" }}><strong>Reported User:</strong> {r.reportedUserName} ({r.reportedUserEmail})</div>
                <div style={{ fontSize: 13, color: "var(--dark)", background: "var(--cream)", padding: "8px 12px", borderRadius: 8 }}>
                  <strong>Reason:</strong> {r.reason}
                </div>
                {!r.reportedUserBanned && (
                  <button onClick={() => setBanModal({ open: true, userId: r.reportedUserId, userName: r.reportedUserName })}
                    style={{ alignSelf: "flex-end", background: "#ef4444", color: "white", border: "none", borderRadius: 8, padding: "8px 16px", cursor: "pointer", fontWeight: 600, fontSize: 13 }}>
                    Ban Reported User
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}