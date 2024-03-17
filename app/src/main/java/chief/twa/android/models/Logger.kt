package chief.twa.android.models

object Logger {
    @JvmStatic fun log(message: String) {
        println("[CHIEF] $message")
    }
}