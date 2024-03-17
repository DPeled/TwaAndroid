package chief.twa.android.authentication

import android.net.Uri
import chief.twa.android.models.Logger.log
import chief.twa.android.network.Http.client
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CompletableFuture

internal class SsoAuthenticationInitialization {
    var httpClient = client
    fun getAuthorizationCode(visaToken: String): CompletableFuture<Response> {
        return executeAuthorizationRequest(visaToken)
    }

    fun executeAuthorizationRequest(visaToken: String): CompletableFuture<Response> {
        val authorizationRequest = getAuthorizationRequest(visaToken)
        val authorizationRequestFuture = CompletableFuture<Response>()
        log("Making request to auth proxy: " +
                authorizationRequest.url +
                ", " +
                authorizationRequest.header(AUTHORIZATION_HEADER)
        )
        httpClient.newCall(authorizationRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                authorizationRequestFuture.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                authorizationRequestFuture.complete(response)
            }
        })
        return authorizationRequestFuture
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private fun getAuthorizationRequest(visaToken: String): Request {
            // ip: "https://80.29.65.50:4004/auth"
            // dns: "https://sso.cargo.idf.cts:4004/auth"
            val AUTH_PROXY_URL = "https://" + AuthProxyAddressManager.getAddress() + "/auth"
            return Request.Builder()
                    .url(AUTH_PROXY_URL)
                    .addHeader(AUTHORIZATION_HEADER, "Bearer $visaToken")
                    .get()
                    .build()
        }

        fun extractAuthParamsFromAuthProxyResponse(response: Response): AuthParamsFromAuthProxy? {
            val LOCATION_HEADER = "Location"
            val CODE_PARAM = "code"
            val VERIFIER_PARAM = "session_state"
            val urlString = response.header(LOCATION_HEADER)
            log("Received url header from auth proxy: $urlString")
            return if (urlString != null) {
                val authUri: Uri = Uri.parse(urlString)
                val authCode: String = authUri.getQueryParameter(CODE_PARAM) ?: ""
                val codeVerifier: String = authUri.getQueryParameter(VERIFIER_PARAM) ?: ""
                log("authCode=$authCode, codeVerifier=$codeVerifier")
                AuthParamsFromAuthProxy(authCode, codeVerifier)
            } else {
                null
            }
        }
    }
}