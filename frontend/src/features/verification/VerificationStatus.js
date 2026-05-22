import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import pawLogo from "../../pawlogo.png";
import "../../shared/styles/Navbar.css";
import "../../shared/styles/OwnerDashboard.css";

export default function VerificationStatus() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "{}");
  const token = localStorage.getItem("token");
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchStatus(); }, []);

  const fetchStatus = async () => {
    try {
      const res = await axios.get("https://it342-catamco-pawpal-production.up.railway.app/api/v1/verification/my", {
        headers: { Authorization: `Bearer ${token}` },
      });
      const status = res.data.data;
      if (status?.status === "APPROVED") {
        if (user.role === "PET_OWNER") navigate("/owner/dashboard");
        else navigate("/adopter/dashboard");
        return;
      }
      if (!status || status.status === "NONE") {
        navigate("/verification");
        return;
      }
      setData(status);
    } catch {
    } finally {
      setLoading(false);
    }
  };

  if (loading) return (
    <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
      <div className="od-spinner" />
    </div>
  );

  const isPending = data?.status === "PENDING";
  const isRejected = data?.status === "REJECTED";

  return (
    <div className="od-page">
      <nav className="navbar">
        <button className="navbar-brand" onClick={() => navigate("/")}>
          <img src={pawLogo} alt="PawPal logo" className="navbar-logo-img" />
          <span className="navbar-brand-text">PawPal</span>
        </button>
      </nav>

      <div style={{
        display: "flex", flexDirection: "column", alignItems: "center",
        justifyContent: "center", flex: 1, padding: "60px 20px", textAlign: "center",
      }}>
        <div style={{ fontSize: 64, marginBottom: 20 }}>🪪</div>

        <span style={{
          display: "inline-block", padding: "6px 18px", borderRadius: 100,
          fontSize: 13, fontWeight: 700, color: "white", marginBottom: 20,
          background: isPending ? "var(--orange)" : "#dc2626",
        }}>
          {isPending ? "⏳ Verification Pending" : "✕ Verification Rejected"}
        </span>

        <p style={{
          fontSize: 15, color: "var(--text)", maxWidth: 440,
          lineHeight: 1.7, marginBottom: 16,
        }}>
          {isPending
            ? "Your verification request is being reviewed by our admin. Please wait — we'll notify you via email once it's processed."
            : "Unfortunately, your verification was not approved."}
        </p>

        {isRejected && data?.adminComment && (
          <div style={{
            background: "rgba(220,38,38,0.06)", border: "1px solid rgba(220,38,38,0.18)",
            borderRadius: 12, padding: "14px 20px", maxWidth: 440, width: "100%",
            fontSize: 13, color: "#b91c1c", marginBottom: 24, textAlign: "left",
          }}>
            <strong>Admin note:</strong> {data.adminComment}
          </div>
        )}

        <div style={{ display: "flex", gap: 12, marginTop: 12 }}>
          {isRejected && (
            <button
              onClick={() => navigate("/verification")}
              style={{
                height: 46, padding: "0 32px", background: "var(--green)", color: "white",
                border: "none", borderRadius: 10, fontFamily: "DM Sans, sans-serif",
                fontSize: 14, fontWeight: 600, cursor: "pointer",
              }}
            >
              ↺ Resubmit
            </button>
          )}

          {/* NEW RETURN HOME BUTTON */}
          <button
            onClick={() => {
              localStorage.removeItem("token");
              localStorage.removeItem("user");
              navigate("/");
            }}
            style={{
              height: 46, padding: "0 32px", background: "transparent", color: "var(--dark)",
              border: "1.5px solid var(--border)", borderRadius: 10, fontFamily: "DM Sans, sans-serif",
              fontSize: 14, fontWeight: 600, cursor: "pointer",
            }}
          >
            Return to Home
          </button>
        </div>
      </div>
    </div>
  );
}