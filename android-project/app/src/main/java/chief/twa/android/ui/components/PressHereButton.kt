import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PressHereButton(
        onClick: () -> Unit
) {
    Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            onClick = onClick,
            modifier = Modifier
                    .border(2.dp, Color.White, shape = RoundedCornerShape(4.dp))
    ) {
        Surface(
                color = Color.Transparent,
                contentColor = contentColorFor(Color.Gray)
        ) {
            Text(
                    text = "לחצו כאן",
                    color = Color.White,
                    style = TextStyle(fontSize = 23.sp)
            )
        }
    }
}