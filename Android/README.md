# Smart Irrigation - Android App

Modern Android application for monitoring and controlling an IoT irrigation system with AI-powered threshold recommendations.

---

## 🎯 Overview

The Smart Irrigation Android app provides real-time monitoring and control of your irrigation system. It communicates with an ESP8266 microcontroller to:
- Monitor soil moisture levels continuously
- Control water pump (manual/automatic modes)
- Set custom moisture thresholds
- Get AI-powered irrigation recommendations *(planned)*

---

## ✨ Features

### 🌡️ Real-time Dashboard
- **Live Monitoring**: Soil moisture percentage updated via Server-Sent Events (SSE)
- **Pump Status**: Current state (ON/OFF) with visual indicators
- **Connection Status**: Network connectivity with auto-reconnection
- **Threshold Display**: Current moisture threshold setting

### 💧 Control Modes

#### Automatic Mode
- ESP8266 autonomously controls pump based on threshold
- Pump turns ON when moisture < threshold
- Pump turns OFF when moisture ≥ threshold
- Set-and-forget irrigation

#### Manual Mode
- Direct pump control from app
- Override automatic behavior
- Instant ON/OFF toggle
- Useful for testing or manual watering

### �️ Threshold Management
- Set custom moisture thresholds (0-100%)
- Real-time threshold updates to ESP8266
- Visual feedback on changes
- Persistent storage of preferences

### 🤖 AI Chatbot Assistant *(Planned Feature)*

The chatbot will provide intelligent irrigation recommendations:

**Inputs:**
- **Plant Name**: Type of plant (e.g., "Tomato", "Rose", "Cactus")
- **Location**: City/region for climate context

**AI Analysis:**
- Analyzes plant water requirements from database
- Considers local climate conditions (temperature, humidity, rainfall)
- Calculates optimal moisture threshold
- Provides reasoning for recommendation

**Outputs:**
- **Threshold Recommendation**: Optimal percentage (e.g., "45%")
- **Explanation**: Why this threshold is ideal
- **Auto-Set Option**: One-tap threshold configuration

**Example Conversation:**
```
User: "I'm growing tomatoes in Mumbai"

AI: "Tomatoes require moderate, consistent moisture. 
     In Mumbai's humid climate with monsoon season, 
     I recommend a threshold of 45%.
     
     This ensures:
     ✓ Adequate moisture for fruit development
     ✓ Prevention of overwatering during monsoons
     ✓ Optimal root health
     
     Shall I set this threshold for you?"

User: "Yes, please"

AI: "✓ Threshold set to 45%
     Your tomatoes will be watered when soil moisture 
     drops below 45%. Happy growing! 🍅"
```

### ⚙️ Settings & Preferences
- Plant name configuration
- Dark mode support
- Notification preferences
- Permission management

### 🔔 Background Monitoring
- Foreground service for persistent connection
- Pump status notifications
- Connection alerts
- Battery-optimized operation

---

## 🛠️ Technology Stack

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture + MVVM
- **Dependency Injection**: Dagger Hilt
- **Async Operations**: Kotlin Coroutines + Flow
- **Navigation**: Jetpack Navigation Compose (type-safe routes)
- **Local Storage**: DataStore Preferences
- **Networking**: Ktor Client (OkHttp engine)
- **Serialization**: Kotlinx Serialization

### Key Libraries
```kotlin
// Compose
androidx.compose.material3
androidx.compose.material.icons.extended

// Hilt DI
com.google.dagger.hilt.android
androidx.hilt.navigation.compose

// Ktor Client
io.ktor.client.core
io.ktor.client.okhttp
io.ktor.client.plugins.sse
io.ktor.serialization.kotlinx.json

// DataStore
androidx.datastore.preferences

// Other
androidx.core.splashscreen
androidx.lifecycle.viewmodel.compose
```

---

## 📂 Project Structure

```
app/src/main/java/com/example/smartirrigation/
├── MainActivity.kt                    # Entry point
├── common/
│   └── AppState.kt                   # Global app state
├── data/
│   ├── local/
│   │   └── preferences/
│   │       └── DatastoreManager.kt   # Local storage
│   ├── network/
│   │   └── dto/                      # Data Transfer Objects
│   │       ├── IrrigatorInfo.kt      # Device status
│   │       ├── Mode.kt               # Control mode
│   │       ├── PumpStatus.kt         # Pump state
│   │       └── Threshold.kt          # Moisture threshold
│   └── repositories/
│       ├── IrrigationRepoImpl.kt     # Network operations
│       └── PreferencesRepoImpl.kt    # Local data ops
├── domain/
│   └── repositories/                 # Repository interfaces
│       ├── IrrigationRepository.kt
│       └── PreferencesRepository.kt
├── di/
│   ├── AppModule.kt                  # Hilt DI module
│   └── MyApplication.kt              # Application class
└── presentation/
    ├── dashboard/                    # Main monitoring screen
    │   ├── components/               # UI components
    │   ├── foreground_service/       # Background service
    │   ├── screen/
    │   ├── state/
    │   └── viewmodels/
    ├── chatbot/                      # AI assistant (planned)
    │   └── screens/
    ├── settings/                     # Settings screen
    ├── setup/                        # Initial setup flow
    ├── navigation/                   # App navigation
    ├── permission/                   # Permission handling
    ├── ui/
    │   └── theme/                    # Material 3 theming
    └── utils/
```

---

## 🚀 Build & Run

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 11 or higher
- **Android SDK**: API 24+ (Android 7.0+)
- **ESP8266**: Running and connected to WiFi

### Setup Steps

1. **Open Project**
   ```bash
   # Open Android Studio
   # File → Open → Select Smart_Irrigation/Android/
   ```

2. **Sync Dependencies**
   - Android Studio will automatically sync Gradle
   - Wait for build to complete

3. **Configure Device IP**
   
   Update ESP8266 IP address in `IrrigationRepoImpl.kt`:
   ```kotlin
   // File: data/repositories/IrrigationRepoImpl.kt
   
   override suspend fun getStatus(): Flow<IrrigatorInfo?> = flow {
       httpClient.prepareGet("http://192.168.1.150/sse") {  // ← Change this IP
           // ...
       }
   }
   
   // Also update in other methods:
   // - setThreshold()
   // - setControlMode()
   // - turnOnPump()
   ```

4. **Update Authentication** *(if changed on ESP8266)*
   ```kotlin
   // File: data/repositories/IrrigationRepoImpl.kt
   
   header("Authorization", "Bearer myStrongAdminKey123")  // ← Update token
   ```

5. **Build & Install**
   - Connect Android device via USB (or use emulator)
   - Click **Run** (▶️) or press `Shift + F10`
   - Grant required permissions when prompted

---

## 🔌 ESP8266 Communication

### API Endpoints

The app communicates with ESP8266 at `http://<ESP_IP>`:

| Endpoint | Method | Purpose | Request Body | Auth |
|----------|--------|---------|--------------|------|
| `/sse` | GET | Real-time updates stream | - | No |
| `/setThreshold` | POST | Set moisture threshold | `{"threshold": 512}` | Yes |
| `/setMode` | POST | Switch manual/auto | `{"mode": true}` | Yes |
| `/setPump` | POST | Control pump | `{"pumpStatus": true}` | Yes |

### SSE Stream Format
```json
data: {
  "threshold": 512,        // 0-1024 (converted to %)
  "soilMoisture": 650,     // 0-1024 (converted to %)
  "relayStatus": false,    // Pump ON/OFF
  "mode": true            // true=manual, false=auto
}
```

### Data Flow

```
ESP8266 (SSE Stream)
       ↓
Ktor HttpClient
       ↓
IrrigationRepository
       ↓
DashboardViewModel (StateFlow)
       ↓
DashboardScreen (Compose UI)
       ↓
User sees real-time updates
```

---

## 🏗️ Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────┐
│      Presentation Layer             │
│  (UI + ViewModels + State)          │
│  - Jetpack Compose                  │
│  - ViewModels with StateFlow        │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│       Domain Layer                  │
│  (Business Logic Interfaces)        │
│  - Repository Interfaces            │
│  - Use Cases (future)               │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│        Data Layer                   │
│  (Repository Implementations)       │
│  - Network (Ktor)                   │
│  - Local (DataStore)                │
└─────────────────────────────────────┘
```

### MVVM Pattern

**ViewModel** manages UI state and business logic:
```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    val repository: IrrigationRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()
    
    fun onWaterPumpToggle() {
        viewModelScope.launch {
            repository.turnOnPump(!currentState)
        }
    }
}
```

**Composable** observes state and renders UI:
```kotlin
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    
    // UI automatically updates when state changes
}
```

---

## 🧪 Development

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

### Building Release APK
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Document complex logic with comments

---

## 🔐 Permissions

Required permissions in `AndroidManifest.xml`:

```xml
<!-- Network communication -->
<uses-permission android:name="android.permission.INTERNET"/>

<!-- Background service -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC"/>

<!-- Notifications -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

<!-- Location (for WiFi scanning) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

---

## 🐛 Troubleshooting

### Connection Issues

**Problem**: App shows "Disconnected" banner

**Solutions**:
1. Verify ESP8266 is powered on and connected to WiFi
2. Check both devices are on same network
3. Confirm IP address in `IrrigationRepoImpl.kt` is correct
4. Ping ESP8266 from terminal: `ping 192.168.1.150`
5. Check router firewall settings

### Build Errors

**Problem**: Gradle sync fails

**Solutions**:
1. `File → Invalidate Caches → Invalidate and Restart`
2. Delete `.gradle` and `.idea` folders, reopen project
3. Update Android Studio to latest version
4. Check internet connection for dependency downloads

### SSE Stream Not Working

**Problem**: No real-time updates

**Solutions**:
1. Check ESP8266 serial logs for SSE endpoint activity
2. Verify Ktor SSE plugin is installed
3. Test SSE endpoint manually: `curl http://ESP_IP/sse`
4. Check network timeout settings in `AppModule.kt`

### Pump Not Responding

**Problem**: Toggle button doesn't control pump

**Solutions**:
1. Verify you're in **Manual Mode**
2. Check bearer token matches ESP8266 configuration
3. Review ESP8266 serial logs for POST requests
4. Test endpoint manually: `curl -X POST http://ESP_IP/setPump -H "Authorization: Bearer TOKEN" -d '{"pumpStatus":true}'`

---

## 🚧 Future Enhancements

### AI Chatbot Implementation
- [ ] Integrate Gemini API / OpenAI
- [ ] Build plant database (water requirements)
- [ ] Add weather API integration
- [ ] Implement conversational UI
- [ ] Add auto-threshold setting logic

### Additional Features
- [ ] Historical data charts (moisture over time)
- [ ] Irrigation scheduling (time-based watering)
- [ ] Multiple device support
- [ ] Water usage analytics
- [ ] Export data to CSV
- [ ] Cloud backup (optional)

---

## 📄 License

See [LICENSE](../LICENSE) file for details.

---

## 📞 Support

- **Issues**: Check ESP8266 serial logs and app Logcat
- **Documentation**: See root [README.md](../README.md)
- **Hardware Setup**: See [Arduino/README.md](../Arduino/README.md)

---

**Happy Irrigating! 🌱💧**
