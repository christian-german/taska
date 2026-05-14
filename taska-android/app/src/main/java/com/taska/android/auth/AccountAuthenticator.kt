package com.taska.android.auth

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle

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
        val cached = AccountManager.get(context).peekAuthToken(account, authTokenType)
        if (!cached.isNullOrEmpty()) {
            return Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                putString(AccountManager.KEY_AUTHTOKEN, cached)
            }
        }
        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, loginIntent(response, account.type))
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
