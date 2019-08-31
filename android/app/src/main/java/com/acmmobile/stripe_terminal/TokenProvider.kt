package com.acmmobile

import com.stripe.stripeterminal.ConnectionTokenCallback
import com.stripe.stripeterminal.ConnectionTokenException
import com.stripe.stripeterminal.ConnectionTokenProvider

/**
 * A simple implementation of the [ConnectionTokenProvider] interface. We just request a
 * new token from our backend simulator and forward any exceptions along to the SDK.
 */
class TokenProvider(URL: String) : ConnectionTokenProvider {
    private val backendURL = URL

    override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
        try {
            val token = ApiClient.createConnectionToken(backendURL)
            callback.onSuccess(token)
        } catch (e: ConnectionTokenException) {
            callback.onFailure(e)
        }
    }
}