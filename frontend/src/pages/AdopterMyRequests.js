import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import pawLogo from "../pawlogo.png";
import "../styles/Navbar.css";
import "../styles/OwnerDashboard.css";
import "../styles/AdopterDashboard.css";
import LogoutModal from "../components/LogoutModal";

export default function AdopterMyRequests() {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");
  const [showLogout, setShowLogout] = useState(false);

  useEffect(() => { fetchRequests(); }, []);

  const fetchRequests = async () => {
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

  const filtered = filter === "ALL" ? requests : requests.filter((r) => r.status === filter);
  const counts = {
    ALL: requests.length,
    PENDING: requests.filter((r) => r.status === "PENDING").length,
    APPROVED: requests.filter((r) => r.status === "APPROVED").length,
    DECLINED: requests.filter((r) => r.status === "DECLINED").length,
  };

  const statusConfig = {
    PENDING:  { label: "Pending",  badgeCls: "badge-pending",   bannerBg: "rgba(199,105,83,0.07)",   bannerBorder: "rgba(199,105,83,0.2)",   bannerColor: "#9a4a35" },
    APPROVED: { label: "Approved", badgeCls: "badge-available", bannerBg: "rgba(31,51,39,0.06)",    bannerBorder: "rgba(31,51,39,0.15)",    bannerColor: "var(--orange)" },
    DECLINED: { label: "Declined", badgeCls: "badge-adopted",   bannerBg: "rgba(220,38,38,0.05)",   bannerBorder: "rgba(220,38,38,0.15)",   bannerColor: "#b91c1c" },
  };

  const statCards = [
    {
      key: "ALL", label: "Total", iconCls: "od-stat-total",
      icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
    },
    {
      key: "PENDING", label: "Pending", iconCls: "od-stat-pending",
      icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
    },
    {
      key: "APPROVED", label: "Approved", iconCls: "od-stat-available",
      icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
    },
    {
      key: "DECLINED", label: "Declined", iconCls: "od-stat-total",
      icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
    },
  ];

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
          <button className="navbar-link active" onClick={() => navigate("/adopter/requests")}>My Requests</button>
          <button className="navbar-link" onClick={() => navigate("/adopter/profile")}>Profile</button>
          <button className="navbar-btn-outline" onClick={() => setShowLogout(true)}>Logout</button>
        </div>
      </nav>

      <div className="od-body">
        <div className="od-header">
          <div>
            <h1 className="od-title">My Adoption Requests</h1>
            <p className="od-subtitle">Track the status of your adoption applications</p>
          </div>
        </div>

        {/* Stats */}
        <div className="od-stats" style={{ gridTemplateColumns: "repeat(4, 1fr)" }}>
          {statCards.map(({ key, label, iconCls, icon }) => (
            <div
              key={key}
              className="od-stat-card"
              onClick={() => setFilter(key)}
              style={{
                cursor: "pointer",
                border: filter === key ? "1.5px solid var(--green)" : "1px solid var(--border)",
                background: filter === key ? "var(--orange)" : "white",
                transition: "all 0.2s",
              }}
            >
              <div
                className={`od-stat-icon ${iconCls}`}
                style={filter === key ? { background: "rgba(255,255,255,0.15)" } : {}}
              >
                {icon}
              </div>
              <div>
                <div className="od-stat-label" style={filter === key ? { color: "rgba(255,255,255,0.65)" } : {}}>
                  {label}
                </div>
                <div className="od-stat-num" style={filter === key ? { color: "white" } : {}}>
                  {counts[key]}
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Filter Tabs */}
        <div className="ad-filter-tabs" style={{ marginBottom: 24 }}>
          {["ALL", "PENDING", "APPROVED", "DECLINED"].map((t) => (
            <button
              key={t}
              className={`ad-filter-tab ${filter === t ? "ad-tab-active" : ""}`}
              onClick={() => setFilter(t)}
            >
              {t === "ALL" ? "All" : statusConfig[t].label}
              {counts[t] > 0 && (
                <span style={{
                  marginLeft: 6, fontSize: 11, fontWeight: 700,
                  background: filter === t ? "rgba(255,255,255,0.25)" : "var(--border)",
                  color: filter === t ? "white" : "var(--muted)",
                  padding: "1px 7px", borderRadius: 100,
                }}>
                  {counts[t]}
                </span>
              )}
            </button>
          ))}
        </div>

        {/* Content */}
        {loading ? (
          <div className="od-loading">
            <div className="od-spinner" />
            <p>Loading your requests...</p>
          </div>
        ) : filtered.length === 0 ? (
          <div className="od-empty">
            <div className="od-empty-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="var(--border)" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 48, height: 48 }}>
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
            </div>
            <h3>{filter === "ALL" ? "No requests yet" : `No ${statusConfig[filter]?.label.toLowerCase()} requests`}</h3>
            <p>{filter === "ALL" ? "Start by browsing pets available for adoption." : "Check back later or browse more pets."}</p>
            <button className="od-post-btn" onClick={() => navigate("/adopter/dashboard")}>Browse Pets</button>
          </div>
        ) : (
          <div className="amr-list">
            {filtered.map((req) => {
              const cfg = statusConfig[req.status] || statusConfig.PENDING;
              return (
                <div key={req.id} className="amr-card">

                  {/* Pet Image */}
                  <div className="amr-pet-img-wrap">
                    {req.pet?.imageUrl ? (
                      <img src={`http://localhost:8080${req.pet.imageUrl}`} alt={req.pet.name} className="amr-pet-img" />
                    ) : (
                      <div className="amr-pet-no-img">
                        <svg viewBox="0 0 24 24" fill="none" stroke="#b89e8a" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 36, height: 36 }}>
                          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                        </svg>
                      </div>
                    )}
                  </div>

                  {/* Info */}
                  <div className="amr-info">
                    <div className="amr-top-row">
                      <div>
                        <h3 className="amr-pet-name">{req.pet?.name || "Pet"}</h3>
                        {req.pet?.breed && <p className="amr-pet-breed">{req.pet.breed}</p>}
                      </div>
                      <span className={`od-badge ${cfg.badgeCls}`} style={{ position: "static" }}>
                        {cfg.label}
                      </span>
                    </div>

                    {req.pet?.age && (
                      <div className="amr-meta-row">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                        </svg>
                        {req.pet.age}
                      </div>
                    )}

                    <div className="amr-divider" />

                    {/* Status Banner */}
                    <div
                      className="amr-status-banner"
                      style={{
                        background: cfg.bannerBg,
                        border: `1px solid ${cfg.bannerBorder}`,
                        color: cfg.bannerColor,
                        display: "flex",
                        alignItems: "center",
                        gap: 10,
                      }}
                    >
                      <span style={{
                        width: 26, height: 26, borderRadius: "50%",
                        background: cfg.bannerBorder,
                        display: "flex", alignItems: "center", justifyContent: "center",
                        flexShrink: 0,
                      }}>
                        {req.status === "PENDING" && (
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 13, height: 13 }}>
                            <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                          </svg>
                        )}
                        {req.status === "APPROVED" && (
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 13, height: 13 }}>
                            <polyline points="20 6 9 17 4 12"/>
                          </svg>
                        )}
                        {req.status === "DECLINED" && (
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 13, height: 13 }}>
                            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                          </svg>
                        )}
                      </span>
                      <span style={{ fontSize: 13, fontWeight: 500, lineHeight: 1.5 }}>
                        {req.status === "PENDING" && "Your request is under review by the pet owner."}
                        {req.status === "APPROVED" && "Your adoption request has been approved! Contact the owner."}
                        {req.status === "DECLINED" && "Unfortunately, your adoption request was not approved this time."}
                      </span>
                    </div>

                    <div className="amr-date">
                      Submitted {req.createdAt ? new Date(req.createdAt).toLocaleDateString("en-US", {
                        month: "long", day: "numeric", year: "numeric",
                      }) : "—"}
                    </div>
                  </div>

                  {/* Action Column */}
                  <div className="amr-actions">
                    {req.status === "APPROVED" && (
                      <button
                        className="od-btn-view"
                        onClick={() => navigate("/adopter/request-accepted", {
                          state: { pet: req.pet, owner: req.owner, status: "APPROVED" }
                        })}
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                        </svg>
                        View Details
                      </button>
                    )}
                    {req.status === "PENDING" && (
                      <button
                        className="od-btn-edit"
                        onClick={() => navigate(`/adopter/pet/${req.pet?.id}`)}
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                        </svg>
                        View Pet
                      </button>
                    )}
                    {req.status === "DECLINED" && (
                      <button
                        className="od-btn-edit"
                        onClick={() => navigate("/adopter/request-accepted", {
                          state: { pet: req.pet, owner: req.owner, status: "DECLINED" }
                        })}
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                        </svg>
                        View Details
                      </button>
                    )}
                  </div>

                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}