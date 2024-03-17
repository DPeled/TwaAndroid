package chief.twa.android.ui.components

import PressHereButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chief.twa.android.ui.theme.messagePageBackground

@Composable
fun VisaErrorPage(retryVisaRegistration: () -> Unit, mainMessage: String) {
    Surface(
            modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
            color = messagePageBackground
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                    modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .absolutePadding(top = 100.dp, right = 28.dp, left = 28.dp, bottom = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                        text = mainMessage,
                        color= Color.White,
                        style = TextStyle(fontSize = 33.sp, fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                        text = "למעבר לאפליקציה $visaApplicationName :",
                        color= Color.White,
                        style = TextStyle(fontSize = 24.sp),
                        textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                PressHereButton(onClick = retryVisaRegistration)
            }
        }
    }
}

