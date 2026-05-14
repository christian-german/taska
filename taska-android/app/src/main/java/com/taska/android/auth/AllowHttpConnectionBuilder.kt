package com.taska.android.auth

import android.net.Uri
import net.openid.appauth.connectivity.ConnectionBuilder
import java.net.HttpURLConnection
import java.net.URL

class AllowHttpConnectionBuilder : ConnectionBuilder {

    override fun openConnection(uri: Uri): HttpURLConnection {
        val url = URL(uri.toString())
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 10_000
        return conn
    }
}