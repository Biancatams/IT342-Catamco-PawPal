package com.mobile.pawpal

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

data class GoogleAuthRequest(
    val idToken: String
)