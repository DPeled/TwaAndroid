package chief.twa.android.ui.activities_views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import chief.twa.android.models.AppLoadingStatuses
import chief.twa.android.models.AuthProxyRequestErrors
import chief.twa.android.models.GeneralErrors
import chief.twa.android.models.VisaStatuses
import chief.twa.android.ui.components.GeneralErrorMessage
import chief.twa.android.ui.components.LoadingStatusWithIcon
import chief.twa.android.ui.components.VisaAppNotInstalledMessage
import chief.twa.android.ui.components.VisaErrorPage

val mapVisaStatusToText = mapOf(
        VisaStatuses.NOT_VALID to "האשרה המבצעית שלך אינה תקינה",
        VisaStatuses.WAITING_FOR_USER to "על מנת להתחבר לצ'יף נדרש להזין אשרה מבצעית",
        VisaStatuses.EXPIRED to "האשרה המבצעית שלך פגה תוקף"
)

val mapAppLoadingStatusToText = mapOf(
        AppLoadingStatuses.GETTING_VISA to "בדיקת אשרה מבצעית...",
        AppLoadingStatuses.GET_AUTHORIZATION_PARAMS to "בדיקת הזדהות...",
        AppLoadingStatuses.UPLOADING_CHIEF to "מעלה את צ'יף..."
)

@Composable
fun appLoadingScreenByUserStage(
        visaLoadingStatusState: MutableState<VisaStatuses>,
        appLoadingStatusState: MutableState<AppLoadingStatuses>,
        authProxyErrorState: MutableState<AuthProxyRequestErrors?>,
        generalErrorsState: MutableState<GeneralErrors?>,
        retryVisaRegistration: () -> Unit) {
    val visaLoadingStatus by visaLoadingStatusState
    val appLoadingStatus by appLoadingStatusState
    val authProxyError by authProxyErrorState
    val generalError by generalErrorsState
    if (authProxyError != null || generalError != null) {
        GeneralErrorMessage(errorMessage = authProxyError?.errorMessage ?: (generalError?.errorMessage ?: ""))
    } else if (appLoadingStatus == AppLoadingStatuses.GETTING_VISA) {
        if (visaLoadingStatus === VisaStatuses.NOT_STARTED || visaLoadingStatus === VisaStatuses.APPLICATION_REGISTERED)
            LoadingStatusWithIcon(text = mapAppLoadingStatusToText[AppLoadingStatuses.GETTING_VISA] ?: "")
        else if (visaLoadingStatus === VisaStatuses.APPLICATION_NOT_EXISTS) {
            VisaAppNotInstalledMessage(retryVisaRegistration)
        } else {
            mapVisaStatusToText[visaLoadingStatus]?.let { loadingText -> VisaErrorPage(retryVisaRegistration, loadingText) }
        }
    } else {
        mapAppLoadingStatusToText[appLoadingStatus]?.let {
            LoadingStatusWithIcon(text = it)
        }
    }
}
