package chief.twa.android.models

// status code 0 means custom error
enum class AuthProxyRequestErrors(val statusCode: Int, val errorMessage: String) {
    NO_TOKEN_SEND(204, "לא נשלח טוקן אשרה מבצעית"),
    AUTH_AGAINST_CLOUD_FAILED(403, "ההזדהות מול הענן לא הצליחה"),
    ERROR_IN_PROXY(500, "תקלה בפרוקסי הזדהות"),
    ERROR_IN_RESPONSE_DATA(0, "תשובה מפרוקסי הזדהות לא תקינה"),
    UNKNOWN(0, "תקלה כללית בתהליך ההזדהות")
}

enum class GeneralErrors(val errorMessage: String) {
    LAUNCHING_TWA("שגיאה בהעלאת אפליקציית צ'יף")
}


