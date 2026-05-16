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

        val tokenAuthenticator = okhttp3.Authenticator { _, response ->
            // Stop retrying after the first retry to avoid infinite loops
            if (response.priorResponse?.code == 401) return@Authenticator null

            val am = AccountManager.get(appContext)
            val account = am.getAccountsByType(AuthConfig.ACCOUNT_TYPE).firstOrNull()
                ?: return@Authenticator null

            val staleToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (staleToken != null) {
                am.invalidateAuthToken(AuthConfig.ACCOUNT_TYPE, staleToken)
            }

            val newToken = am.blockingGetAuthToken(account, AuthConfig.AUTH_TOKEN_TYPE, false)
                ?: return@Authenticator null

            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
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
                    .authenticator(tokenAuthenticator)
                    .addInterceptor(logging)
                    .build()
            )
            .build()
            .create(TaskaApi::class.java)
    }
}
