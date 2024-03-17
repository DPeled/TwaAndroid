package chief.twa.android.authentication

import android.content.ComponentName
import com.idf.jar.UnitedAuthManager
import org.json.JSONObject
import java.util.Base64

const val APP_ID: Int = 1
class VisaManager {
    fun getAuthManager(): UnitedAuthManager {
        val mobilityPackageName = "com.mobility.opin"
        val mobilityClassName = "com.mobility.opin.services.OperCertService"
        val componentName = ComponentName(
                mobilityPackageName,
                mobilityClassName
        )
        return UnitedAuthManager(componentName)
    }

    companion object {
        fun decodeVisaToken(token: String): JSONObject {
            val tokenSplitToSections: List<String> = token.split(".")
            val decoder: Base64.Decoder = Base64.getUrlDecoder()
            val payloadSectionIndex = 1
            val decodedTokenPayload = String(decoder.decode(tokenSplitToSections[payloadSectionIndex]))

            return JSONObject(decodedTokenPayload)
        }
    }
}