import React, { useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import pawLogo from "../pawlogo.png";
import "../styles/Navbar.css";
import "../styles/OwnerDashboard.css";
import LogoutModal from "../components/LogoutModal";

const PERSONALITY_TRAITS = [
  "Friendly", "Energetic", "Playful", "Good with Kids", "Good with Dogs",
  "Good with Cats", "House-trained", "Calm", "Loyal", "Curious",
  "Affectionate", "Independent",
];

export default function PostPet() {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");
  const fileRef = useRef(null);
  const [showLogout, setShowLogout] = useState(false);

  const [form, setForm] = useState({
    name: "", type: "DOG", breed: "", age: "",
    gender: "", description: "", location: "",
  });
  const [selectedTraits, setSelectedTraits] = useState([]);
  const [health, setHealth] = useState({
    vaccinated: false, neutered: false, microchipped: false, healthChecked: false,
  });
  const [photo, setPhoto] = useState(null);
  const [photoPreview, setPhotoPreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const toggleTrait = (trait) => {
    setSelectedTraits((prev) =>
      prev.includes(trait) ? prev.filter((t) => t !== trait) : [...prev, trait]
    );
  };

  const toggleHealth = (key) => setHealth((prev) => ({ ...prev, [key]: !prev[key] }));

  const handlePhoto = (e) => {
    const file = e.target.files[0];
    if (file) { setPhoto(file); setPhotoPreview(URL.createObjectURL(file)); }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file) { setPhoto(file); setPhotoPreview(URL.createObjectURL(file)); }
  };

  const geocodeLocation = async (locationText) => {
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(locationText)}&format=json&limit=1`,
        { headers: { "Accept-Language": "en", "User-Agent": "PawPalApp/1.0" } }
      );
      const data = await res.json();
      if (data && data.length > 0)
        return { latitude: parseFloat(data[0].lat), longitude: parseFloat(data[0].lon) };
    } catch {}
    return { latitude: null, longitude: null };
  };

  const handleSubmit = async () => {
    if (!form.name || !form.type || !form.age || !form.description || !form.location) {
      setError("Please fill in all required fields.");
      return;
    }
    setError("");
    setLoading(true);
    try {
      const { latitude, longitude } = await geocodeLocation(form.location);
      const formData = new FormData();
      const petData = {
        name: form.name, type: form.type, breed: form.breed || null,
        age: form.age, gender: form.gender || null, description: form.description,
        location: form.location, latitude, longitude,
        characteristics: selectedTraits,
        vaccinated: health.vaccinated,
        neutered: health.neutered,
        microchipped: health.microchipped,
        healthChecked: health.healthChecked,
      };
      formData.append("data", new Blob([JSON.stringify(petData)], { type: "application/json" }));
      if (photo) formData.append("image", photo);
      await axios.post("http://localhost:8080/api/v1/pets", formData, {
        headers: { Authorization: `Bearer ${token}` },
      });
      navigate("/owner/dashboard");
    } catch (err) {
      setError(err.response?.data?.error?.message || err.response?.data?.message || "Failed to post pet. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const confirmLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

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
          <button className="navbar-link" onClick={() => navigate("/owner/dashboard")}>Home</button>
          <button className="navbar-btn-outline" onClick={() => setShowLogout(true)}>Logout</button>
        </div>
      </nav>

      <div className="od-body">
        <div className="pp-page-header">
          <button className="vr-back-btn" onClick={() => navigate("/owner/dashboard")}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="15 18 9 12 15 6" />
            </svg>
            Back
          </button>
          <h1 className="pp-page-title">Post a Pet for Adoption</h1>
          <p className="pp-page-sub">Help your pet find a loving new home</p>
        </div>

        {error && <div className="od-error" style={{ marginBottom: 20 }}>{error}</div>}

        <div className="pp-form-wrap">
          {/* LEFT COLUMN */}
          <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>

            {/* Pet Information */}
            <div className="pp-section">
              <h2 className="pp-section-title">Pet Information</h2>

              <div className="pp-field">
                <label className="pp-label">Pet Name <span className="pp-required">*</span></label>
                <input className="pp-input" name="name" value={form.name} onChange={handleChange} placeholder="Enter your pet's name" />
              </div>

              <div className="pp-field">
                <label className="pp-label">Type of Pet <span className="pp-required">*</span></label>
                <div className="pp-select-wrap">
                  <select className="pp-select" name="type" value={form.type} onChange={handleChange}>
                    <option value="DOG">Dog</option>
                    <option value="CAT">Cat</option>
                    <option value="BIRD">Bird</option>
                    <option value="RABBIT">Rabbit</option>
                    <option value="OTHER">Other</option>
                  </select>
                  <svg className="pp-select-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="6 9 12 15 18 9"/></svg>
                </div>
              </div>

              <div className="pp-field">
                <label className="pp-label">Breed</label>
                <div className="pp-select-wrap">
                  <select className="pp-select" name="breed" value={form.breed} onChange={handleChange}>
                    <option value="">Unknown / Mixed</option>
                    {form.type === "DOG" && <><option value="Aspin">Aspin (Mixed)</option><option value="Labrador Retriever">Labrador Retriever</option><option value="German Shepherd">German Shepherd</option><option value="Golden Retriever">Golden Retriever</option><option value="Shih Tzu">Shih Tzu</option><option value="Poodle">Poodle</option><option value="Beagle">Beagle</option><option value="Bulldog">Bulldog</option><option value="Chihuahua">Chihuahua</option><option value="Pomeranian">Pomeranian</option><option value="Dachshund">Dachshund</option><option value="Siberian Husky">Siberian Husky</option><option value="Other">Other</option></>}
                    {form.type === "CAT" && <><option value="Puspin">Puspin (Mixed)</option><option value="Persian">Persian</option><option value="Siamese">Siamese</option><option value="Maine Coon">Maine Coon</option><option value="British Shorthair">British Shorthair</option><option value="Ragdoll">Ragdoll</option><option value="Bengal">Bengal</option><option value="Abyssinian">Abyssinian</option><option value="Other">Other</option></>}
                    {form.type === "BIRD" && <><option value="Maya">Maya</option><option value="Budgerigar">Budgerigar (Budgie)</option><option value="Cockatiel">Cockatiel</option><option value="Lovebird">Lovebird</option><option value="African Grey">African Grey</option><option value="Macaw">Macaw</option><option value="Canary">Canary</option><option value="Other">Other</option></>}
                    {form.type === "RABBIT" && <><option value="Holland Lop">Holland Lop</option><option value="Dutch">Dutch</option><option value="Mini Rex">Mini Rex</option><option value="Lionhead">Lionhead</option><option value="Angora">Angora</option><option value="Other">Other</option></>}
                    {form.type === "OTHER" && <option value="Other">Other / Unknown</option>}
                  </select>
                  <svg className="pp-select-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="6 9 12 15 18 9"/></svg>
                </div>
              </div>

              <div className="pp-field">
                <label className="pp-label">Gender</label>
                <div className="pp-select-wrap">
                  <select className="pp-select" name="gender" value={form.gender} onChange={handleChange}>
                    <option value="">Unknown</option>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                  </select>
                  <svg className="pp-select-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="6 9 12 15 18 9"/></svg>
                </div>
              </div>

              <div className="pp-field">
                <label className="pp-label">Age <span className="pp-required">*</span></label>
                <input className="pp-input" name="age" value={form.age} onChange={handleChange} placeholder="e.g., 2 years, 6 months" />
              </div>

              <div className="pp-field">
                <label className="pp-label">Description <span className="pp-required">*</span></label>
                <textarea className="pp-textarea" name="description" value={form.description} onChange={handleChange} placeholder="Tell adopters about your pet's personality, habits, and needs..." maxLength={500} />
                <div className="pp-char-count">{form.description.length}/500 characters</div>
              </div>

              <div className="pp-field">
                <label className="pp-label">Location <span className="pp-required">*</span></label>
                <div className="pp-input-wrap">
                  <input className="pp-input pp-input-icon" name="location" value={form.location} onChange={handleChange} placeholder="City, Province (e.g., Cebu City)" />
                  <svg className="pp-input-icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                  </svg>
                </div>
                <p style={{ fontSize: 11, color: "var(--muted)", marginTop: 4 }}>
                  Enter your general area — exact address will only be shared after adoption approval.
                </p>
              </div>
            </div>

            {/* Personality Traits */}
            <div className="pp-section">
              <h2 className="pp-section-title">Personality Traits</h2>
              <p style={{ fontSize: 13, color: "var(--muted)", marginBottom: 14 }}>
                Select all traits that describe your pet.
              </p>
              <div className="pp-traits-grid">
                {PERSONALITY_TRAITS.map((trait) => (
                  <button
                    key={trait}
                    type="button"
                    className={`pp-trait-tag ${selectedTraits.includes(trait) ? "pp-trait-active" : ""}`}
                    onClick={() => toggleTrait(trait)}
                  >
                    {selectedTraits.includes(trait) && (
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ width: 12, height: 12 }}>
                        <polyline points="20 6 9 17 4 12"/>
                      </svg>
                    )}
                    {trait}
                  </button>
                ))}
              </div>
            </div>

            {/* Health & Care */}
            <div className="pp-section">
              <h2 className="pp-section-title">Health &amp; Care</h2>
              <p style={{ fontSize: 13, color: "var(--muted)", marginBottom: 14 }}>
                Check all that apply to your pet.
              </p>
              <div className="pp-health-grid">
                {[
                  { key: "vaccinated", label: "Vaccinated", sub: "Up to date on all shots" },
                  { key: "neutered", label: "Neutered / Spayed", sub: "Spayed/neutered" },
                  { key: "microchipped", label: "Microchipped", sub: "Registered microchip" },
                  { key: "healthChecked", label: "Health Check", sub: "Recent vet examination" },
                ].map(({ key, label, sub }) => (
                  <div
                    key={key}
                    className={`pp-health-card ${health[key] ? "pp-health-active" : ""}`}
                    onClick={() => toggleHealth(key)}
                  >
                    <div className="pp-health-check">
                      {health[key] ? (
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="20 6 9 17 4 12"/>
                        </svg>
                      ) : (
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <circle cx="12" cy="12" r="10"/>
                        </svg>
                      )}
                    </div>
                    <div>
                      <div className="pp-health-label">{label}</div>
                      <div className="pp-health-sub">{sub}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* RIGHT COLUMN — Photo */}
          <div className="pp-section" style={{ alignSelf: "start" }}>
            <h2 className="pp-section-title">Pet Photo</h2>
            <div
              className={`pp-dropzone ${photoPreview ? "pp-dropzone-filled" : ""}`}
              onClick={() => fileRef.current.click()}
              onDrop={handleDrop}
              onDragOver={(e) => e.preventDefault()}
            >
              {photoPreview ? (
                <img src={photoPreview} alt="Preview" className="pp-preview-img" />
              ) : (
                <div className="pp-drop-content">
                  <div className="pp-drop-icon">🐾</div>
                  <div className="pp-drop-title">Upload Pet Photo</div>
                  <div className="pp-drop-sub">Drag and drop or click to browse</div>
                  <div className="pp-drop-hint">JPG, PNG or GIF (max 5MB)</div>
                </div>
              )}
              <input ref={fileRef} type="file" accept="image/*" style={{ display: "none" }} onChange={handlePhoto} />
            </div>
            <div className="pp-tips-card">
              <div className="pp-tips-title">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ width: 14, height: 14 }}>
                  <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                Photo Tips
              </div>
              <ul className="pp-tips-list">
                <li>Use good lighting and clear focus</li>
                <li>Show your pet's face and personality</li>
                <li>Avoid blurry or dark photos</li>
              </ul>
            </div>
          </div>
        </div>

        <div className="pp-actions">
          <button className="pp-btn-submit" onClick={handleSubmit} disabled={loading}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            {loading ? "Posting..." : "Post Pet for Adoption"}
          </button>
          <button className="pp-btn-cancel" onClick={() => navigate("/owner/dashboard")} disabled={loading}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}