package com.taska.android.auth

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.auth0.android.jwt.JWT
import com.taska.android.BuildConfig
import com.taska.android.ui.theme.TaskaTheme
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : ComponentActivity() {

    private lateinit var authService: AuthorizationService
    private var accountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var resultBundle: Bundle? = null

    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@registerForActivityResult runOnUiThread { onAuthCancel() }
        val response = AuthorizationResponse.fromIntent(data)
        if (response != null) exchangeCode(response) else runOnUiThread { onAuthCancel() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountAuthenticatorResponse =
            intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        accountAuthenticatorResponse?.onRequestContinued()

        val connectionBuilder = if (BuildConfig.DEBUG) AllowHttpConnectionBuilder()
        else DefaultConnectionBuilder.INSTANCE

        authService = AuthorizationService(
            this,
            AppAuthConfiguration.Builder().setConnectionBuilder(connectionBuilder).build()
        )

        setContent {
            TaskaTheme {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        if (savedInstanceState != null) return

        AuthorizationServiceConfiguration.fetchFromIssuer(
            AuthConfig.ISSUER_URI,
            { config, ex ->
                runOnUiThread {
                    if (config == null) {
                        Log.e("LoginActivity", "fetchFromIssuer failed: $ex")
                        onAuthCancel()
                        return@runOnUiThread
                    }
                    launchAuthFlow(config)
                }
            },
            connectionBuilder
        )
    }

    private fun launchAuthFlow(config: AuthorizationServiceConfiguration) {
        val request = AuthorizationRequest.Builder(
            config,
            AuthConfig.CLIENT_ID,
            ResponseTypeValues.CODE,
            AuthConfig.REDIRECT_URI,
        )
            .setScopes("openid", "email", "profile", "offline_access")
            .build()
        authLauncher.launch(authService.getAuthorizationRequestIntent(request))
    }

    private fun exchangeCode(response: AuthorizationResponse) {
        Thread {
            try {
                val conn = URL(response.request.configuration.tokenEndpoint.toString())
                    .openConnection() as HttpURLConnection
                conn.apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 15_000
                    readTimeout = 10_000
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }

                val body = buildString {
                    append("grant_type=authorization_code")
                    append("&code=${response.authorizationCode}")
                    append("&redirect_uri=${AuthConfig.REDIRECT_URI}")
                    append("&client_id=${AuthConfig.CLIENT_ID}")
                    append("&code_verifier=${response.request.codeVerifier}")
                }
                conn.outputStream.use { it.write(body.toByteArray()) }

                val responseCode = conn.responseCode
                val rawJson = if (responseCode == 200) {
                    conn.inputStream.bufferedReader().readText()
                } else {
                    val error = conn.errorStream?.bufferedReader()?.readText() ?: "unknown"
                    Log.e("LoginActivity", "Token endpoint error $responseCode: $error")
                    runOnUiThread { onAuthCancel() }
                    return@Thread
                }

                Log.d("LoginActivity", "Token response: $rawJson")

                val json = JSONObject(rawJson)
                val accessToken  = json.getString("access_token")
                val refreshToken = json.optString("refresh_token")
                val idToken      = json.optString("id_token")

                runOnUiThread { saveAccount(accessToken, refreshToken, idToken) }

            } catch (e: Exception) {
                Log.e("LoginActivity", "exchangeCode failed", e)
                runOnUiThread { onAuthCancel() }
            }
        }.start()
    }

    private fun saveAccount(accessToken: String, refreshToken: String, idToken: String) {
        val name = idToken.takeIf { it.isNotEmpty() }
            ?.let { JWT(it).getClaim("preferred_username").asString() }
            ?: "taska"

        val accountManager = AccountManager.get(this)
        val account = Account(name, AuthConfig.ACCOUNT_TYPE)

        val exists = accountManager.getAccountsByType(AuthConfig.ACCOUNT_TYPE).any { it.name == name }
        if (!exists) {
            val added = accountManager.addAccountExplicitly(account, null, null)
            Log.d("LoginActivity", "addAccountExplicitly=$added name=$name")
        }

        accountManager.setAuthToken(account, AuthConfig.AUTH_TOKEN_TYPE, accessToken)
        accountManager.setUserData(account, "refresh_token", refreshToken)

        Log.d("LoginActivity", "accounts: ${accountManager.getAccountsByType(AuthConfig.ACCOUNT_TYPE).map { it.name }}")

        resultBundle = Bundle().apply {
            putString(AccountManager.KEY_ACCOUNT_NAME, name)
            putString(AccountManager.KEY_ACCOUNT_TYPE, AuthConfig.ACCOUNT_TYPE)
            putString(AccountManager.KEY_AUTHTOKEN, accessToken)
        }
        setResult(RESULT_OK, Intent().putExtras(resultBundle!!))
        finish()
    }

    private fun onAuthCancel() {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun finish() {
        accountAuthenticatorResponse?.let { response ->
            if (resultBundle != null) response.onResult(resultBundle)
            else response.onError(AccountManager.ERROR_CODE_CANCELED, "canceled")
            accountAuthenticatorResponse = null
        }
        super.finish()
    }

    override fun onDestroy() {
        authService.dispose()
        super.onDestroy()
    }
}