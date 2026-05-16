package com.taska.android.auth

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class AccountAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?,
    ): Bundle = Bundle().apply {
        putParcelable(AccountManager.KEY_INTENT, loginIntent(response, accountType))
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle?,
    ): Bundle {
        val am = AccountManager.get(context)
        val cached = am.peekAuthToken(account, authTokenType)
        if (!cached.isNullOrEmpty()) {
            return Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                putString(AccountManager.KEY_AUTHTOKEN, cached)
            }
        }

        val refreshToken = am.getUserData(account, "refresh_token")
        val tokenEndpoint = am.getUserData(account, "token_endpoint")
        if (!refreshToken.isNullOrEmpty() && !tokenEndpoint.isNullOrEmpty()) {
            val newToken = tryRefresh(am, account, authTokenType, refreshToken, tokenEndpoint)
            if (newToken != null) {
                return Bundle().apply {
                    putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                    putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                    putString(AccountManager.KEY_AUTHTOKEN, newToken)
                }
            }
        }

        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, loginIntent(response, account.type))
        }
    }

    private fun tryRefresh(
        am: AccountManager,
        account: Account,
        authTokenType: String,
        refreshToken: String,
        tokenEndpoint: String,
    ): String? {
        return try {
            val conn = URL(tokenEndpoint).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 15_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val body = "grant_type=refresh_token" +
                "&refresh_token=${URLEncoder.encode(refreshToken, "UTF-8")}" +
                "&client_id=${URLEncoder.encode(AuthConfig.CLIENT_ID, "UTF-8")}"
            conn.outputStream.use { it.write(body.toByteArray()) }

            val code = conn.responseCode
            if (code != 200) {
                Log.w("AccountAuthenticator", "Token refresh failed: $code")
                return null
            }

            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val newAccessToken = json.getString("access_token")
            val newRefreshToken = json.optString("refresh_token")

            am.setAuthToken(account, authTokenType, newAccessToken)
            if (newRefreshToken.isNotEmpty()) {
                am.setUserData(account, "refresh_token", newRefreshToken)
            }

            newAccessToken
        } catch (e: Exception) {
            Log.e("AccountAuthenticator", "tryRefresh failed", e)
            null
        }
    }

    private fun loginIntent(response: AccountAuthenticatorResponse, accountType: String) =
        Intent(context, LoginActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        }

    override fun editProperties(r: AccountAuthenticatorResponse, s: String): Bundle =
        throw UnsupportedOperationException()

    override fun confirmCredentials(r: AccountAuthenticatorResponse, a: Account, b: Bundle?) = null

    override fun getAuthTokenLabel(authTokenType: String) = authTokenType

    override fun updateCredentials(
        r: AccountAuthenticatorResponse,
        a: Account,
        s: String?,
        b: Bundle?,
    ) = null

    override fun hasFeatures(
        r: AccountAuthenticatorResponse,
        a: Account,
        fs: Array<out String>,
    ) = Bundle().apply { putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false) }
}
