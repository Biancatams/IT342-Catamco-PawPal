package com.mobile.pawpal.shared

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.Part

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

    @GET("api/v1/pets/{id}")
    suspend fun getPetById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<PetDetailResponse>

    @POST("api/v1/adoption-requests")
    suspend fun submitAdoptionRequest(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<AdoptionRequestResponse>

    @GET("api/v1/adoption-requests/my")
    suspend fun getMyRequests(
        @Header("Authorization") token: String
    ): Response<MyRequestsResponse>

    @GET("api/v1/users/me")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>

    @Multipart
    @PUT("api/v1/users/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Part("fullName") fullName: RequestBody,
        @Part("phoneNumber") phoneNumber: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part("bio") bio: RequestBody?,
        @Part profileImage: MultipartBody.Part?
    ): Response<ProfileResponse>

    @DELETE("api/v1/pets/{id}")
    suspend fun deletePet(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<AdoptionRequestResponse>

    @GET("api/v1/adoption-requests/pet/{petId}")
    suspend fun getRequestsForPet(
        @Header("Authorization") token: String,
        @Path("petId") petId: Int
    ): Response<PetRequestsResponse>

    @PUT("api/v1/adoption-requests/{id}/approve")
    suspend fun approveRequest(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<AdoptionRequestResponse>

    @PUT("api/v1/adoption-requests/{id}/decline")
    suspend fun declineRequest(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<AdoptionRequestResponse>

    @Multipart
    @POST("api/v1/pets")
    suspend fun createPet(
        @Header("Authorization") token: String,
        @Part("data") data: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<AdoptionRequestResponse>

    @Multipart
    @PUT("api/v1/pets/{id}")
    suspend fun updatePet(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part("data") data: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<AdoptionRequestResponse>

    @PUT("api/v1/pets/admin/{id}/reject")
    suspend fun rejectPet(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<AdoptionRequestResponse>

    @Multipart
    @POST("api/v1/verification/submit")
    suspend fun submitVerification(
        @Header("Authorization") token: String,
        @Part("reason") reason: RequestBody,
        @Part idImage: MultipartBody.Part,
        @Part("fullName") fullName: RequestBody,
        @Part("phoneNumber") phoneNumber: RequestBody,
        @Part("location") location: RequestBody
    ): Response<AdoptionRequestResponse>

    @GET("api/v1/verification/my")
    suspend fun getMyVerification(
        @Header("Authorization") token: String
    ): Response<VerificationStatusResponse>

    @GET("api/v1/verification/all")
    suspend fun getAllVerifications(
        @Header("Authorization") token: String
    ): Response<VerificationListResponse>

    @PUT("api/v1/verification/{id}/approve")
    suspend fun approveVerification(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<AdoptionRequestResponse>

    @PUT("api/v1/verification/{id}/reject")
    suspend fun rejectVerification(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<AdoptionRequestResponse>

    @GET("api/v1/users/all")
    suspend fun getAllUsers(
        @Header("Authorization") token: String
    ): Response<AdminUsersResponse>

    @PUT("api/v1/users/{id}/ban")
    suspend fun banUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<AdoptionRequestResponse>

    @POST("api/v1/reports")
    suspend fun submitReport(
        @Header("Authorization") token: String,
        @Body body: ReportRequest
    ): Response<AdoptionRequestResponse>

    @PUT("api/v1/users/{id}/unban")
    suspend fun unbanUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<AdoptionRequestResponse>

    @GET("api/v1/reports/all")
    suspend fun getAllReports(@Header("Authorization") token: String): Response<ReportsResponse>

    @PUT("api/v1/reports/{id}/status")
    suspend fun updateReportStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): Response<SimpleMessageResponse>
}