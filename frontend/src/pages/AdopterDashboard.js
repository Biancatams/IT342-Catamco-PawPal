import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import pawLogo from "../pawlogo.png";
import "../styles/Navbar.css";
import "../styles/OwnerDashboard.css";
import "../styles/AdopterDashboard.css";
import LogoutModal from "../components/LogoutModal";

export default function AdopterDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "{}");
  const token = localStorage.getItem("token");
  const [pets, setPets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [filterType, setFilterType] = useState("ALL");

  useEffect(() => { fetchPets(); }, []);

  const fetchPets = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/v1/pets", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setPets(res.data.data?.pets || []);
    } catch (err) {
      console.error("Failed to load pets", err);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  const filtered = pets.filter((p) => {
    const matchesType = filterType === "ALL" || p.type === filterType;
    const matchesSearch =
      p.name?.toLowerCase().includes(search.toLowerCase()) ||
      p.breed?.toLowerCase().includes(search.toLowerCase()) ||
      p.location?.toLowerCase().includes(search.toLowerCase());
    return matchesType && matchesSearch && p.status === "AVAILABLE";
  });

  return (
    <div className="od-page">
      <nav className="navbar">
        <button className="navbar-brand" onClick={() => navigate("/")}>
          <img src={pawLogo} alt="PawPal logo" className="navbar-logo-img" />
          <span className="navbar-brand-text">PawPal</span>
        </button>
        <div className="navbar-links">
          <button className="navbar-link active" onClick={() => navigate("/adopter/dashboard")}>Browse</button>
          <button className="navbar-link" onClick={() => navigate("/adopter/requests")}>My Requests</button>
          <button className="navbar-link" onClick={() => navigate("/adopter/profile")}>Profile</button>
          <button className="navbar-btn-outline" onClick={handleLogout}>Logout</button>
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

        {/* Search + Filter */}
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

        {/* Grid */}
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
            {filtered.map((pet) => (
              <div key={pet.id} className="od-pet-card" onClick={() => navigate(`/adopter/pet/${pet.id}`, { state: { pet } })}>
                <div className="od-pet-img-wrap">
                  {pet.imageUrl ? (
                    <img src={`http://localhost:8080${pet.imageUrl}`} alt={pet.name} className="od-pet-img" />
                  ) : (
                    <div className="od-pet-no-img"><span>🐾</span></div>
                  )}
                  <span className="od-badge badge-available">Available</span>
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
                  <button className="od-btn-view" style={{ marginTop: 4 }}>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                    </svg>
                    Adopt Me
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}