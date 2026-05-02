package com.mobile.pawpal

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/google-login")
    suspend fun googleLogin(@Body request: GoogleAuthRequest): Response<AuthResponse>

    @GET("api/v1/pets")
    suspend fun getAllPets(@Header("Authorization") token: String): Response<PetsResponse>

    @GET("api/v1/pets/my")
    suspend fun getMyPets(@Header("Authorization") token: String): Response<PetsResponse>

    @GET("api/v1/pets/admin/under-review")
    suspend fun getUnderReviewPets(@Header("Authorization") token: String): Response<PetsResponse>

    @PUT("api/v1/pets/admin/{id}/approve")
    suspend fun approvePet(@Header("Authorization") token: String, @Path("id") id: Int): Response<PetsResponse>
}