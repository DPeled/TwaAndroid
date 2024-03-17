package chief.twa.android.network

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager

class TLSSocketFactory @Throws(KeyManagementException::class, NoSuchAlgorithmException::class)
constructor(trustManagers: Array<out TrustManager>? = null) : SSLSocketFactory() {

    private var internalSSLSocketFactory: SSLSocketFactory

    init {
        val context = SSLContext.getInstance("TLS")
        context.init(null, trustManagers, null)
        internalSSLSocketFactory = context.socketFactory
    }

    override fun getDefaultCipherSuites(): Array<String> =
            internalSSLSocketFactory.defaultCipherSuites


    override fun getSupportedCipherSuites(): Array<String> =
            internalSSLSocketFactory.supportedCipherSuites


    @Throws(IOException::class)
    override fun createSocket(): Socket? =
            enableTLSOnSocket(internalSSLSocketFactory.createSocket())


    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket? =
            enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose))


    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket? =
            enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))


    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket? =
            enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort))


    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket? =
            enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))


    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket? =
            enableTLSOnSocket(internalSSLSocketFactory.createSocket(address, port, localAddress, localPort))


    private fun enableTLSOnSocket(socket: Socket?): Socket? {
        if (socket != null && socket is SSLSocket) {
            socket.enabledProtocols = arrayOf("TLSv1.1", "TLSv1.2")
        }
        return socket
    }
}