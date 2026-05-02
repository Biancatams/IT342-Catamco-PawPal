import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Landing from "./pages/Landing";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Home from "./pages/Home";
import ProtectedRoute from "./ProtectedRoute";
import OwnerDashboard from "./pages/OwnerDashboard";
import PostPet from "./pages/PostPet";
import ViewRequests from "./pages/ViewRequests";
import OwnerProfile from "./pages/OwnerProfile";
import AdopterDashboard from "./pages/AdopterDashboard";
import PetDetail from "./pages/PetDetail";
import EditPet from "./pages/EditPet";
import AdopterProfile from "./pages/AdopterProfile";
import AdopterMyRequests from "./pages/AdopterMyRequests";
import RequestAdoption from "./pages/RequestAdoption";
import RequestAccepted from "./pages/RequestAccepted";
import AdminDashboard from "./pages/AdminDashboard";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/owner/dashboard" element={<OwnerDashboard />} />
        <Route path="/owner/post-pet" element={<PostPet />} />
        <Route path="/owner/requests/:petId" element={<ViewRequests />} />
        <Route path="/owner/profile" element={<OwnerProfile />} />
        <Route path="/adopter/profile" element={<AdopterProfile />} />
        <Route path="/adopter/requests" element={<AdopterMyRequests />} />
        <Route path="/adopter/request/:petId" element={<RequestAdoption />} />
        <Route path="/adopter/request-accepted" element={<RequestAccepted />} />
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
        <Route path="/adopter/dashboard" element={
          <ProtectedRoute>
            <AdopterDashboard />
          </ProtectedRoute>
        } />
        <Route path="/home" element={
          <ProtectedRoute>
            <Home />
          </ProtectedRoute>
        } />
        <Route path="/adopter/pet/:petId" element={
          <ProtectedRoute>
            <PetDetail />
          </ProtectedRoute>
        } />
        <Route path="/owner/edit-pet/:petId" element={<EditPet />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;