import React, { useState, useEffect } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import axios from "axios";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import pawLogo from "../../pawlogo.png";
import "../../shared/styles/Navbar.css";
import "./PetDetail.css";
import LogoutModal from "../../shared/components/LogoutModal";

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});

const REPORT_REASONS = [
  "Fake or misleading listing",
  "Suspected animal abuse or neglect",
  "Inappropriate content",
  "Suspicious or scam behavior",
  "Others",
];

export default function PetDetail() {
  const navigate = useNavigate();
  const { petId } = useParams();
  const location = useLocation();
  const token = localStorage.getItem("token");
  const existingStatus = location.state?.existingStatus || null;

  const [pet, setPet] = useState(null);
  const [loading, setLoading] = useState(true);

  const [reportModal, setReportModal] = useState(false);
  const [selectedReportReason, setSelectedReportReason] = useState("");
  const [customReportReason, setCustomReportReason] = useState("");
  const [reportSubmitting, setReportSubmitting] = useState(false);
  const [reportSuccess, setReportSuccess] = useState(false);

  useEffect(() => { fetchPet(); }, [petId]);

  useEffect(() => { fetchPet(); }, [petId]);

  useEffect(() => {
    const controls = document.querySelectorAll('.leaflet-control-container');
    controls.forEach(el => {
      el.style.visibility = reportModal ? 'hidden' : 'visible';
    });
  }, [reportModal]);

  const fetchPet = async () => {
    try {
      const res = await axios.get(`https://it342-catamco-pawpal-production.up.railway.app/api/v1/pets/${petId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setPet(res.data.data);
    } catch {}
    finally { setLoading(false); }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  const openReportModal = () => {
    setSelectedReportReason("");
    setCustomReportReason("");
    setReportSuccess(false);
    setReportModal(true);
  };

  const handleSubmitReport = async () => {
    const reason = selectedReportReason === "Others"
      ? customReportReason.trim()
      : selectedReportReason;
    if (!reason) return;
    setReportSubmitting(true);
    try {
      await axios.post(
        "https://it342-catamco-pawpal-production.up.railway.app/api/v1/reports",
        { reportedUserId: pet.owner.id, reason },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setReportSuccess(true);
    } catch {
      alert("Failed to submit report.");
    } finally {
      setReportSubmitting(false);
    }
  };

  if (loading) return (
    <div className="od-page">
      <div className="od-loading" style={{ paddingTop: 120 }}>
        <div className="od-spinner" />
        <p>Loading pet details...</p>
      </div>
    </div>
  );

  if (!pet) return (
    <div className="od-page">
      <div className="od-empty" style={{ paddingTop: 120 }}>
        <div className="od-empty-icon">🐾</div>
        <h3>Pet not found</h3>
        <button className="od-post-btn" onClick={() => navigate("/adopter/dashboard")}>Back to Browse</button>
      </div>
    </div>
  );

  const hasCoords = pet.latitude != null && pet.longitude != null;
  const traits = pet.characteristics || [];
  const hasHealth = pet.vaccinated || pet.neutered || pet.microchipped || pet.healthChecked;

  return (
    <div className="od-page">

      {/* Report Modal */}
      {reportModal && (
        <div style={{
          position: "fixed", inset: 0, zIndex: 1000,
          background: "rgba(0,0,0,0.35)", backdropFilter: "blur(3px)",
          display: "flex", alignItems: "center", justifyContent: "center", padding: 20,
        }}>
          <div style={{
            background: "white", borderRadius: 20, padding: 32,
            width: "100%", maxWidth: 460, boxShadow: "0 20px 60px rgba(0,0,0,0.15)",
          }}>
            {reportSuccess ? (
              <div style={{ textAlign: "center", padding: "12px 0" }}>
                <div style={{
                  width: 56, height: 56, borderRadius: "50%",
                  background: "rgba(22,163,74,0.1)", display: "flex",
                  alignItems: "center", justifyContent: "center", margin: "0 auto 16px",
                }}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 24, height: 24 }}>
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                </div>
                <h3 style={{ fontSize: 18, fontWeight: 700, color: "var(--green)", marginBottom: 8 }}>Report Submitted</h3>
                <p style={{ fontSize: 14, color: "var(--muted)", marginBottom: 24 }}>Thank you for helping keep PawPal safe.</p>
                <button onClick={() => setReportModal(false)} style={{
                  background: "var(--green)", color: "white", border: "none",
                  borderRadius: 10, padding: "11px 32px", fontWeight: 600, fontSize: 14, cursor: "pointer",
                }}>Done</button>
              </div>
            ) : (
              <>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 16 }}>
                  <div style={{
                    width: 48, height: 48, borderRadius: "50%",
                    background: "rgba(220,38,38,0.08)", display: "flex",
                    alignItems: "center", justifyContent: "center",
                  }}>
                    <svg viewBox="0 0 24 24" fill="none" stroke="#dc2626" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 22, height: 22 }}>
                      <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/>
                      <line x1="4" y1="22" x2="4" y2="15"/>
                    </svg>
                  </div>
                  <button onClick={() => setReportModal(false)} style={{ background: "none", border: "none", cursor: "pointer", color: "var(--muted)", fontSize: 22, lineHeight: 1 }}>✕</button>
                </div>
                <h3 style={{ fontSize: 18, fontWeight: 700, color: "var(--green)", marginBottom: 6 }}>Report this Owner</h3>
                <p style={{ fontSize: 13, color: "var(--muted)", marginBottom: 20, lineHeight: 1.6 }}>
                  Select a reason for your report. Your identity will remain anonymous.
                </p>
                <div style={{ display: "flex", flexDirection: "column", gap: 8, marginBottom: 16 }}>
                  {REPORT_REASONS.map((r) => (
                    <button key={r}
                      onClick={() => setSelectedReportReason(r)}
                      style={{
                        display: "flex", alignItems: "center", gap: 10,
                        background: selectedReportReason === r ? "rgba(220,38,38,0.06)" : "var(--cream)",
                        border: selectedReportReason === r ? "1.5px solid #dc2626" : "1.5px solid var(--border)",
                        borderRadius: 8, padding: "10px 14px", fontSize: 13,
                        fontWeight: selectedReportReason === r ? 600 : 500,
                        color: selectedReportReason === r ? "#dc2626" : "var(--text)",
                        cursor: "pointer", textAlign: "left", fontFamily: "inherit",
                      }}
                    >
                      {selectedReportReason === r && (
                        <svg viewBox="0 0 24 24" fill="none" stroke="#dc2626" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 13, height: 13, flexShrink: 0 }}>
                          <polyline points="20 6 9 17 4 12"/>
                        </svg>
                      )}
                      {r}
                    </button>
                  ))}
                </div>
                {selectedReportReason === "Others" && (
                  <textarea
                    value={customReportReason}
                    onChange={(e) => setCustomReportReason(e.target.value)}
                    placeholder="Please describe the issue..."
                    maxLength={300}
                    style={{
                      width: "100%", borderRadius: 10, border: "1.5px solid var(--border)",
                      padding: "10px 14px", fontSize: 13, resize: "vertical", minHeight: 80,
                      fontFamily: "inherit", outline: "none", boxSizing: "border-box", marginBottom: 8,
                    }}
                  />
                )}
                <div style={{ display: "flex", gap: 10, marginTop: 8 }}>
                  <button onClick={() => setReportModal(false)} style={{
                    flex: 1, padding: "11px 0", borderRadius: 10,
                    border: "1.5px solid var(--border)", background: "white",
                    color: "var(--muted)", fontWeight: 600, fontSize: 14, cursor: "pointer",
                  }}>Cancel</button>
                  <button
                    onClick={handleSubmitReport}
                    disabled={!selectedReportReason || (selectedReportReason === "Others" && !customReportReason.trim()) || reportSubmitting}
                    style={{
                      flex: 1, padding: "11px 0", borderRadius: 10, border: "none",
                      background: "#dc2626", color: "white", fontWeight: 600, fontSize: 14,
                      cursor: "pointer", opacity: (!selectedReportReason || reportSubmitting) ? 0.5 : 1,
                    }}
                  >
                    {reportSubmitting ? "Submitting..." : "Submit Report"}
                  </button>
                </div>
              </>
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
          <button className="navbar-link" onClick={() => navigate("/adopter/dashboard")}>Browse</button>
          <button className="navbar-link" onClick={() => navigate("/adopter/requests")}>My Requests</button>
          <button className="navbar-link" onClick={() => navigate("/adopter/profile")}>Profile</button>
          <button className="navbar-btn-outline" onClick={handleLogout}>Logout</button>
        </div>
      </nav>

      <div className="od-body">
        <button className="vr-back-btn" onClick={() => navigate(-1)} style={{ marginBottom: 24 }}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="15 18 9 12 15 6" />
          </svg>
          Back
        </button>

        <div className="pd-layout">
          <div className="pd-img-col">
            <div className="pd-img-wrap">
              {pet.imageUrl ? (
                <img src={`${process.env.REACT_APP_API_URL || "http://localhost:8080"}${pet.imageUrl}`} alt={pet.name} className="pd-img" />
              ) : (
                <div className="pd-img-placeholder"><span>🐾</span></div>
              )}
              <span className="od-badge badge-available" style={{ position: "absolute", top: 16, right: 16 }}>Available</span>
            </div>
          </div>

          <div className="pd-info-col">
            <div className="pd-card">
              <h1 className="pd-name">{pet.name}</h1>
              {pet.breed && <p className="pd-breed">{pet.breed}</p>}

              <div className="pd-meta-row-wrap">
                {pet.age && (
                  <div className="pd-meta-chip">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    {pet.age}
                  </div>
                )}
                <div className="pd-meta-chip">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                  {pet.type}
                </div>
                {pet.gender && (
                  <div className="pd-meta-chip">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="8" r="4"/><path d="M12 12v8M8 20h8"/></svg>
                    {pet.gender.charAt(0) + pet.gender.slice(1).toLowerCase()}
                  </div>
                )}
                {pet.location && (
                  <div className="pd-meta-chip">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                    {pet.location}
                  </div>
                )}
              </div>

              <div className="pd-divider" />

              <div className="pd-section">
                <h3 className="pd-section-title">About {pet.name}</h3>
                <p className="pd-description">{pet.description || "No description provided."}</p>
              </div>

              {traits.length > 0 && (
                <>
                  <div className="pd-divider" />
                  <div className="pd-section">
                    <h3 className="pd-section-title">Personality Traits</h3>
                    <div className="pd-traits-wrap">
                      {traits.map((trait) => <span key={trait} className="pd-trait-tag">{trait}</span>)}
                    </div>
                  </div>
                </>
              )}

              {hasHealth && (
                <>
                  <div className="pd-divider" />
                  <div className="pd-section">
                    <h3 className="pd-section-title">Health &amp; Care</h3>
                    <div className="pd-health-grid">
                      {pet.vaccinated && (
                        <div className="pd-health-card">
                          <div className="pd-health-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg></div>
                          <div><div className="pd-health-label">Vaccinated</div><div className="pd-health-sub">Up to date on all shots</div></div>
                        </div>
                      )}
                      {pet.neutered && (
                        <div className="pd-health-card">
                          <div className="pd-health-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg></div>
                          <div><div className="pd-health-label">Neutered</div><div className="pd-health-sub">Spayed/neutered</div></div>
                        </div>
                      )}
                      {pet.microchipped && (
                        <div className="pd-health-card">
                          <div className="pd-health-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg></div>
                          <div><div className="pd-health-label">Microchipped</div><div className="pd-health-sub">Registered microchip</div></div>
                        </div>
                      )}
                      {pet.healthChecked && (
                        <div className="pd-health-card">
                          <div className="pd-health-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg></div>
                          <div><div className="pd-health-label">Health Check</div><div className="pd-health-sub">Recent vet examination</div></div>
                        </div>
                      )}
                    </div>
                  </div>
                </>
              )}

              <div className="pd-divider" />

              <div className="pd-section">
                <h3 className="pd-section-title">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 16, height: 16 }}>
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                  </svg>
                  Location
                </h3>
                <p className="pd-location-label">{pet.location}</p>
                <p className="pd-location-note">General area shown — exact address shared after adoption approval.</p>
                {hasCoords ? (
                  <div className="pd-map-wrap">
                    <MapContainer center={[pet.latitude, pet.longitude]} zoom={13} style={{ height: "260px", width: "100%", borderRadius: 12 }} scrollWheelZoom={false}>
                      <TileLayer attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                      <Marker position={[pet.latitude, pet.longitude]}>
                        <Popup>{pet.name} is near this area</Popup>
                      </Marker>
                    </MapContainer>
                  </div>
                ) : (
                  <div className="pd-map-placeholder">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                    <p>Map not available for this listing</p>
                  </div>
                )}
              </div>

              {pet.owner && (
                <>
                  <div className="pd-divider" />
                  <button onClick={openReportModal} style={{
                    display: "flex", alignItems: "center", gap: 6,
                    background: "none", border: "1px solid rgba(220,38,38,0.25)",
                    borderRadius: 8, color: "#dc2626", fontSize: 13,
                    cursor: "pointer", padding: "8px 14px", fontWeight: 500,
                    fontFamily: "inherit", transition: "background 0.2s",
                  }}>
                    <svg viewBox="0 0 24 24" fill="none" stroke="#dc2626" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 14, height: 14 }}>
                      <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/>
                      <line x1="4" y1="22" x2="4" y2="15"/>
                    </svg>
                    Report this owner
                  </button>
                </>
              )}

              <div className="pd-divider" />

              {!existingStatus && (
                <button className="pp-btn-submit" onClick={() => navigate(`/adopter/request/${pet.id}`, { state: { pet } })}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                  </svg>
                  Request Adoption
                </button>
              )}

              {existingStatus === "PENDING" && (
                <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 10, background: "rgba(199,105,83,0.07)", border: "1px solid rgba(199,105,83,0.2)", borderRadius: 10, padding: "14px 20px", color: "#9a4a35", fontSize: 14, fontWeight: 600 }}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 16, height: 16 }}><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                  Your request is pending review
                </div>
              )}
              {existingStatus === "APPROVED" && (
                <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 10, background: "rgba(22,163,74,0.07)", border: "1px solid rgba(22,163,74,0.2)", borderRadius: 10, padding: "14px 20px", color: "#15803d", fontSize: 14, fontWeight: 600 }}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 16, height: 16 }}><polyline points="20 6 9 17 4 12"/></svg>
                  Your adoption request was approved!
                </div>
              )}
              {existingStatus === "DECLINED" && (
                <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 10, background: "rgba(220,38,38,0.05)", border: "1px solid rgba(220,38,38,0.15)", borderRadius: 10, padding: "14px 20px", color: "#b91c1c", fontSize: 14, fontWeight: 600 }}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 16, height: 16 }}><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                  Your request was not approved
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}