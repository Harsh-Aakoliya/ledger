package com.noviaralabs.ledger.data

import com.noviaralabs.ledger.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface LedgerApi {
    @POST("api/auth/login") suspend fun login(@Body r: LoginRequest): User
    @POST("api/auth/register") suspend fun register(@Body r: RegisterRequest): User

    @GET("api/banks") suspend fun banks(): List<Bank>
    @GET("api/categories") suspend fun categories(): List<Category>

    @GET("api/users/{uid}/dashboard") suspend fun dashboard(@Path("uid") uid: Long): Dashboard

    @POST("api/users/{uid}/banks")
    suspend fun addBank(@Path("uid") uid: Long, @Body r: AddBankRequest): Dashboard

    @PUT("api/users/{uid}/banks/{id}")
    suspend fun updateBank(@Path("uid") uid: Long, @Path("id") id: Long, @Body r: UpdateBankRequest): Dashboard

    @DELETE("api/users/{uid}/banks/{id}")
    suspend fun removeBank(@Path("uid") uid: Long, @Path("id") id: Long): Dashboard

    @POST("api/users/{uid}/transactions")
    suspend fun addTxn(@Path("uid") uid: Long, @Body r: TxnRequest): Dashboard

    @PUT("api/users/{uid}/transactions/{id}")
    suspend fun updateTxn(@Path("uid") uid: Long, @Path("id") id: Long, @Body r: TxnRequest): Dashboard

    @DELETE("api/users/{uid}/transactions/{id}")
    suspend fun deleteTxn(@Path("uid") uid: Long, @Path("id") id: Long): Dashboard
}

object Network {
    // generous timeouts: Neon may need a few seconds to wake up
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: LedgerApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LedgerApi::class.java)
    }
}
