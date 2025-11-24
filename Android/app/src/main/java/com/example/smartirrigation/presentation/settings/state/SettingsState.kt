data class SettingsState(
    val isDarkModeEnabled: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val plantName: String = "",
    val userLocation: String = "",
    val showPlantDialog: Boolean = false,
    val showLocationDialog: Boolean = false
)
