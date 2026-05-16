package com.mobile.pawpal.shared

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val role: String
)

data class UserData(
    val id: Int,
    val fullName: String,
    val email: String,
    val role: String
)

data class AuthData(
    val accessToken: String,
    val user: UserData
)

data class ErrorInfo(
    val code: String,
    val message: String
)

data class AuthResponse(
    val success: Boolean,
    val data: AuthData?,
    val error: ErrorInfo?,
    val timestamp: String?
)

// FIXED: Now properly sends "token" to match the Java backend
data class GoogleAuthRequest(
    @SerializedName("token") val token: String,
    @SerializedName("role") val role: String? = ""
)

data class Pet(
    val id: Int,
    val name: String,
    val breed: String,
    val age: String,
    val type: String?,
    val location: String,
    val status: String,
    val imageUrl: String?,
    val requestCount: Int?,
    val owner: PetOwnerSummary? = null
)

data class PetOwnerSummary(
    val id: Int,
    val fullName: String
)

data class PetsResponse(
    val success: Boolean,
    val data: PetsData?
)

data class PetsData(
    val pets: List<Pet>
)

data class PetDetail(
    val id: Int,
    val name: String,
    val breed: String,
    val age: String,
    val type: String,
    val gender: String?,
    val location: String,
    val description: String?,
    val imageUrl: String?,
    val status: String,
    val characteristics: List<String>?,
    val vaccinated: Boolean,
    val neutered: Boolean,
    val microchipped: Boolean,
    val healthChecked: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val adminNote: String?
)

data class PetDetailData(
    val id: Int,
    val name: String,
    val breed: String,
    val age: String,
    val type: String,
    val gender: String?,
    val location: String,
    val description: String?,
    val imageUrl: String?,
    val status: String,
    val characteristics: List<String>?,
    val vaccinated: Boolean,
    val neutered: Boolean,
    val microchipped: Boolean,
    val healthChecked: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val adminNote: String?,
    val owner: PetOwnerInfo?
)

data class PetDetailResponse(
    val success: Boolean,
    val data: PetDetailData?
)

data class AdoptionRequestBody(
    val petId: String,
    val adopterName: String,
    val contactInfo: String,
    val reason: String,
    val noteToOwner: String?
)

data class AdoptionRequestResponse(
    val success: Boolean,
    val data: AdoptionRequestData?,
    val error: ErrorInfo?
)

data class AdoptionRequestData(
    val id: Int,
    val petId: Int,
    val status: String,
    val createdAt: String
)

data class AdoptionRequestItem(
    val id: Int,
    val pet: AdoptionPetInfo,
    val owner: AdoptionOwnerInfo,
    val status: String,
    val declineReason: String?,
    val createdAt: String
)

data class AdoptionPetInfo(
    val id: Int,
    val name: String,
    val breed: String,
    val age: String,
    val imageUrl: String?
)

data class AdoptionOwnerInfo(
    val id: Int = 0,
    val fullName: String,
    val email: String,
    val phoneNumber: String?,
    val profileImageUrl: String?
)

data class MyRequestsResponse(
    val success: Boolean,
    val data: List<AdoptionRequestItem>?
)

data class UserProfile(
    val id: Int,
    val fullName: String,
    val email: String,
    val role: String,
    val phoneNumber: String?,
    val address: String?,
    val profileImageUrl: String?,
    val bio: String?
)

data class ProfileResponse(
    val success: Boolean,
    val data: UserProfile?,
    val error: ErrorInfo?
)

data class OwnerPetResponse(
    val success: Boolean,
    val data: PetsData?,
    val error: ErrorInfo?
)
data class OwnerAdoptionRequest(
    val id: Int,
    val adopterId: Int,
    val adopterName: String,
    val contactInfo: String,
    val reason: String,
    val noteToOwner: String?,
    val status: String,
    val declineReason: String?,
    val createdAt: String,
    val adopter: AdoptionOwnerInfo?
)

data class PetRequestsResponse(
    val success: Boolean,
    val data: List<OwnerAdoptionRequest>?,
    val error: ErrorInfo?
)

data class VerificationStatusResponse(
    val success: Boolean,
    val data: VerificationData?,
    val error: ErrorInfo?
)

data class VerificationData(
    val id: Int?,
    val status: String,
    val idImageUrl: String?,
    val reason: String?,
    val adminComment: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val user: VerificationUserInfo?
)

data class VerificationUserInfo(
    val id: Int,
    val fullName: String,
    val email: String,
    val role: String,
    val profileImageUrl: String?,
    val phoneNumber: String?,
    val address: String?
)

data class VerificationListResponse(
    val success: Boolean,
    val data: List<VerificationData>?,
    val error: ErrorInfo?
)

data class AdminUserItem(
    val id: Int,
    val fullName: String,
    val email: String,
    val role: String,
    val phoneNumber: String?,
    val profileImageUrl: String?,
    val address: String?,
    val createdAt: String?,
    val isBanned: Boolean,
    val isVerified: Boolean
)

data class AdminUsersResponse(
    val success: Boolean,
    val data: List<AdminUserItem>?
)

data class PetOwnerInfo(
    val id: Int,
    val fullName: String,
    val email: String,
    val phoneNumber: String?,
    val profileImageUrl: String?
)

data class ReportRequest(
    val reportedUserId: Int,
    val reason: String,
    val adoptionRequestId: Int? = null
)

data class AdoptionRequestSummary(
    val id: Long,
    val adopterName: String,
    val contactInfo: String,
    val reason: String,
    val noteToOwner: String?,
    val status: String,
    val createdAt: String
)

data class ReportItem(
    val id: Long,
    val reporterName: String,
    val reporterEmail: String,
    val reporterRole: String = "",
    val reportedUserId: Long,
    val reportedUserName: String,
    val reportedUserEmail: String,
    val reportedUserRole: String = "",
    val reportedUserBanned: Boolean,
    val reason: String,
    val status: String,
    val createdAt: String,
    val adoptionRequest: AdoptionRequestSummary? = null
)
data class ReportsResponse(
    val success: Boolean,
    val data: List<ReportItem>?
)

data class SimpleMessageResponse(
    val success: Boolean,
    val data: Any?,
    val timestamp: String?
)