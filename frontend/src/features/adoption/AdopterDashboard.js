import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import pawLogo from "../../pawlogo.png";
import "../../shared/styles/Navbar.css";
import "../../shared/styles/OwnerDashboard.css";
import "./AdopterDashboard.css";
import LogoutModal from "../../shared/components/LogoutModal";


const filterByAvailability = (pets) =>
  pets.filter((p) => p.status === "AVAILABLE");

const filterByType = (pets, type) =>
  type === "ALL" ? pets : pets.filter((p) => p.type === type);

const filterBySearch = (pets, search) => {
  if (!search.trim()) return pets;
  const q = search.toLowerCase();
  return pets.filter(
    (p) =>
      p.name?.toLowerCase().includes(q) ||
      p.breed?.toLowerCase().includes(q) ||
      p.location?.toLowerCase().includes(q)
  );
};

// Compose all strategies — each filters the result of the previous
const filterPets = (pets, type, search) => {
  let result = filterByAvailability(pets);
  result = filterByType(result, type);
  result = filterBySearch(result, search);
  return result;
};

// ── Component ────────────────────────────────────────────────────────────────

export default function AdopterDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "{}");
  const token = localStorage.getItem("token");
  const [pets, setPets] = useState([]);
  const [myRequests, setMyRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [filterType, setFilterType] = useState("ALL");
  const [showLogout, setShowLogout] = useState(false);

  useEffect(() => { fetchAll(); }, []);

  const fetchAll = async () => {
    try {
      const [petsRes, reqRes] = await Promise.all([
        axios.get("http://localhost:8080/api/v1/pets", {
          headers: { Authorization: `Bearer ${token}` },
        }),
        axios.get("http://localhost:8080/api/v1/adoption-requests/my", {
          headers: { Authorization: `Bearer ${token}` },
        }),
      ]);
      setPets(petsRes.data.data?.pets || []);
      setMyRequests(reqRes.data.data || []);
    } catch (err) {
      console.error("Failed to load data", err);
    } finally {
      setLoading(false);
    }
  };

  const requestByPetId = myRequests.reduce((acc, r) => {
    if (r.pet?.id) acc[r.pet.id] = r;
    return acc;
  }, {});

  const hasPendingRequest = myRequests.some((r) => r.status === "PENDING");

  const confirmLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  // Strategy pattern applied here — clean, composable, readable
  const filtered = filterPets(pets, filterType, search);

  const statusConfig = {
    PENDING:  { label: "Pending",  badgeCls: "badge-pending" },
    APPROVED: { label: "Approved", badgeCls: "badge-available" },
    DECLINED: { label: "Declined", badgeCls: "badge-adopted" },
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
          <button className="navbar-link active" onClick={() => navigate("/adopter/dashboard")}>Browse</button>
          <button className="navbar-link" onClick={() => navigate("/adopter/requests")}>My Requests</button>
          <button className="navbar-link" onClick={() => navigate("/adopter/profile")}>Profile</button>
          <button className="navbar-btn-outline" onClick={() => setShowLogout(true)}>Logout</button>
        </div>
      </nav>

      <div className="od-body">
        <div className="od-header">
          <div>
            <h1 className="od-title">Find Your New Best Friend</h1>
            <p className="od-subtitle">
              Hello, {user.fullName?.split(" ")[0] || "there"} — browse pets available for adoption.
            </p>
          </div>
        </div>

        {hasPendingRequest && (
          <div style={{
            display: "flex", alignItems: "center", gap: 10,
            background: "rgba(199,105,83,0.08)", border: "1px solid rgba(199,105,83,0.25)",
            borderRadius: 12, padding: "12px 16px", marginBottom: 20,
            color: "#9a4a35", fontSize: 14, fontWeight: 500,
          }}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 18, height: 18, flexShrink: 0 }}>
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            You have a pending adoption request. You can only submit one request at a time — please wait for a response before applying for another pet.
          </div>
        )}

        <div className="ad-search-row">
          <div className="ad-search-wrap">
            <svg className="ad-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input
              className="ad-search-input"
              placeholder="Search by name, breed, or location..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <div className="ad-filter-tabs">
            {["ALL", "DOG", "CAT", "BIRD", "RABBIT", "OTHER"].map((t) => (
              <button
                key={t}
                className={`ad-filter-tab ${filterType === t ? "ad-tab-active" : ""}`}
                onClick={() => setFilterType(t)}
              >
                {t === "ALL" ? "All Pets" : t.charAt(0) + t.slice(1).toLowerCase() + "s"}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="od-loading">
            <div className="od-spinner" />
            <p>Finding pets near you...</p>
          </div>
        ) : filtered.length === 0 ? (
          <div className="od-empty">
            <div className="od-empty-icon">🐾</div>
            <h3>No pets found</h3>
            <p>Try adjusting your search or filter.</p>
          </div>
        ) : (
          <div className="od-grid">
            {filtered.map((pet) => {
              const existingReq = requestByPetId[pet.id];
              const reqStatus = existingReq?.status;
              const cfg = reqStatus ? statusConfig[reqStatus] : null;

              const isThisPetPending = reqStatus === "PENDING";
              const isBlockedByOtherPending = !reqStatus && hasPendingRequest;
              const isApproved = reqStatus === "APPROVED";
              const isDeclined = reqStatus === "DECLINED";

              return (
                <div
                    key={pet.id}
                    className="od-pet-card"
                    onClick={() => {
                      if (isBlockedByOtherPending) return;
                      if (isThisPetPending) {
                        navigate("/adopter/requests");
                        return;
                      }
                      if (isApproved) {
                        navigate("/adopter/request-accepted", {
                          state: { pet, owner: existingReq?.owner, status: "APPROVED" }
                        });
                        return;
                      }
                      navigate(`/adopter/pet/${pet.id}`, {
                        state: { pet, existingStatus: reqStatus || null }
                      });
                    }}
                    style={{
                      opacity: isBlockedByOtherPending ? 0.65 : 1,
                      cursor: isBlockedByOtherPending ? "not-allowed" : "pointer"
                    }}
                  >
                  <div className="od-pet-img-wrap">
                    {pet.imageUrl ? (
                      <img src={`${process.env.REACT_APP_API_URL || "http://localhost:8080"}${pet.imageUrl}`} alt={pet.name} className="od-pet-img" />
                    ) : (
                      <div className="od-pet-no-img"><span>🐾</span></div>
                    )}
                    {cfg ? (
                      <span className={`od-badge ${cfg.badgeCls}`} style={{ position: "absolute", top: 10, right: 10 }}>
                        {cfg.label}
                      </span>
                    ) : (
                      <span className="od-badge badge-available">Available</span>
                    )}
                  </div>
                  <div className="od-pet-body">
                    <h3 className="od-pet-name">{pet.name}</h3>
                    {pet.breed && <p className="od-pet-breed">{pet.breed}</p>}
                    <div className="od-pet-meta">
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

                    {isThisPetPending ? (
                      <button className="od-btn-edit" style={{ marginTop: 4, cursor: "default", opacity: 0.85 }}
                        onClick={(e) => { e.stopPropagation(); navigate("/adopter/requests"); }}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                        </svg>
                        Request Pending
                      </button>
                    ) : isApproved ? (
                      <button className="od-btn-view" style={{ marginTop: 4, background: "var(--green)" }}
                        onClick={(e) => { e.stopPropagation(); navigate("/adopter/request-accepted", {
                          state: { pet, owner: existingReq?.owner, status: "APPROVED" }
                        }); }}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="20 6 9 17 4 12"/>
                        </svg>
                        Approved — View Details
                      </button>
                    ) : isDeclined ? (
                      <button className="od-btn-edit" style={{ marginTop: 4, opacity: 0.75 }}
                        onClick={(e) => { e.stopPropagation(); navigate(`/adopter/pet/${pet.id}`, {
                          state: { pet, existingStatus: "DECLINED" }
                        }); }}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                        </svg>
                        Request Declined
                      </button>
                    ) : isBlockedByOtherPending ? (
                      <button className="od-btn-view" style={{ marginTop: 4, background: "#ccc", cursor: "not-allowed" }}
                        onClick={(e) => e.stopPropagation()} disabled>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
                        </svg>
                        Unavailable
                      </button>
                    ) : (
                      <button className="od-btn-view" style={{ marginTop: 4 }}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                        </svg>
                        Adopt Me
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
