package chief.twa.android

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import chief.twa.android.authentication.APP_ID
import chief.twa.android.authentication.VisaManager
import chief.twa.android.authentication.VisaManagerActivity
import chief.twa.android.models.Logger.log
import chief.twa.android.models.TwaIntent
import chief.twa.android.models.VisaManagerActivityIntent
import com.google.androidbrowserhelper.trusted.LauncherActivity
import com.idf.jar.UnitedAuthManager
import com.msi.authservice.AuthStatusListener

class TwaLauncherActivity : LauncherActivity(), AuthStatusListener {
    private var mAuthManager: UnitedAuthManager? = null
    private var isVisaAlreadyValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        log("TwaLauncherActivity onCreate")
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        // we want to listen when the visa is expired so we can exit from the twa
        // and show in mobile appropriate message page
        registerVisaListener()
    }

    override fun onDestroy() {
        log("TwaLauncherActivity onDestroy")
        mAuthManager?.unregister()
        super.onDestroy()
    }

    override fun getLaunchingUrl(): Uri {
        log("Getting launching url")
        val intent = intent
        val authorizationCode: String? = intent.getStringExtra(TwaIntent.AUTH_CODE)
        val codeVerifier: String? = intent.getStringExtra(TwaIntent.CODE_VERIFIER)
        val visaTokenExpiration: String? = intent.getStringExtra(TwaIntent.VISA_TOKEN_EXPIRATION)
        val originalUri = super.getLaunchingUrl()
        log("Original Uri: $originalUri")

        val modifiedUri: Uri = appendQueryParameters(
                originalUri,
                authorizationCode,
                codeVerifier,
                visaTokenExpiration
        )

        log("Modified Uri: $modifiedUri")
        return modifiedUri
    }

    private fun appendQueryParameters(
            originalUri: Uri,
            authorizationCode: String?,
            codeVerifier: String?,
            visaTokenExpiration: String?
    ): Uri {
        val builder = originalUri.buildUpon()
        builder.appendPath("auth").appendPath("mobile")
        builder.appendQueryParameter("code", authorizationCode)
        builder.appendQueryParameter("session_state", codeVerifier)
        builder.appendQueryParameter("visaTokenExpiration", visaTokenExpiration)
        return builder.build()
    }

    private fun startVisaManagerActivityAfterVisaExpiration() {
        val intent = Intent(this, VisaManagerActivity::class.java)
        intent.putExtra(
                VisaManagerActivityIntent.TRIGGERED_FROM_TWA_BECAUSE_EXPIRED_VISA,
                "true"
        )
        startActivity(intent)
        finish()
    }

    private fun registerVisaListener() {
        mAuthManager?.unregister() ?: run {
            val visaManager = VisaManager()
            mAuthManager = visaManager.getAuthManager()
        }
        // if we got to here, this means the application of visa must be installed,
        // so no need to check again
        mAuthManager?.register(APP_ID, this)
    }

    override fun onStatusReceive(status: Int) {
        log("In TwaLauncherActivity, received visa status $status, isVisaAlreadyValid=$isVisaAlreadyValid")
        /*
        We might receive status 0 right after register, so we don't want to finish the TWA
        If the visa hasn't been validated yet
         */
        if (isVisaAlreadyValid && status == UnitedAuthManager.AUTH_IND_OP_ACCESS_NOT_EXIST) {
            log("Returning to VisaManagerActivity")
            startVisaManagerActivityAfterVisaExpiration()
        } else if (!isVisaAlreadyValid && status == UnitedAuthManager.AUTH_IND_OP_ACCESS_EXIST) {
            isVisaAlreadyValid = true
        }
    }
}
