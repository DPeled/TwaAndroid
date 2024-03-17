package chief.twa.android.authentication

import chief.twa.android.ui.activities_views.appLoadingScreenByUserStage
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import chief.twa.android.models.Logger.log
import chief.twa.android.models.TwaIntent
import chief.twa.android.TwaLauncherActivity
import chief.twa.android.authentication.VisaManager.Companion.decodeVisaToken
import chief.twa.android.models.AppLoadingStatuses
import chief.twa.android.models.AuthProxyRequestErrors
import chief.twa.android.models.GeneralErrors
import chief.twa.android.models.VisaManagerActivityIntent
import chief.twa.android.models.VisaStatuses
import com.idf.jar.UnitedAuthManager
import com.msi.authservice.AuthStatusListener
import okhttp3.Response
import javax.net.ssl.HttpsURLConnection

data class AuthParamsFromAuthProxy(val authCode: String, val codeVerifier: String)

class VisaManagerActivity : ComponentActivity(), AuthStatusListener {
    private var mAuthManager: UnitedAuthManager? = null
    private var visaLoadingStatusState: MutableState<VisaStatuses> =
            mutableStateOf(VisaStatuses.NOT_STARTED)
    private var appLoadingStatusState: MutableState<AppLoadingStatuses> =
            mutableStateOf(AppLoadingStatuses.GETTING_VISA)
    private var authProxyErrorState: MutableState<AuthProxyRequestErrors?> =
            mutableStateOf(null)
    private var generalErrorsState: MutableState<GeneralErrors?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        log("VisaManagerActivity onCreate")
        super.onCreate(savedInstanceState)

        val isTriggeredFromTwaBecauseVisaExpired: Boolean = intent.getStringExtra(
                VisaManagerActivityIntent.TRIGGERED_FROM_TWA_BECAUSE_EXPIRED_VISA
        ) != null

        if (isTriggeredFromTwaBecauseVisaExpired) {
            visaLoadingStatusState.value = VisaStatuses.EXPIRED
        }

        val retryVisaRegistration: () -> Unit = { registerVisaListener() }
        setContent {
            appLoadingScreenByUserStage(
                    visaLoadingStatusState,
                    appLoadingStatusState,
                    authProxyErrorState,
                    generalErrorsState,
                    retryVisaRegistration
            )
        }

        // if we got to here from expired visa while being in twa,
        // we want to wait for the user to click to start the registration
        if (!isTriggeredFromTwaBecauseVisaExpired) {
            registerVisaListener()
        }
    }

    override fun onDestroy() {
        log("VisaManagerActivity onDestroy")
        mAuthManager?.unregister()
        super.onDestroy()
    }

    private fun registerVisaListener() {
        mAuthManager?.unregister() ?: run {
            val visaManager = VisaManager()
            mAuthManager = visaManager.getAuthManager()
        }
        val isRegistered = mAuthManager?.register(APP_ID, this) ?: false
        if (!isRegistered) {
            visaLoadingStatusState.value = VisaStatuses.APPLICATION_NOT_EXISTS
            log("Visa app is not installed on device")
        } else {
            val isAlreadyInWaitingForUserMode: Boolean =
                    visaLoadingStatusState.value == VisaStatuses.WAITING_FOR_USER
            if (!isAlreadyInWaitingForUserMode) {
                visaLoadingStatusState.value = VisaStatuses.APPLICATION_REGISTERED
                updateStatusAfterDelayIfNeeded()
            }

        }
    }

    override fun onStatusReceive(status: Int) {
        try {
            log("Received visa status: $status")
            if (status == UnitedAuthManager.AUTH_IND_OP_ACCESS_EXIST) {
                val mToken = mAuthManager?.token
                mToken?.let {
                    log("Received valid visa, current state is ${visaLoadingStatusState.value}")
                    if (visaLoadingStatusState.value != VisaStatuses.VALID) {
                        handleVisaTokenReceived(it)
                    }
                } ?: run {
                    log("Visa is null")
                    visaLoadingStatusState.value = VisaStatuses.NOT_VALID
                }
            } else if (status == UnitedAuthManager.AUTH_IND_OP_ACCESS_NOT_EXIST) {
                log("Visa does not exist")
                visaLoadingStatusState.value = VisaStatuses.WAITING_FOR_USER
            }
        } catch (e: Exception) {
            log("Error in trying to fetch visa token. exp: ${e.message}")
            visaLoadingStatusState.value = VisaStatuses.NOT_VALID
        }
    }

    private fun updateStatusAfterDelayIfNeeded() {
        val timeTillCheck: Long = 2 * 1000
        val handler = Handler(Looper.getMainLooper())
        val checkIfStatusStuckInApplicationRegistered = {
            if (visaLoadingStatusState.value == VisaStatuses.APPLICATION_REGISTERED) {
                /* we check after a small period of time if the status is still APPLICATION_REGISTERED
                 which means that the user still hasn't entered visa and not has one from before
                 so we want to show him appropriate message page */
                visaLoadingStatusState.value = VisaStatuses.WAITING_FOR_USER
            }
        }

        handler.postDelayed(checkIfStatusStuckInApplicationRegistered, timeTillCheck)
    }

    private fun handleVisaTokenReceived(visaToken: String) {
        visaLoadingStatusState.value = VisaStatuses.VALID
        appLoadingStatusState.value = AppLoadingStatuses.GET_AUTHORIZATION_PARAMS
        log("Visa: '$visaToken'")
        val visaTokenPayload = decodeVisaToken(visaToken)
        val visaTokenExpiration: Any? = visaTokenPayload.get("exp")
        log("visa expiration: $visaTokenExpiration")

        val authorizationCodeResponseFuture =
                SsoAuthenticationInitialization().getAuthorizationCode(visaToken)
        log("Sent request to get authorization code")
        authorizationCodeResponseFuture.thenAccept { response: Response ->
            val statusCode = response.code
            log("Got response from auth proxy with status code $statusCode")
            if (statusCode == HttpsURLConnection.HTTP_OK) {
                try {
                    handleAuthProxyResponseReceived(response, visaTokenExpiration.toString())
                } catch (e: Exception) {
                    authProxyErrorState.value = AuthProxyRequestErrors.ERROR_IN_RESPONSE_DATA
                }
            } else {
                val matchingErrorDetails: AuthProxyRequestErrors? =
                        AuthProxyRequestErrors.values().find { requestError ->
                            requestError.statusCode == statusCode
                        }
                when (matchingErrorDetails) {
                    null -> authProxyErrorState.value = AuthProxyRequestErrors.UNKNOWN
                    else -> authProxyErrorState.value = matchingErrorDetails
                }
            }
        }.exceptionally { ex: Throwable ->
            log("Error from auth proxy: $ex")
            authProxyErrorState.value = AuthProxyRequestErrors.UNKNOWN
            null
        }
    }

    private fun handleAuthProxyResponseReceived(response: Response, visaTokenExpiration: String) {
        val authParamsFromURLResult =
                SsoAuthenticationInitialization.extractAuthParamsFromAuthProxyResponse(response)
        authParamsFromURLResult?.authCode?.let {
            launchTwaActivity(it, authParamsFromURLResult.codeVerifier, visaTokenExpiration)
        } ?: run {
            authProxyErrorState.value = AuthProxyRequestErrors.ERROR_IN_RESPONSE_DATA
        }
    }

    private fun launchTwaActivity(
            authorizationCode: String,
            codeVerifier: String,
            visaTokenExpiration: String
    ) {
        try {
            Handler(Looper.getMainLooper()).post {
                log("Launching TWA activity")
                appLoadingStatusState.value = AppLoadingStatuses.UPLOADING_CHIEF
                val intent = Intent(this, TwaLauncherActivity::class.java)
                intent.putExtra(TwaIntent.AUTH_CODE, authorizationCode)
                intent.putExtra(TwaIntent.CODE_VERIFIER, codeVerifier)
                intent.putExtra(TwaIntent.VISA_TOKEN_EXPIRATION, visaTokenExpiration)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            log("Error launching TWA activity. error: ${e.message}")
            generalErrorsState.value = GeneralErrors.LAUNCHING_TWA
        }
    }
}