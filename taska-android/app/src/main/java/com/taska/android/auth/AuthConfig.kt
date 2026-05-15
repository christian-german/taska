package com.taska.android.auth

import android.net.Uri
import com.taska.android.BuildConfig

object AuthConfig {
    val ISSUER_URI: Uri = Uri.parse(BuildConfig.OIDC_ISSUER_URL)
    val CLIENT_ID: String = BuildConfig.OIDC_CLIENT_ID
    val REDIRECT_URI: Uri = Uri.parse(BuildConfig.OIDC_REDIRECT_URI)

    val ACCOUNT_TYPE: String = BuildConfig.ACCOUNT_TYPE
    const val AUTH_TOKEN_TYPE = "Bearer"
}
