package chief.twa.android.authentication

object AuthProxyAddressManager {
    private const val currentCargoHost = "chief.np.idf.cts"

    // np does not have DR
    private val availableAuthProxyAddresses = if (currentCargoHost.contains(".np.")) {
        listOf("chief.np.idf.cts:3001")
    } else {
        listOf("80.29.65.213:4004", "81.29.65.213:4004")
    }

    private var lastUsedAddress: String? = null

    /**
     * This function is responsible for choosing an ip address for the request to auth proxy
     * First time it chooses randomly;
     * If we reached second time, it chooses the one that was not chosen before
     */
    fun getAddress(): String {
        val randomHost = availableAuthProxyAddresses.shuffled().first()

        val addressToUse = lastUsedAddress?.let {
            availableAuthProxyAddresses.find { address ->
                address != lastUsedAddress
            } ?: randomHost
        } ?: randomHost

        lastUsedAddress = addressToUse
        return addressToUse
    }
}
