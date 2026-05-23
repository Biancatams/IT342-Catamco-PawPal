import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import pawLogo from "../../pawlogo.png";
import "../../shared/styles/Navbar.css";
import "../../shared/styles/OwnerDashboard.css";

const CEBU_LOCATIONS = [
  "Cebu City", "Mandaue", "Lapu-Lapu", "Talisay", "Danao", "Carcar", "Toledo",
  "Naga", "Bogo", "Minglanilla", "San Fernando", "Consolacion", "Liloan",
  "Compostela", "Cordova", "Moalboal", "Oslob", "Alcoy", "Dalaguete", "Others",
];

export default function VerificationPage() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "{}");
  const token = localStorage.getItem("token");

  const [fullName, setFullName] = useState(user.fullName || "");
  const [phone, setPhone] = useState("");
  const [location, setLocation] = useState("");
  const [reason, setReason] = useState("");
  const [idFile, setIdFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setIdFile(file);
      setPreview(URL.createObjectURL(file));
    }
  };

  const handlePhoneChange = (e) => {
    let raw = e.target.value;
    if (!raw.startsWith("+63")) raw = "+63";
    const digits = raw.slice(3).replace(/\D/g, "").slice(0, 10);
    let formatted = "+63";
    if (digits.length > 0) formatted += " " + digits.slice(0, 3);
    if (digits.length > 3) formatted += " " + digits.slice(3, 6);
    if (digits.length > 6) formatted += " " + digits.slice(6, 10);
    setPhone(formatted);
  };

  const handleSubmit = async () => {
    if (!fullName.trim()) { setError("Please enter your full name."); return; }
    if (!phone || phone.replace(/\D/g, "").length !== 12) { setError("Please enter a valid phone number (+63 9XX XXX XXXX)."); return; }
    if (!location) { setError("Please select your location."); return; }
    if (!idFile) { setError("Please upload a valid government-issued ID."); return; }
    if (!reason.trim()) { setError("Please provide a reason for verification."); return; }

    setLoading(true);
    setError("");
    try {
      const formData = new FormData();
      formData.append("idImage", idFile);
      formData.append("reason", reason);
      formData.append("fullName", fullName.trim());
      formData.append("phoneNumber", phone);
      formData.append("location", location);

      await axios.post("${process.env.REACT_APP_API_URL}/api/v1/verification/submit", formData, {
        headers: { Authorization: `Bearer ${token}`, "Content-Type": "multipart/form-data" },
      });
      navigate("/verification/status");
    } catch (err) {
      setError(err.response?.data?.error?.message || "Submission failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const inputStyle = {
    width: "100%", fontFamily: "DM Sans, sans-serif", fontSize: 14,
    color: "var(--text)", background: "var(--cream)",
    border: "1.5px solid var(--border)", borderRadius: 10,
    padding: "12px 14px", outline: "none", boxSizing: "border-box",
    transition: "border-color 0.2s",
  };

  return (
    <div className="od-page">
      <nav className="navbar">
        <button className="navbar-brand" onClick={() => navigate("/")}>
          <img src={pawLogo} alt="PawPal logo" className="navbar-logo-img" />
          <span className="navbar-brand-text">PawPal</span>
        </button>
      </nav>

      <div className="od-body" style={{ maxWidth: 600, margin: "0 auto" }}>
        <h1 className="od-title">Identity Verification</h1>
        <p className="od-subtitle" style={{ marginBottom: 32 }}>
          {user.role === "PET_OWNER"
            ? "As a Pet Owner, you need to verify your identity before posting pets."
            : "As an Adopter, you need to verify your identity before sending adoption requests."}
        </p>

        {error && (
          <div style={{
            background: "rgba(220,38,38,0.07)", border: "1px solid rgba(220,38,38,0.2)",
            borderRadius: 10, padding: "12px 16px", color: "#b91c1c",
            fontSize: 13, marginBottom: 20,
          }}>
            {error}
          </div>
        )}

        <div style={{ marginBottom: 20 }}>
          <div style={{ fontWeight: 600, fontSize: 14, color: "var(--dark)", marginBottom: 8 }}>Full Name (as on your ID)</div>
          <input
            style={inputStyle}
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            placeholder="Your full name"
            onFocus={(e) => e.target.style.borderColor = "var(--green)"}
            onBlur={(e) => e.target.style.borderColor = "var(--border)"}
          />
        </div>

        <div style={{ marginBottom: 20 }}>
          <div style={{ fontWeight: 600, fontSize: 14, color: "var(--dark)", marginBottom: 8 }}>Phone Number</div>
          <input
            style={inputStyle}
            value={phone || "+63"}
            onChange={handlePhoneChange}
            placeholder="+63 9XX XXX XXXX"
            maxLength={16}
            onFocus={(e) => e.target.style.borderColor = "var(--green)"}
            onBlur={(e) => e.target.style.borderColor = "var(--border)"}
          />
          <p style={{ fontSize: 11, color: "var(--muted)", marginTop: 4 }}>Format: +63 9XX XXX XXXX</p>
        </div>

        <div style={{ marginBottom: 20 }}>
          <div style={{ fontWeight: 600, fontSize: 14, color: "var(--dark)", marginBottom: 8 }}>Location</div>
          <select
            style={{ ...inputStyle, cursor: "pointer" }}
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            onFocus={(e) => e.target.style.borderColor = "var(--green)"}
            onBlur={(e) => e.target.style.borderColor = "var(--border)"}
          >
            <option value="">Select your city/municipality</option>
            {CEBU_LOCATIONS.map((loc) => (
              <option key={loc} value={loc}>{loc}</option>
            ))}
          </select>
        </div>

        <div style={{ marginBottom: 24 }}>
          <div style={{ fontWeight: 600, fontSize: 14, color: "var(--dark)", marginBottom: 8 }}>
            Upload Government-Issued ID
          </div>
          <p style={{ fontSize: 12, color: "var(--muted)", marginBottom: 10 }}>
            Accepted: UMID, PhilSys ID, Driver's License, Passport, SSS, GSIS, PRC, Voter's ID
          </p>
          <label style={{
            display: "flex", flexDirection: "column", alignItems: "center",
            justifyContent: "center", gap: 8, height: 140,
            border: "2px dashed var(--border)", borderRadius: 12,
            cursor: "pointer", background: "var(--cream)", transition: "border-color 0.2s",
          }}>
            <span style={{ fontSize: 36 }}>🪪</span>
            <span style={{ fontSize: 13, color: "var(--muted)" }}>
              {idFile ? `✓ ${idFile.name}` : "Click to upload ID image"}
            </span>
            <input type="file" accept="image/*" style={{ display: "none" }} onChange={handleFileChange} />
          </label>
          {preview && (
            <img src={preview} alt="ID preview" style={{
              width: "100%", height: 180, objectFit: "cover",
              borderRadius: 12, marginTop: 12, border: "1px solid var(--border)",
            }} />
          )}
        </div>

        <div style={{ marginBottom: 28 }}>
          <div style={{ fontWeight: 600, fontSize: 14, color: "var(--dark)", marginBottom: 8 }}>
            Why do you want to use PawPal?
          </div>
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Tell us a little about yourself and your intention..."
            style={{ ...inputStyle, minHeight: 120, resize: "vertical" }}
            onFocus={(e) => e.target.style.borderColor = "var(--green)"}
            onBlur={(e) => e.target.style.borderColor = "var(--border)"}
          />
        </div>

        <button
          onClick={handleSubmit}
          disabled={loading}
          style={{
            width: "100%", height: 48, background: "var(--green)", color: "white",
            border: "none", borderRadius: 10, fontFamily: "DM Sans, sans-serif",
            fontSize: 15, fontWeight: 600, cursor: loading ? "not-allowed" : "pointer",
            opacity: loading ? 0.7 : 1, transition: "opacity 0.2s",
          }}
        >
          {loading ? "Submitting..." : "Submit Verification Request"}
        </button>

        {/* NEW CANCEL BUTTON HERE */}
        <button
          onClick={() => {
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            navigate("/");
          }}
          disabled={loading}
          style={{
            width: "100%", height: 48, background: "transparent", color: "var(--dark)",
            border: "1.5px solid var(--border)", borderRadius: 10, fontFamily: "DM Sans, sans-serif",
            fontSize: 15, fontWeight: 600, cursor: loading ? "not-allowed" : "pointer",
            marginTop: 12, opacity: loading ? 0.7 : 1, transition: "all 0.2s",
          }}
        >
          Return to Home
        </button>

      </div>
    </div>
  );
}