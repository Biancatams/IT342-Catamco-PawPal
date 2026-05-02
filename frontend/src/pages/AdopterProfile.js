import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import pawLogo from "../pawlogo.png";
import "../styles/Navbar.css";
import "../styles/OwnerDashboard.css";
import LogoutModal from "../components/LogoutModal";

export default function AdopterProfile() {
  const navigate = useNavigate();
  const [user, setUser] = useState(JSON.parse(localStorage.getItem("user") || "{}"));
  const token = localStorage.getItem("token");

  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [showLogout, setShowLogout] = useState(false);
  const [form, setForm] = useState({
    fullName: user.fullName || "",
    phone: user.phoneNumber || user.phone || "",
  });
  const [saving, setSaving] = useState(false);
  const [saveMsg, setSaveMsg] = useState("");

  const profileImgRef = useRef(null);
  const [profileImg, setProfileImg] = useState(null);
  const [profileImgPreview, setProfileImgPreview] = useState(
    user.profileImageUrl ? `http://localhost:8080${user.profileImageUrl}` : null
  );

  useEffect(() => { fetchMyRequests(); }, []);

  const fetchMyRequests = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/v1/adoption-requests/my", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setRequests(res.data.data || []);
    } catch {}
    finally { setLoading(false); }
  };

  const confirmLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  const handlePhoneChange = (e) => {
    let raw = e.target.value;
    if (!raw.startsWith("+63")) raw = "+63";
    const digits = raw.slice(3).replace(/\D/g, "").slice(0, 10);
    let formatted = "+63";
    if (digits.length > 0) formatted += " " + digits.slice(0, 3);
    if (digits.length > 3) formatted += " " + digits.slice(3, 6);
    if (digits.length > 6) formatted += " " + digits.slice(6, 10);
    setForm({ ...form, phone: formatted });
  };

  const handleProfileImg = (e) => {
    const file = e.target.files[0];
    if (file) { setProfileImg(file); setProfileImgPreview(URL.createObjectURL(file)); }
  };

  const handleSave = async () => {
    const digitsOnly = (form.phone || "").replace(/\D/g, "");
    if (form.phone && form.phone.length > 3 && digitsOnly.length !== 12) {
      setSaveMsg("Phone number must be +63 followed by 10 digits.");
      return;
    }
    setSaving(true);
    try {
      const formData = new FormData();
      formData.append("fullName", form.fullName);
      formData.append("phoneNumber", form.phone || "");
      if (profileImg) formData.append("image", profileImg);

      const res = await axios.put(
        "http://localhost:8080/api/v1/users/me",
        formData,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const updated = {
        ...user,
        fullName: res.data.data.fullName,
        phone: res.data.data.phoneNumber,
        phoneNumber: res.data.data.phoneNumber,
        profileImageUrl: res.data.data.profileImageUrl,
      };
      localStorage.setItem("user", JSON.stringify(updated));
      setUser(updated);
      setSaveMsg("Profile updated!");
      setEditing(false);
      setProfileImg(null);
      setProfileImgPreview(
        res.data.data.profileImageUrl
          ? `http://localhost:8080${res.data.data.profileImageUrl}`
          : null
      );
      setTimeout(() => setSaveMsg(""), 3000);
    } catch {
      setSaveMsg("Failed to update. Try again.");
    } finally {
      setSaving(false);
    }
  };

  const stats = {
    total: requests.length,
    pending: requests.filter((r) => r.status === "PENDING").length,
    approved: requests.filter((r) => r.status === "APPROVED").length,
    declined: requests.filter((r) => r.status === "DECLINED").length,
  };

  const memberSince = user.createdAt
    ? new Date(user.createdAt).toLocaleDateString("en-US", { month: "long", year: "numeric" })
    : "January 2024";

  const initials = (user.fullName || "U")
    .split(" ").map((n) => n[0]).join("").toUpperCase().slice(0, 2);

  const statusLabel = (s) =>
    s === "PENDING" ? "Pending" : s === "APPROVED" ? "Approved" : "Declined";
  const statusClass = (s) =>
    s === "PENDING" ? "badge-pending" : s === "APPROVED" ? "badge-available" : "badge-adopted";

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
          <button className="navbar-link active" onClick={() => navigate("/adopter/profile")}>Profile</button>
          <button className="navbar-btn-outline" onClick={() => setShowLogout(true)}>Logout</button>
        </div>
      </nav>

      <div className="od-body">
        <div className="prof-layout">
          <div className="prof-sidebar">
            <div className="prof-card">
              <div className="prof-card-header">
                <h2 className="prof-card-title">Profile Information</h2>
                <button className="prof-edit-icon" onClick={() => setEditing(!editing)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </button>
              </div>
              <p className="prof-card-sub">Your account details</p>

              <div
                className="prof-avatar-wrap"
                onClick={() => editing && profileImgRef.current.click()}
                style={{ cursor: editing ? "pointer" : "default" }}
                title={editing ? "Click to change photo" : ""}
              >
                {profileImgPreview ? (
                  <img src={profileImgPreview} alt="Profile" className="prof-avatar-img" />
                ) : (
                  <div className="prof-avatar">{initials}</div>
                )}
                <div className="prof-avatar-badge">
                  {editing ? (
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                      <circle cx="12" cy="13" r="4"/>
                    </svg>
                  ) : (
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                  )}
                </div>
                <input ref={profileImgRef} type="file" accept="image/*" style={{ display: "none" }} onChange={handleProfileImg} />
              </div>

              {editing ? (
                <div className="prof-edit-form">
                  {profileImgPreview && (
                    <p style={{ fontSize: 11, color: "var(--muted)", textAlign: "center", marginBottom: 8 }}>
                      Click your photo above to change it
                    </p>
                  )}
                  <div className="pp-field">
                    <label className="pp-label">Full Name</label>
                    <input className="pp-input" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
                  </div>
                  <div className="pp-field">
                    <label className="pp-label">Phone</label>
                    <input className="pp-input" value={form.phone || "+63"} onChange={handlePhoneChange} placeholder="+63 9XX XXX XXXX" maxLength={16} />
                    <p style={{ fontSize: 11, color: "var(--muted)", marginTop: 4 }}>Format: +63 9XX XXX XXXX</p>
                  </div>
                  <button className="prof-save-btn" onClick={handleSave} disabled={saving}>
                    {saving ? "Saving..." : "Save Changes"}
                  </button>
                  {saveMsg && (
                    <p className="prof-save-msg" style={{ color: saveMsg.includes("Failed") || saveMsg.includes("must") ? "#dc2626" : "#16a34a" }}>
                      {saveMsg}
                    </p>
                  )}
                </div>
              ) : (
                <>
                  <div className="prof-name">{user.fullName || "User"}</div>
                  <div className="prof-role-tag">Adopter</div>

                  <div className="prof-fields">
                    <div className="prof-field-item">
                      <div className="prof-field-icon-row">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>
                        </svg>
                        <div>
                          <div className="prof-field-label">EMAIL ADDRESS</div>
                          <div className="prof-field-value">{user.email || "—"}</div>
                        </div>
                      </div>
                    </div>
                    <div className="prof-field-item">
                      <div className="prof-field-icon-row">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.4 2 2 0 0 1 3.6 1.22h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.8a16 16 0 0 0 5.29 5.29l.96-.96a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
                        </svg>
                        <div>
                          <div className="prof-field-label">PHONE NUMBER</div>
                          <div className="prof-field-value">{user.phoneNumber || user.phone || "Not set"}</div>
                        </div>
                      </div>
                    </div>
                    <div className="prof-field-item">
                      <div className="prof-field-icon-row">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                        </svg>
                        <div>
                          <div className="prof-field-label">ROLE</div>
                          <div><span className="prof-adopter-badge">Adopter</span></div>
                        </div>
                      </div>
                    </div>
                    <div className="prof-field-item">
                      <div className="prof-field-icon-row">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
                        </svg>
                        <div>
                          <div className="prof-field-label">MEMBER SINCE</div>
                          <div className="prof-field-value">{memberSince}</div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <button className="prof-edit-btn" onClick={() => setEditing(true)}>Edit Profile</button>
                </>
              )}
            </div>

            <div className="prof-card prof-stats-card">
              <h3 className="prof-card-title">Quick Stats</h3>
              <p className="prof-card-sub">Your adoption activity</p>
              <div className="prof-stat-item">
                <div className="prof-stat-dot" style={{ background: "#6b7280" }} />
                <div className="prof-stat-body">
                  <div className="prof-stat-label">TOTAL REQUESTS</div>
                  <div className="prof-stat-num">{stats.total}</div>
                </div>
              </div>
              <div className="prof-stat-item">
                <div className="prof-stat-dot prof-dot-pending" />
                <div className="prof-stat-body">
                  <div className="prof-stat-label">PENDING</div>
                  <div className="prof-stat-num">{stats.pending}</div>
                </div>
              </div>
              <div className="prof-stat-item">
                <div className="prof-stat-dot prof-dot-available" />
                <div className="prof-stat-body">
                  <div className="prof-stat-label">APPROVED</div>
                  <div className="prof-stat-num">{stats.approved}</div>
                </div>
              </div>
              <div className="prof-stat-item">
                <div className="prof-stat-dot prof-dot-adopted" />
                <div className="prof-stat-body">
                  <div className="prof-stat-label">DECLINED</div>
                  <div className="prof-stat-num">{stats.declined}</div>
                </div>
              </div>
            </div>
          </div>

          <div className="prof-main">
            <div className="prof-pets-card">
              <div className="prof-pets-header">
                <div>
                  <h2 className="prof-card-title">My Adoption Requests</h2>
                  <p className="prof-card-sub">Track your adoption applications</p>
                </div>
                <span className="prof-total-badge">{requests.length} Total</span>
              </div>

              {loading ? (
                <div className="od-loading"><div className="od-spinner" /></div>
              ) : requests.length === 0 ? (
                <div className="od-empty">
                  <div className="od-empty-icon">🐾</div>
                  <p>No adoption requests yet.</p>
                  <button className="od-post-btn" onClick={() => navigate("/adopter/dashboard")}>Browse Pets</button>
                </div>
              ) : (
                <div className="prof-pets-grid">
                  {requests.map((req) => (
                    <div key={req.id} className="prof-pet-item" style={{ cursor: "default" }}>
                      <div className="prof-pet-thumb-wrap">
                        {req.pet?.imageUrl ? (
                          <img src={`http://localhost:8080${req.pet.imageUrl}`} alt={req.pet.name} className="prof-pet-thumb" />
                        ) : (
                          <div className="prof-pet-thumb-placeholder">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 24, height: 24, stroke: "#c4b5a5" }}>
                              <path d="M10 5.172C10 3.782 8.423 2.679 6.5 3c-2.823.47-4.113 6.006-4 7 .08.703 1.725 1.722 3.656 1 1.261-.472 1.96-1.45 2.344-2.5"/><path d="M14.267 5.172c0-1.39 1.577-2.493 3.5-2.172 2.823.47 4.113 6.006 4 7-.08.703-1.725 1.722-3.656 1-1.261-.472-1.96-1.45-2.344-2.5"/><path d="M8 14v.5"/><path d="M16 14v.5"/><path d="M11.25 16.25h1.5L12 17l-.75-.75z"/><path d="M4.42 11.247A13.152 13.152 0 0 0 4 14.556C4 18.728 7.582 21 12 21s8-2.272 8-6.444c0-1.061-.162-2.2-.493-3.309m-9.243-6.082A8.801 8.801 0 0 1 12 5c.78 0 1.5.108 2.161.306"/>
                            </svg>
                          </div>
                        )}
                      </div>
                      <div className="prof-pet-item-info">
                        <div className="prof-pet-item-name">{req.pet?.name || "Pet"}</div>
                        <div className="prof-pet-item-breed">{req.pet?.breed || req.pet?.age || ""}</div>
                        <div className="prof-pet-item-meta">
                          <span className={`od-badge ${statusClass(req.status)}`} style={{ position: "static", fontSize: 10, padding: "2px 8px" }}>
                            {statusLabel(req.status)}
                          </span>
                        </div>
                        {req.createdAt && (
                          <div className="prof-pet-item-date">
                            {new Date(req.createdAt).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}