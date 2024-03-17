package chief.twa.android.network

import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object Http {
    val client: OkHttpClient

    init {
        val trustAllCerts: Array<TrustManager> = arrayOf(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                    ) {
                    }

                    override fun checkServerTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
        )
        SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, SecureRandom())
        }

        client = OkHttpClient.Builder()
                .sslSocketFactory(
                        TLSSocketFactory(trustAllCerts),
                        trustAllCerts[0] as X509TrustManager
                )
                .hostnameVerifier(HostnameVerifier(({ _, _ -> true })))
                .build()
    }
}