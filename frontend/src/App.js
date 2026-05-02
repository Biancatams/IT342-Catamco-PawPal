import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Landing from "./features/auth/Landing";
import Register from "./features/auth/Register";
import Login from "./features/auth/Login";
import Home from "./features/auth/Home";
import ProtectedRoute from "./ProtectedRoute";
import OwnerDashboard from "./features/pets/OwnerDashboard";
import PostPet from "./features/pets/PostPet";
import ViewRequests from "./features/adoption/ViewRequests";
import OwnerProfile from "./features/pets/OwnerProfile";
import AdopterDashboard from "./features/adoption/AdopterDashboard";
import PetDetail from "./features/pets/PetDetail";
import EditPet from "./features/pets/EditPet";
import AdopterProfile from "./features/adoption/AdopterProfile";
import AdopterMyRequests from "./features/adoption/AdopterMyRequests";
import RequestAdoption from "./features/adoption/RequestAdoption";
import RequestAccepted from "./features/adoption/RequestAccepted";
import AdminDashboard from "./features/admin/AdminDashboard";

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