import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object AppState {
    var isSetUpCompleted: Boolean = false
    var isDarkMode = mutableStateOf(true)
}