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
  const [reportModalView, setReportModalView] = useState("report");
  const [userFilter, setUserFilter] = useState("ALL");
  const [reportFilter, setReportFilter] = useState("ALL");
  const [reportDetailModal, setReportDetailModal] = useState(null);
  const [petDetailModal, setPetDetailModal] = useState(null);
  const [reportModalParent, setReportModalParent] = useState(null);

  useEffect(() => { fetchAll(); }, []);

  const fetchAll = async () => {
    try {
      const res = await axios.get("https://it342-catamco-pawpal-production.up.railway.app/api/v1/pets/admin/all", {
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
      const res = await axios.get("https://it342-catamco-pawpal-production.up.railway.app/api/v1/verification/all", {
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
    const res = await axios.get("https://it342-catamco-pawpal-production.up.railway.app/api/v1/users/all", {
      headers: { Authorization: `Bearer ${token}` },
    });
    setUsers(res.data.data || []);
  } catch (err) { console.error(err); }
  finally { setUsersLoading(false); }
};

const fetchReports = async () => {
  setReportsLoading(true);
  try {
    const res = await axios.get("https://it342-catamco-pawpal-production.up.railway.app/api/v1/reports/all", {
      headers: { Authorization: `Bearer ${token}` },
    });
    setReports(res.data.data || []);
  } catch (err) { console.error(err); }
  finally { setReportsLoading(false); }
};

const handleBanUser = async () => {
  setBanning(true);
  try {
    await axios.put(`https://it342-catamco-pawpal-production.up.railway.app/api/v1/users/${banModal.userId}/ban`, {}, {
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
      await axios.put(`https://it342-catamco-pawpal-production.up.railway.app/api/v1/verification/${id}/approve`, {}, {
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
        `https://it342-catamco-pawpal-production.up.railway.app/api/v1/verification/${rejectVerifModal.id}/reject`,
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
        `https://it342-catamco-pawpal-production.up.railway.app/api/v1/pets/admin/${approveModal.petId}/approve`,
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
        `https://it342-catamco-pawpal-production.up.railway.app/api/v1/pets/admin/${rejectModal.petId}/reject`,
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

  const UserAvatar = ({ name, imageUrl, size = 48 }) => {
        const initials = name ? name.split(" ").map(w => w[0]).slice(0, 2).join("").toUpperCase() : "?";
        const colors = ["#7c6f64","#927b6e","#6b7f8c","#7a8c6b","#8c6b7a"];
        const color = colors[name ? name.charCodeAt(0) % colors.length : 0];
        return imageUrl && imageUrl !== "" ? (
          <img
            src={`https://it342-catamco-pawpal-production.up.railway.app${imageUrl}`}
            alt={name}
            style={{ width: size, height: size, borderRadius: "50%", objectFit: "cover", flexShrink: 0, border: "2px solid var(--border)" }}
          />
        ) : (
          <div style={{
            width: size, height: size, borderRadius: "50%", background: color,
            display: "flex", alignItems: "center", justifyContent: "center",
            fontSize: size * 0.36, fontWeight: 700, color: "white", flexShrink: 0,
            border: "2px solid var(--border)", letterSpacing: 0.5
          }}>
            {initials}
          </div>
        );
      };

      return (
        <div className="od-page">
    {showLogout && <LogoutModal onConfirm={confirmLogout} onCancel={() => setShowLogout(false)} />}

          {petDetailModal && (
            <div className="od-modal-overlay" onClick={() => setPetDetailModal(null)}>
              <div
                className="od-modal"
                style={{ maxWidth: 620, width: "92%", padding: 0, overflow: "hidden", borderRadius: 16 }}
                onClick={e => e.stopPropagation()}
              >
                <div style={{ padding: "22px 28px 18px", borderBottom: "1px solid var(--border)", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <div style={{ width: 80 }}>
                    {reportModalParent && (
                      <button
                        onClick={() => {
                          setReportDetailModal(reportModalParent);
                          setReportModalParent(null);
                          setPetDetailModal(null);
                        }}
                        style={{ background: "none", border: "1px solid var(--border)", borderRadius: 8, cursor: "pointer", color: "var(--dark)", fontSize: 13, fontWeight: 600, padding: "5px 12px", display: "flex", alignItems: "center", gap: 5 }}
                      >
                        ← Back
                      </button>
                    )}
                  </div>
                  <div style={{ textAlign: "center" }}>
                    <div style={{ fontSize: 11, fontWeight: 700, color: "var(--muted)", textTransform: "uppercase", letterSpacing: 1, marginBottom: 4 }}>Pet Listing</div>
                    <h3 style={{ margin: 0, fontSize: 18, color: "var(--dark)", fontFamily: "'Playfair Display', serif" }}>{petDetailModal.name}</h3>
                  </div>
                  <div style={{ width: 80, display: "flex", justifyContent: "flex-end" }}>
                    <button onClick={() => { setPetDetailModal(null); setReportModalParent(null); }} style={{ background: "none", border: "none", cursor: "pointer", color: "var(--muted)", fontSize: 22, lineHeight: 1 }}>✕</button>
                  </div>
                </div>

                <div style={{ padding: "22px 28px", maxHeight: "78vh", overflowY: "auto" }}>
                  {petDetailModal.imageUrl ? (
                    <img
                      src={`https://it342-catamco-pawpal-production.up.railway.app${petDetailModal.imageUrl}`}
                      alt={petDetailModal.name}
                      style={{ width: "100%", maxHeight: 240, objectFit: "cover", borderRadius: 12, marginBottom: 20 }}
                    />
                  ) : (
                    <div style={{ width: "100%", height: 140, background: "var(--cream)", border: "1px solid var(--border)", borderRadius: 12, display: "flex", alignItems: "center", justifyContent: "center", marginBottom: 20 }}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="var(--muted)" strokeWidth="1.2" style={{ width: 52, height: 52 }}><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/></svg>
                    </div>
                  )}

                  <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10, marginBottom: 16 }}>
                    {[
                      { label: "Type", value: petDetailModal.type },
                      { label: "Breed", value: petDetailModal.breed || "—" },
                      { label: "Age", value: petDetailModal.age },
                      { label: "Gender", value: petDetailModal.gender || "—" },
                      { label: "Location", value: petDetailModal.location },
                      { label: "Status", value: petDetailModal.status ? petDetailModal.status.replace("_", " ") : "—" },
                    ].map(({ label, value }) => (
                      <div key={label} style={{ background: "var(--cream)", border: "1px solid var(--border)", borderRadius: 10, padding: "10px 14px" }}>
                        <div style={{ fontSize: 10, fontWeight: 700, color: "var(--muted)", textTransform: "uppercase", letterSpacing: 0.8, marginBottom: 3 }}>{label}</div>
                        <div style={{ fontSize: 14, fontWeight: 600, color: "var(--dark)" }}>{value}</div>
                      </div>
                    ))}
                  </div>

                  {petDetailModal.description && (
                    <div style={{ background: "var(--cream)", border: "1px solid var(--border)", borderRadius: 10, padding: "12px 16px", marginBottom: 16 }}>
                      <div style={{ fontSize: 10, fontWeight: 700, color: "var(--muted)", textTransform: "uppercase", letterSpacing: 0.8, marginBottom: 6 }}>Description</div>
                      <div style={{ fontSize: 14, color: "var(--dark)", lineHeight: 1.6 }}>{petDetailModal.description}</div>
                    </div>
                  )}

                  {petDetailModal.characteristics && petDetailModal.characteristics.length > 0 && (
                    <div style={{ marginBottom: 16 }}>
                      <div style={{ fontSize: 10, fontWeight: 700, color: "var(--muted)", textTransform: "uppercase", letterSpacing: 0.8, marginBottom: 8 }}>Characteristics</div>
                      <div style={{ display: "flex", flexWrap: "wrap", gap: 6 }}>
                        {petDetailModal.characteristics.map(c => (
                          <span key={c} style={{ fontSize: 12, fontWeight: 600, background: "var(--cream)", border: "1px solid var(--border)", borderRadius: 100, padding: "3px 12px", color: "var(--dark)" }}>{c}</span>
                        ))}
                      </div>
                    </div>
                  )}

                  <div style={{ marginBottom: 16 }}>
                    <div style={{ fontSize: 10, fontWeight: 700, color: "var(--muted)", textTransform: "uppercase", letterSpacing: 0.8, marginBottom: 8 }}>Health & Care</div>
                    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8 }}>
                      {[
                        { label: "Vaccinated", value: petDetailModal.vaccinated },
                        { label: "Neutered", value: petDetailModal.neutered },
                        { label: "Microchipped", value: petDetailModal.microchipped },
                        { label: "Health Checked", value: petDetailModal.healthChecked },
                      ].map(({ label, value }) => (
                        <div key={label} style={{ display: "flex", alignItems: "center", gap: 8, background: "var(--cream)", border: "1px solid var(--border)", borderRadius: 10, padding: "9px 14px" }}>
                          <span style={{ display: "flex", alignItems: "center", justifyContent: "center", width: 18, height: 18, borderRadius: "50%", background: value ? "#dcfce7" : "#fee2e2", flexShrink: 0 }}>
                              {value
                                ? <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 11, height: 11 }}><polyline points="20 6 9 17 4 12"/></svg>
                                : <svg viewBox="0 0 24 24" fill="none" stroke="#dc2626" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 11, height: 11 }}><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                              }
                            </span>
                          <span style={{ fontSize: 13, color: "var(--dark)", fontWeight: 500 }}>{label}</span>
                        </div>
                      ))}
                    </div>
                  </div>

                  {petDetailModal.createdAt && (
                    <div style={{ fontSize: 12, color: "var(--muted)" }}>
                      Listed on {new Date(petDetailModal.createdAt).toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" })}
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
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
                  <img src={`https://it342-catamco-pawpal-production.up.railway.app${viewVerifModal.data.user.profileImageUrl}`} alt="" style={{ width: 80, height: 80, borderRadius: "50%", objectFit: "cover", border: "2px solid var(--border)" }} />
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
                src={`https://it342-catamco-pawpal-production.up.railway.app${viewVerifModal.data.idImageUrl}`} 
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
                style={{ cursor: "pointer", border: filter === key ? "1.5px solid var(--orange)" : "1px solid var(--border)", background: filter === key ? "var(--orange)" : "white", transition: "all 0.2s" }}>                <div className={`od-stat-icon ${iconCls}`} style={filter === key ? { background: "rgba(255,255,255,0.15)" } : {}}>{icon}</div>
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
                style={{ cursor: "pointer", border: verifStatusFilter === key ? "1.5px solid var(--orange)" : "1px solid var(--border)", background: verifStatusFilter === key ? "var(--orange)" : "white", transition: "all 0.2s" }}>
                <div className={`od-stat-icon ${iconCls}`} style={verifStatusFilter === key ? { background: "rgba(255,255,255,0.15)" } : {}}>{icon}</div>
                <div>
                  <div className="od-stat-label" style={verifStatusFilter === key ? { color: "rgba(255,255,255,0.7)" } : {}}>{label}</div>
                  <div className="od-stat-num" style={verifStatusFilter === key ? { color: "white" } : {}}>{verifCounts[key]}</div>
                </div>
              </div>
            ))}
          </div>
        )}

        {activeSection === "USERS" && (
          <div className="od-stats" style={{ gridTemplateColumns: "repeat(4, 1fr)" }}>
            {[
              { key: "ALL", label: "Total Users", count: users.length, iconCls: "od-stat-total", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg> },
              { key: "ADOPTER", label: "Adopters", count: users.filter(u => u.role === "ADOPTER").length, iconCls: "od-stat-available", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg> },
              { key: "PET_OWNER", label: "Pet Owners", count: users.filter(u => u.role === "PET_OWNER").length, iconCls: "od-stat-pending", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg> },
              { key: "BANNED", label: "Banned", count: users.filter(u => u.isBanned).length, iconCls: "od-stat-rejected", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/></svg> },
            ].map(({ key, label, count, iconCls, icon }) => (
              <div key={key} className="od-stat-card" onClick={() => setUserFilter(key)}
                style={{ cursor: "pointer", border: userFilter === key ? "1.5px solid var(--orange)" : "1px solid var(--border)", background: userFilter === key ? "var(--orange)" : "white", transition: "all 0.2s" }}>
                <div className={`od-stat-icon ${iconCls}`} style={userFilter === key ? { background: "rgba(255,255,255,0.15)" } : {}}>{icon}</div>
                <div>
                  <div className="od-stat-label" style={userFilter === key ? { color: "rgba(255,255,255,0.7)" } : {}}>{label}</div>
                  <div className="od-stat-num" style={userFilter === key ? { color: "white" } : {}}>{count}</div>
                </div>
              </div>
            ))}
          </div>
        )}

        {activeSection === "REPORTS" && (
          <div className="od-stats" style={{ gridTemplateColumns: "repeat(3, 1fr)" }}>
            {[
              { key: "ALL", label: "Total Reports", count: reports.length, iconCls: "od-stat-total", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/><line x1="4" y1="22" x2="4" y2="15"/></svg> },
              { key: "PENDING", label: "Pending", count: reports.filter(r => r.status === "PENDING" && !r.reportedUserBanned).length, iconCls: "od-stat-pending", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg> },
              { key: "RESOLVED", label: "Resolved", count: reports.filter(r => r.status === "RESOLVED" || r.reportedUserBanned).length, iconCls: "od-stat-available", icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg> },
            ].map(({ key, label, count, iconCls, icon }) => (
              <div key={key} className="od-stat-card" onClick={() => setReportFilter(key)}
                style={{ cursor: "pointer", border: reportFilter === key ? "1.5px solid var(--orange)" : "1px solid var(--border)", background: reportFilter === key ? "var(--orange)" : "white", transition: "all 0.2s" }}>
                <div className={`od-stat-icon ${iconCls}`} style={reportFilter === key ? { background: "rgba(255,255,255,0.15)" } : {}}>{icon}</div>
                <div>
                  <div className="od-stat-label" style={reportFilter === key ? { color: "rgba(255,255,255,0.7)" } : {}}>{label}</div>
                  <div className="od-stat-num" style={reportFilter === key ? { color: "white" } : {}}>{count}</div>
                </div>
              </div>
            ))}
          </div>
        )}

        <div style={{ display: "flex", gap: 10, marginBottom: 16, marginTop: 8 }}>
          <button className={`ad-filter-tab ${activeSection === "PETS" ? "ad-tab-active" : ""}`} onClick={() => setActiveSection("PETS")}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 14, height: 14 }}>
              <path d="M10 5.172C10 3.782 8.423 2.679 6.5 3c-2.823.47-4.113 6.006-4 7 .08.703 1.725 1.722 3.656 1 1.261-.472 1.344-1.549 1.344-1.549"/><path d="M14.5 5.172c0-1.39 1.577-2.493 3.5-2.172 2.823.47 4.113 6.006 4 7-.08.703-1.725 1.722-3.656 1-1.261-.472-1.344-1.549-1.344-1.549"/><path d="M8 14v.5"/><path d="M16 14v.5"/><path d="M11.25 16.25h1.5L12 17l-.75-.75z"/><path d="M4.42 11.247A13.152 13.152 0 0 0 4 14.556C4 18.728 7.582 21 12 21s8-2.272 8-6.444c0-1.061-.162-2.2-.493-3.309m-9.243-6.082A8.801 8.801 0 0 1 12 5c.78 0 1.5.108 2.161.306"/>
            </svg>
            Pet Listings
          </button>
          <button
            className={`ad-filter-tab ${activeSection === "VERIFICATIONS" ? "ad-tab-active" : ""}`}
            onClick={() => { setActiveSection("VERIFICATIONS"); fetchVerifications(); }}
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 14, height: 14 }}>
              <rect x="2" y="5" width="20" height="14" rx="2"/><path d="M16 10h.01M2 10h13"/>
            </svg>
            Verifications
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
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 14, height: 14 }}>
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
            Users
          </button>
          <button
            className={`ad-filter-tab ${activeSection === "REPORTS" ? "ad-tab-active" : ""}`}
            onClick={() => { setActiveSection("REPORTS"); fetchReports(); }}
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 14, height: 14 }}>
              <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/><line x1="4" y1="22" x2="4" y2="15"/>
            </svg>
            Reports
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
                        <img src={`https://it342-catamco-pawpal-production.up.railway.app${pet.imageUrl}`} alt={pet.name} className="admin-listing-img" />
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
                          <img src={`https://it342-catamco-pawpal-production.up.railway.app${v.user.profileImageUrl}`} alt="" style={{ width: 52, height: 52, borderRadius: "50%", objectFit: "cover", border: "2px solid var(--border)", flexShrink: 0 }} />
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
  <>
          <div className="ad-filter-tabs" style={{ marginBottom: 24 }}>
            {[
              { key: "ALL", label: "All" },
              { key: "ADOPTER", label: "Adopters" },
              { key: "PET_OWNER", label: "Pet Owners" },
              { key: "BANNED", label: "Banned" },
            ].map(({ key, label }) => {
              const count = key === "ALL" ? users.length
                : key === "BANNED" ? users.filter(u => u.isBanned).length
                : users.filter(u => u.role === key).length;
              return (
                <button key={key} className={`ad-filter-tab ${userFilter === key ? "ad-tab-active" : ""}`}
                  onClick={() => setUserFilter(key)}>
                  {label}
                  {count > 0 && (
                    <span style={{ marginLeft: 6, fontSize: 11, fontWeight: 700, background: userFilter === key ? "rgba(255,255,255,0.25)" : "var(--border)", color: userFilter === key ? "white" : "var(--muted)", padding: "1px 7px", borderRadius: 100 }}>
                      {count}
                    </span>
                  )}
                </button>
              );
            })}
          </div>

          <div className="admin-listings-list">
            {usersLoading ? (
              <div className="od-loading"><div className="od-spinner" /><p>Loading users...</p></div>
            ) : (() => {
              const filteredUsers = userFilter === "ALL" ? users
                : userFilter === "BANNED" ? users.filter(u => u.isBanned)
                : users.filter(u => u.role === userFilter);
              return filteredUsers.length === 0 ? (
                <div className="od-empty">
                  <div className="od-empty-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 48, height: 48 }}>
                      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
                      <path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                    </svg>
                  </div>
                  <h3>No users found</h3>
                </div>
              ) : filteredUsers.map((u) => (
                <div key={u.id} className="admin-listing-card" style={{ padding: 16, alignItems: "center" }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 700, fontSize: 15, color: "var(--dark)" }}>{u.fullName}</div>
                    <div style={{ fontSize: 13, color: "var(--muted)" }}>{u.email}</div>
                    <div style={{ display: "flex", gap: 8, marginTop: 4 }}>
                      <span style={{ fontSize: 11, fontWeight: 700, color: "white", background: u.role === "ADOPTER" ? "var(--green)" : "var(--orange)", padding: "2px 8px", borderRadius: 100 }}>{u.role}</span>
                      {u.isBanned && <span style={{ fontSize: 11, fontWeight: 700, color: "white", background: "#ef4444", padding: "2px 8px", borderRadius: 100 }}>BANNED</span>}
                    </div>
                  </div>
                  {!u.isBanned && u.role !== "ADMIN" && (
                    <button onClick={() => setBanModal({ open: true, userId: u.id, userName: u.fullName })}
                      style={{ background: "#ef4444", color: "white", border: "none", borderRadius: 8, padding: "8px 16px", cursor: "pointer", fontWeight: 600, fontSize: 13 }}>
                      Ban User
                    </button>
                  )}
                  {u.isBanned && <span style={{ fontSize: 11, fontWeight: 700, color: "white", background: "#ef4444", padding: "2px 8px", borderRadius: 100 }}>BANNED</span>}
                </div>
              ));
            })()}
          </div>
        </>
      )}
        {activeSection === "REPORTS" && (
          <>
            {/* Report Detail Modal */}
            {reportDetailModal && (() => {
              const r = reportDetailModal;
              const isAdopterReport = !!r.adoptionRequest;
              return (
                <div className="od-modal-overlay" onClick={() => setReportDetailModal(null)}>
                  <div
                    className="od-modal"
                    style={{ maxWidth: 680, width: "92%", padding: 0, overflow: "hidden", borderRadius: 16 }}
                    onClick={(e) => e.stopPropagation()}
                  >
                    <div style={{
                      padding: "22px 28px 18px",
                      borderBottom: "1px solid var(--border)",
                      display: "flex", justifyContent: "space-between", alignItems: "center"
                    }}>
                      <div>
                        <div style={{ fontSize: 11, fontWeight: 700, color: "var(--muted)", textTransform: "uppercase", letterSpacing: 1, marginBottom: 4 }}>
                          Report #{r.id}
                        </div>
                        <h3 style={{ margin: 0, fontSize: 18, color: "var(--dark)", fontFamily: "'Playfair Display', serif" }}>
                          {isAdopterReport ? "Adopter Reported an Owner" : "Owner Reported an Adopter"}
                        </h3>
                      </div>
                      <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                        <span style={{
                          fontSize: 11, fontWeight: 700, color: "white", padding: "3px 10px", borderRadius: 100,
                          background: r.reportedUserBanned ? "#ef4444" : r.status === "RESOLVED" ? "#16a34a" : "#f97316"
                        }}>
                          {r.reportedUserBanned ? "BANNED" : r.status || "PENDING"}
                        </span>
                        <button onClick={() => setReportDetailModal(null)} style={{ background: "none", border: "none", cursor: "pointer", color: "var(--muted)", fontSize: 22, lineHeight: 1 }}>✕</button>
                      </div>
                    </div>

                    <div style={{ padding: "22px 28px", maxHeight: "75vh", overflowY: "auto" }}>
                      <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 18, background: "var(--cream)", border: "1px solid var(--border)", borderRadius: 14, padding: "16px 18px" }}>
                        <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", gap: 8, textAlign: "center" }}>
                          <UserAvatar name={r.reporterName} imageUrl={r.reporterProfileImage} size={52} />
                          <div>
                            <div style={{ fontSize: 11, fontWeight: 700, color: "var(--muted)", textTransform: "uppercase", letterSpacing: 0.8, marginBottom: 4 }}>Reporter</div>
                            <div style={{ fontSize: 13, fontWeight: 700, color: "var(--dark)", marginBottom: 2 }}>{r.reporterName}</div>
                            <div style={{ fontSize: 11, color: "var(--muted)", marginBottom: 6 }}>{r.reporterEmail}</div>
                            {r.reporterRole && (
                              <span style={{ fontSize: 10, fontWeight: 700, color: "white", background: r.reporterRole === "ADOPTER" ? "var(--green)" : "var(--orange)", padding: "2px 8px", borderRadius: 100 }}>
                                {r.reporterRole === "ADOPTER" ? "Adopter" : "Pet Owner"}
                              </span>
                            )}
                          </div>
                        </div>

                        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 4, flexShrink: 0 }}>
                          <svg viewBox="0 0 24 24" fill="none" stroke="var(--muted)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 18, height: 18 }}>
                            <line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/>
                          </svg>
                          <div style={{ fontSize: 10, fontWeight: 700, color: "var(--muted)", textTransform: "uppercase", letterSpacing: 0.8 }}>Reported</div>
                        </div>

                        <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", gap: 8, textAlign: "center" }}>
                          <UserAvatar name={r.reportedUserName} imageUrl={r.reportedUserProfileImage} size={52} />
                          <div>
                            <div style={{ fontSize: 11, fontWeight: 700, color: "var(--muted)", textTransform: "uppercase", letterSpacing: 0.8, marginBottom: 4 }}>Reported User</div>
                            <div style={{ fontSize: 13, fontWeight: 700, color: "var(--dark)", marginBottom: 2 }}>{r.reportedUserName}</div>
                            <div style={{ fontSize: 11, color: "var(--muted)", marginBottom: 6 }}>{r.reportedUserEmail}</div>
                            <div style={{ display: "flex", gap: 6, justifyContent: "center", flexWrap: "wrap" }}>
                              {r.reportedUserRole && (
                                <span style={{ fontSize: 10, fontWeight: 700, color: "white", background: r.reportedUserRole === "ADOPTER" ? "var(--green)" : "var(--orange)", padding: "2px 8px", borderRadius: 100 }}>
                                  {r.reportedUserRole === "ADOPTER" ? "Adopter" : "Pet Owner"}
                                </span>
                              )}
                              {r.reportedUserBanned && (
                                <span style={{ fontSize: 10, fontWeight: 700, color: "white", background: "#ef4444", padding: "2px 8px", borderRadius: 100 }}>BANNED</span>
                              )}
                            </div>
                          </div>
                        </div>
                      </div>

                      <div style={{ background: "rgba(220,38,38,0.04)", border: "1px solid rgba(220,38,38,0.15)", borderRadius: 12, padding: "14px 16px", marginBottom: 18 }}>
                        <div style={{ fontSize: 11, fontWeight: 700, color: "#dc2626", textTransform: "uppercase", letterSpacing: 0.8, marginBottom: 6 }}>Reason for Report</div>
                        <div style={{ fontSize: 14, color: "var(--dark)", lineHeight: 1.6 }}>{r.reason}</div>
                      </div>

                      {r.adoptionRequest && (
                        <div style={{ marginBottom: 18 }}>
                          <div style={{ fontSize: 11, fontWeight: 700, color: "var(--muted)", textTransform: "uppercase", letterSpacing: 0.8, marginBottom: 10 }}>Adoption Request Form</div>
                          <div style={{ background: "white", border: "1px solid var(--border)", borderRadius: 12, overflow: "hidden" }}>
                            {[
                              { label: "Adopter Name", value: r.adoptionRequest.adopterName },
                              { label: "Contact Info", value: r.adoptionRequest.contactInfo },
                              { label: "Reason for Adopting", value: r.adoptionRequest.reason },
                              { label: "Note to Owner", value: r.adoptionRequest.noteToOwner },
                            ].filter(f => f.value).map(({ label, value }, i, arr) => (
                              <div key={label} style={{
                                display: "grid", gridTemplateColumns: "140px 1fr",
                                borderBottom: i < arr.length - 1 ? "1px solid var(--border)" : "none",
                                fontSize: 13
                              }}>
                                <div style={{ padding: "11px 14px", fontWeight: 600, color: "var(--muted)", background: "var(--cream)", borderRight: "1px solid var(--border)" }}>{label}</div>
                                <div style={{ padding: "11px 14px", color: "var(--dark)", lineHeight: 1.5 }}>{value}</div>
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      {r.reportedUserPets && r.reportedUserPets.length > 0 && (
                          <div style={{ marginBottom: 18 }}>
                            <div style={{ fontSize: 11, fontWeight: 700, color: "var(--muted)", textTransform: "uppercase", letterSpacing: 0.8, marginBottom: 10 }}>
                              Report Details
                            </div>
                            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
                              {r.reportedUserPets.map(pet => (
                                <div
                                  key={pet.id}
                                  onClick={() => {
                                    setReportModalParent(reportDetailModal);
                                    setReportDetailModal(null);
                                    setPetDetailModal(pet);
                                  }}
                                  style={{ background: "white", border: "1px solid var(--border)", borderRadius: 12, display: "flex", gap: 14, padding: 14, alignItems: "center", cursor: "pointer", transition: "box-shadow 0.15s" }}
                                  onMouseEnter={e => e.currentTarget.style.boxShadow = "0 2px 12px rgba(0,0,0,0.09)"}
                                  onMouseLeave={e => e.currentTarget.style.boxShadow = "none"}
                                >
                                  {pet.imageUrl ? (
                                    <img src={`https://it342-catamco-pawpal-production.up.railway.app${pet.imageUrl}`} alt="" style={{ width: 72, height: 72, borderRadius: 8, objectFit: "cover", flexShrink: 0 }} />
                                  ) : (
                                    <div style={{ width: 72, height: 72, borderRadius: 8, background: "var(--cream)", border: "1px solid var(--border)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
                                      <svg viewBox="0 0 24 24" fill="none" stroke="var(--muted)" strokeWidth="1.5" style={{ width: 28, height: 28 }}><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/></svg>
                                    </div>
                                  )}
                                  <div style={{ flex: 1 }}>
                                    <div style={{ fontWeight: 700, fontSize: 15, color: "var(--dark)", marginBottom: 2 }}>{pet.name}</div>
                                    <div style={{ fontSize: 13, color: "var(--muted)", marginBottom: 6 }}>
                                      {pet.type}{pet.breed ? ` · ${pet.breed}` : ""} · {pet.age}
                                    </div>
                                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                                      <span style={{
                                        fontSize: 11, fontWeight: 700, color: "white", padding: "2px 8px", borderRadius: 100,
                                        background: pet.status === "AVAILABLE" ? "#16a34a" : pet.status === "UNDER_REVIEW" ? "#f97316" : "#6b7280"
                                      }}>
                                        {pet.status.replace("_", " ")}
                                      </span>
                                      <span style={{ fontSize: 12, color: "var(--muted)", display: "flex", alignItems: "center", gap: 3 }}>
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 12, height: 12, flexShrink: 0 }}><path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                                        {pet.location}
                                      </span>
                                    </div>
                                  </div>
                                  <div style={{ fontSize: 12, color: "var(--muted)", display: "flex", alignItems: "center", gap: 4, flexShrink: 0 }}>
                                    View listing
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 13, height: 13 }}><polyline points="9 18 15 12 9 6"/></svg>
                                  </div>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}

                      <div style={{ fontSize: 12, color: "var(--muted)", marginBottom: 20 }}>
                        Submitted {r.createdAt ? new Date(r.createdAt).toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" }) : "—"}
                      </div>

                      {!r.reportedUserBanned && (
                        <div style={{ display: "flex", gap: 10 }}>
                          <button
                            onClick={async () => {
                              try {
                                await axios.put(`https://it342-catamco-pawpal-production.up.railway.app/api/v1/reports/${r.id}/resolve`, {}, {
                                  headers: { Authorization: `Bearer ${token}` }
                                });
                                setReportDetailModal(null);
                                await fetchReports();
                              } catch { /* silent */ }
                            }}
                            style={{ flex: 1, padding: "12px 0", borderRadius: 10, border: "1.5px solid var(--border)", background: "white", color: "var(--muted)", fontWeight: 600, fontSize: 14, cursor: "pointer" }}
                          >
                            Resolve
                          </button>
                          <button
                            onClick={() => { setReportDetailModal(null); setBanModal({ open: true, userId: r.reportedUserId, userName: r.reportedUserName }); }}
                            style={{ flex: 1, padding: "12px 0", borderRadius: 10, border: "none", background: "#ef4444", color: "white", fontWeight: 600, fontSize: 14, cursor: "pointer" }}
                          >
                            Ban Reported User
                          </button>
                        </div>
                      )}
                      {r.reportedUserBanned && (
                        <div style={{ textAlign: "center", padding: "12px", background: "rgba(239,68,68,0.06)", border: "1px solid rgba(239,68,68,0.2)", borderRadius: 10, fontSize: 13, color: "#dc2626", fontWeight: 600 }}>
                          This user has already been banned.
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              );
            })()}

            {/* Filter Tabs */}
            <div className="ad-filter-tabs" style={{ marginBottom: 24 }}>
              {[
                { key: "ALL", label: "All" },
                { key: "PENDING", label: "Pending" },
                { key: "RESOLVED", label: "Resolved" },
              ].map(({ key, label }) => {
                const count = key === "ALL" ? reports.length
                  : key === "PENDING" ? reports.filter(r => r.status === "PENDING" && !r.reportedUserBanned).length
                  : reports.filter(r => r.status === "RESOLVED" || r.reportedUserBanned).length;
                return (
                  <button key={key} className={`ad-filter-tab ${reportFilter === key ? "ad-tab-active" : ""}`}
                    onClick={() => setReportFilter(key)}>
                    {label}
                    {count > 0 && (
                      <span style={{ marginLeft: 6, fontSize: 11, fontWeight: 700, background: reportFilter === key ? "rgba(255,255,255,0.25)" : "var(--border)", color: reportFilter === key ? "white" : "var(--muted)", padding: "1px 7px", borderRadius: 100 }}>
                        {count}
                      </span>
                    )}
                  </button>
                );
              })}
            </div>

            {/* Reports List */}
            <div className="admin-listings-list">
              {reportsLoading ? (
                <div className="od-loading"><div className="od-spinner" /><p>Loading reports...</p></div>
              ) : (() => {
                const filteredReports = reportFilter === "ALL" ? reports
                  : reportFilter === "PENDING" ? reports.filter(r => r.status === "PENDING" && !r.reportedUserBanned)
                  : reports.filter(r => r.status === "RESOLVED" || r.reportedUserBanned);
                return filteredReports.length === 0 ? (
                  <div className="od-empty">
                    <div className="od-empty-icon">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 48, height: 48 }}>
                        <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/>
                        <line x1="4" y1="22" x2="4" y2="15"/>
                      </svg>
                    </div>
                    <h3>No reports here</h3>
                  </div>
                ) : filteredReports.map((r) => (
                  <div
                    key={r.id}
                    className="admin-listing-card"
                    style={{ padding: 18, cursor: "pointer", gap: 0 }}
                    onClick={() => { setReportDetailModal(r); setReportModalView("report"); }}
                  >
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", width: "100%" }}>
                      <div style={{ flex: 1 }}>
                        <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 8 }}>
                          <span style={{ fontWeight: 700, fontSize: 15, color: "var(--dark)" }}>Report #{r.id}</span>
                          <span style={{
                            fontSize: 10, fontWeight: 700, color: "white", padding: "2px 8px", borderRadius: 100,
                            background: r.reportedUserBanned ? "#ef4444" : r.status === "RESOLVED" ? "#16a34a" : "#f97316"
                          }}>
                            {r.reportedUserBanned ? "BANNED" : r.status || "PENDING"}
                          </span>
                        </div>
                        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "4px 24px", marginBottom: 10 }}>
                          <div style={{ fontSize: 13, color: "var(--muted)" }}>
                            <strong style={{ color: "var(--dark)" }}>Reporter:</strong> {r.reporterName}
                          </div>
                          <div style={{ fontSize: 13, color: "var(--muted)" }}>
                            <strong style={{ color: "var(--dark)" }}>Reported:</strong> {r.reportedUserName}
                            {r.reportedUserBanned && <span style={{ marginLeft: 6, fontSize: 10, fontWeight: 700, color: "white", background: "#ef4444", padding: "1px 6px", borderRadius: 100 }}>BANNED</span>}
                          </div>
                        </div>
                        <div style={{ fontSize: 13, color: "var(--dark)", background: "var(--cream)", padding: "8px 12px", borderRadius: 8 }}>
                          <strong>Reason:</strong> {r.reason.length > 80 ? r.reason.slice(0, 80) + "…" : r.reason}
                        </div>
                      </div>
                      <div style={{ marginLeft: 16, display: "flex", alignItems: "center", color: "var(--muted)", fontSize: 12, gap: 4, flexShrink: 0 }}>
                        View details
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 14, height: 14 }}>
                          <polyline points="9 18 15 12 9 6"/>
                        </svg>
                      </div>
                    </div>
                  </div>
                ));
              })()}
            </div>
          </>
        )}
      </div>
    </div>
  );
}