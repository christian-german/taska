package com.taska.android.data.api

import android.accounts.AccountManager
import android.content.Context
import com.taska.android.BuildConfig
import com.taska.android.auth.AuthConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val api: TaskaApi by lazy {
        val authInterceptor = Interceptor { chain ->
            val am = AccountManager.get(appContext)
            val token = am.getAccountsByType(AuthConfig.ACCOUNT_TYPE)
                .firstOrNull()
                ?.let { account -> am.blockingGetAuthToken(account, AuthConfig.AUTH_TOKEN_TYPE, false) }
            val request = if (token != null) {
                chain.request().newBuilder().header("Authorization", "Bearer $token").build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .build()
            )
            .build()
            .create(TaskaApi::class.java)
    }
}
