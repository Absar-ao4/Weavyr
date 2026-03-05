package com.weavyr.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://backend.weavyr.workers.dev/api/"
    private const val PAPER_BASE_URL = "https://api.semanticscholar.org/"

    private lateinit var appContext: Context

    /* ---------------- INITIALIZE CONTEXT ---------------- */

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /* ---------------- AUTH INTERCEPTOR ---------------- */

    private val authInterceptor = Interceptor { chain ->

        val prefs = appContext.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        val request: Request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        chain.proceed(request)
    }

    /* ---------------- OKHTTP CLIENT ---------------- */

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    /* ---------------- MAIN BACKEND RETROFIT ---------------- */

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }

    /* ---------------- SEMANTIC SCHOLAR API ---------------- */

    private val paperRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(PAPER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val paperApi: PaperApi by lazy {
        paperRetrofit.create(PaperApi::class.java)
    }
}