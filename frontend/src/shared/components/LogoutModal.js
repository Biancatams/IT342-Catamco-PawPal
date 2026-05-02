import React from "react";
import "../styles/LogoutModal.css";

export default function LogoutModal({ onConfirm, onCancel }) {
  return (
    <div className="lm-overlay">
      <div className="lm-modal">
        <div className="lm-icon">🐾</div>
        <h3 className="lm-title">Leaving so soon?</h3>
        <p className="lm-msg">
          Are you sure you want to log out of PawPal?
        </p>
        <div className="lm-actions">
          <button className="lm-cancel" onClick={onCancel}>
            Stay
          </button>
          <button className="lm-confirm" onClick={onConfirm}>
            Yes, Log Out
          </button>
        </div>
      </div>
    </div>
  );
}