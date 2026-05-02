import React, { useState, useEffect } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import axios from "axios";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import pawLogo from "../pawlogo.png";
import "../styles/Navbar.css";
import "../styles/PetDetail.css";
import LogoutModal from "../components/LogoutModal";

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});

export default function PetDetail() {
  const navigate = useNavigate();
  const { petId } = useParams();
  const location = useLocation();
  const token = localStorage.getItem("token");
  const existingStatus = location.state?.existingStatus || null;

  const [pet, setPet] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPet();
  }, [petId]);

  const fetchPet = async () => {
    try {
      const res = await axios.get(`http://localhost:8080/api/v1/pets/${petId}`, {
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
        <button className="od-post-btn" onClick={() => navigate("/adopter/dashboard")}>
          Back to Browse
        </button>
      </div>
    </div>
  );

  const hasCoords = pet.latitude != null && pet.longitude != null;
  const traits = pet.characteristics || [];
  const hasHealth = pet.vaccinated || pet.neutered || pet.microchipped || pet.healthChecked;

  return (
    <div className="od-page">
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
                <img src={`http://localhost:8080${pet.imageUrl}`} alt={pet.name} className="pd-img" />
              ) : (
                <div className="pd-img-placeholder"><span>🐾</span></div>
              )}
              <span className="od-badge badge-available" style={{ position: "absolute", top: 16, right: 16 }}>
                Available
              </span>
            </div>
          </div>

          <div className="pd-info-col">
            <div className="pd-card">
              <h1 className="pd-name">{pet.name}</h1>
              {pet.breed && <p className="pd-breed">{pet.breed}</p>}

              <div className="pd-meta-row-wrap">
                {pet.age && (
                  <div className="pd-meta-chip">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                    </svg>
                    {pet.age}
                  </div>
                )}
                <div className="pd-meta-chip">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                  </svg>
                  {pet.type}
                </div>
                {pet.gender && (
                  <div className="pd-meta-chip">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="12" cy="8" r="4"/><path d="M12 12v8M8 20h8"/>
                    </svg>
                    {pet.gender.charAt(0) + pet.gender.slice(1).toLowerCase()}
                  </div>
                )}
                {pet.location && (
                  <div className="pd-meta-chip">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                    </svg>
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
                      {traits.map((trait) => (
                        <span key={trait} className="pd-trait-tag">{trait}</span>
                      ))}
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
                          <div className="pd-health-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                              <polyline points="20 6 9 17 4 12"/>
                            </svg>
                          </div>
                          <div>
                            <div className="pd-health-label">Vaccinated</div>
                            <div className="pd-health-sub">Up to date on all shots</div>
                          </div>
                        </div>
                      )}
                      {pet.neutered && (
                        <div className="pd-health-card">
                          <div className="pd-health-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                              <polyline points="20 6 9 17 4 12"/>
                            </svg>
                          </div>
                          <div>
                            <div className="pd-health-label">Neutered</div>
                            <div className="pd-health-sub">Spayed/neutered</div>
                          </div>
                        </div>
                      )}
                      {pet.microchipped && (
                        <div className="pd-health-card">
                          <div className="pd-health-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                              <polyline points="20 6 9 17 4 12"/>
                            </svg>
                          </div>
                          <div>
                            <div className="pd-health-label">Microchipped</div>
                            <div className="pd-health-sub">Registered microchip</div>
                          </div>
                        </div>
                      )}
                      {pet.healthChecked && (
                        <div className="pd-health-card">
                          <div className="pd-health-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                              <polyline points="20 6 9 17 4 12"/>
                            </svg>
                          </div>
                          <div>
                            <div className="pd-health-label">Health Check</div>
                            <div className="pd-health-sub">Recent vet examination</div>
                          </div>
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
                <p className="pd-location-note">
                  General area shown — exact address shared after adoption approval.
                </p>

                {hasCoords ? (
                  <div className="pd-map-wrap">
                    <MapContainer
                      center={[pet.latitude, pet.longitude]}
                      zoom={13}
                      style={{ height: "260px", width: "100%", borderRadius: 12 }}
                      scrollWheelZoom={false}
                    >
                      <TileLayer
                        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                      />
                      <Marker position={[pet.latitude, pet.longitude]}>
                        <Popup>{pet.name} is near this area</Popup>
                      </Marker>
                    </MapContainer>
                  </div>
                ) : (
                  <div className="pd-map-placeholder">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                    </svg>
                    <p>Map not available for this listing</p>
                  </div>
                )}
              </div>

              <div className="pd-divider" />

              {!existingStatus && (
                <button
                  className="pp-btn-submit"
                  onClick={() => navigate(`/adopter/request/${pet.id}`, { state: { pet } })}
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                  </svg>
                  Request Adoption
                </button>
              )}

              {existingStatus === "PENDING" && (
                <div style={{
                  display: "flex", alignItems: "center", justifyContent: "center", gap: 10,
                  background: "rgba(199,105,83,0.07)", border: "1px solid rgba(199,105,83,0.2)",
                  borderRadius: 10, padding: "14px 20px", color: "#9a4a35", fontSize: 14, fontWeight: 600,
                }}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 16, height: 16 }}>
                    <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                  </svg>
                  Your request is pending review
                </div>
              )}

              {existingStatus === "APPROVED" && (
                <div style={{
                  display: "flex", alignItems: "center", justifyContent: "center", gap: 10,
                  background: "rgba(22,163,74,0.07)", border: "1px solid rgba(22,163,74,0.2)",
                  borderRadius: 10, padding: "14px 20px", color: "#15803d", fontSize: 14, fontWeight: 600,
                }}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 16, height: 16 }}>
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                  Your adoption request was approved!
                </div>
              )}

              {existingStatus === "DECLINED" && (
                <div style={{
                  display: "flex", alignItems: "center", justifyContent: "center", gap: 10,
                  background: "rgba(220,38,38,0.05)", border: "1px solid rgba(220,38,38,0.15)",
                  borderRadius: 10, padding: "14px 20px", color: "#b91c1c", fontSize: 14, fontWeight: 600,
                }}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 16, height: 16 }}>
                    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
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