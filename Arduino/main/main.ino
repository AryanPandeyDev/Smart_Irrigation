#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <EEPROM.h>

// ===== Hardware =====
const int soilMoisturePin = A0;
const int relayPin = D1;

// ===== Defaults / Config =====
int threshold = 700;
bool relayStatus = false;
String wifiSSID = "";
String wifiPassword = "";
bool mode = false; // false = auto, true = manual

// Admin key (for production, consider storing in EEPROM instead)
const String ADMIN_KEY = "myStrongAdminKey123"; // change this before deploying

// ===== Static IP (optional) =====
IPAddress staticIP(192, 168, 1, 150);
IPAddress gateway(192, 168, 1, 1);
IPAddress subnet(255, 255, 255, 0);

// ===== Web Server =====
ESP8266WebServer server(80);
WiFiClient sseClient;

// ===== EEPROM =====
#define EEPROM_SIZE 128
#define ADDR_SSID 0
#define ADDR_PASS 32
#define ADDR_THRESHOLD 96
#define ADDR_MODE 98 // store mode

// ===== Helpers =====
String readEEPROMString(int start, int maxLength) {
  String s = "";
  for (int i = 0; i < maxLength; i++) {
    char c = EEPROM.read(start + i);
    if (c == 0) break;
    s += c;
  }
  return s;
}

void writeEEPROMString(int start, int maxLength, const String& str) {
  for (int i = 0; i < maxLength; i++) {
    if (i < str.length()) {
      EEPROM.write(start + i, str[i]);
    } else {
      EEPROM.write(start + i, 0);
    }
  }
}

// ===== Authentication =====
bool checkAuth() {
  if (server.hasHeader("Authorization")) {
    String auth = server.header("Authorization");
    auth.trim();
    if (auth.startsWith("Bearer ")) {
      String key = auth.substring(7);
      if (key == ADMIN_KEY) return true;
    } else if (auth == ADMIN_KEY) return true;
  }
  if (server.hasArg("key") && server.arg("key") == ADMIN_KEY) return true;
  return false;
}

void sendJson(int code, const String& body) {
  server.send(code, "application/json", body);
}

// ===== Wi-Fi Setup Page (GET) =====
void handleWiFiRoot() {
  String page = "<!DOCTYPE html><html><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'>";
  page += "<style>body{font-family:Arial;margin:20px}label{display:block;margin-top:8px}input{width:100%;padding:8px;margin-top:4px}</style>";
  page += "</head><body>";
  page += "<h2>Wi-Fi Setup</h2>";
  page += "<form action='/saveWifi' method='POST'>";
  page += "<label>SSID<input type='text' name='ssid' maxlength='32' required></label>";
  page += "<label>Password<input type='password' name='password' maxlength='64'></label>";
  page += "<label>Admin Key (for testing) <input type='password' name='key' required></label>";
  page += "<button type='submit'>Save</button></form>";
  page += "<p>Note: do not keep admin key in plain HTML for production.</p>";
  page += "</body></html>";
  server.send(200, "text/html", page);
}

// ===== Save Wi-Fi (POST, supports JSON or form) =====
void handleSaveWiFi() {
  if (!checkAuth()) {
    sendJson(401, "{\"error\":\"Unauthorized\"}");
    return;
  }

  String ssid = "";
  String pass = "";

  if (server.hasArg("ssid")) {
    ssid = server.arg("ssid");
    pass = server.hasArg("password") ? server.arg("password") : "";
  } else {
    String body = server.hasArg("plain") ? server.arg("plain") : "";
    if (body.length() > 0) {
      int s1 = body.indexOf("\"ssid\"");
      int c1 = body.indexOf(':', s1);
      if (c1 > 0) {
        int q1 = body.indexOf('"', c1);
        int q2 = body.indexOf('"', q1 + 1);
        if (q1 >= 0 && q2 > q1) ssid = body.substring(q1 + 1, q2);
      }
      int s2 = body.indexOf("\"password\"");
      int c2 = body.indexOf(':', s2);
      if (c2 > 0) {
        int q3 = body.indexOf('"', c2);
        int q4 = body.indexOf('"', q3 + 1);
        if (q3 >= 0 && q4 > q3) pass = body.substring(q3 + 1, q4);
      }
    }
  }

  if (ssid.length() == 0 || ssid.length() > 32 || pass.length() > 64) {
    sendJson(400, "{\"error\":\"Invalid SSID/password length\"}");
    return;
  }

  writeEEPROMString(ADDR_SSID, 32, ssid);
  writeEEPROMString(ADDR_PASS, 32, pass);
  EEPROM.commit();

  sendJson(200, "{\"status\":\"saved\",\"rebooting\":true}");
  delay(500);
  ESP.restart();
}

// ===== Reset (POST, protected) =====
void handleReset() {
  if (!checkAuth()) {
    sendJson(401, "{\"error\":\"Unauthorized\"}");
    return;
  }

  for (int i = 0; i < EEPROM_SIZE; i++) EEPROM.write(i, 0);
  EEPROM.commit();

  sendJson(200, "{\"status\":\"reset\",\"rebooting\":true}");
  delay(500);
  ESP.restart();
}

// ===== SSE (GET) =====
void handleSSE() {
  WiFiClient client = server.client();
  if (!client) return;

  client.print("HTTP/1.1 200 OK\r\n");
  client.print("Content-Type: text/event-stream\r\n");
  client.print("Cache-Control: no-cache\r\n");
  client.print("Connection: keep-alive\r\n");
  client.print("Access-Control-Allow-Origin: *\r\n");
  client.print("\r\n");
  client.flush();

  sseClient = client;
  Serial.println("SSE client connected");
}

// ===== Set Threshold (POST) =====
void handleSetThreshold() {
  if (!checkAuth()) {
    sendJson(401, "{\"error\":\"Unauthorized\"}");
    return;
  }

  int newVal = -1;
  if (server.hasArg("value")) {
    newVal = server.arg("value").toInt();
  } else {
    String body = server.arg("plain");
    int p = body.indexOf("threshold");
    if (p >= 0) {
      int c = body.indexOf(':', p);
      if (c > 0) {
        String num = "";
        for (int i = c + 1; i < body.length(); i++) {
          char ch = body[i];
          if ((ch >= '0' && ch <= '9')) num += ch;
          else if (num.length() > 0) break;
        }
        if (num.length() > 0) newVal = num.toInt();
      }
    }
  }

  if (newVal < 0 || newVal > 1023) {
    sendJson(400, "{\"error\":\"Invalid threshold (0-1023)\"}");
    return;
  }

  threshold = newVal;
  EEPROM.write(ADDR_THRESHOLD, threshold & 0xFF);
  EEPROM.write(ADDR_THRESHOLD + 1, (threshold >> 8) & 0xFF);
  EEPROM.commit();

  sendJson(200, String("{\"status\":\"ok\",\"threshold\":") + String(threshold) + "}");
}

// ===== Set Mode (POST, boolean) =====
void handleSetMode() {
  if (!checkAuth()) {
    sendJson(401, "{\"error\":\"Unauthorized\"}");
    return;
  }

  bool newMode = mode;
  if (server.hasArg("mode")) {
    newMode = server.arg("mode") == "true";
  } else if (server.hasArg("plain")) {
    String body = server.arg("plain");
    newMode = body.indexOf("true") != -1;
  }

  mode = newMode;
  EEPROM.write(ADDR_MODE, mode ? 1 : 0);
  EEPROM.commit();

  sendJson(200, String("{\"status\":\"ok\",\"mode\":") + (mode ? "true" : "false") + "}");
}

// ===== Set Pump (POST, boolean) =====
void handleSetPump() {
  if (!checkAuth()) {
    sendJson(401, "{\"error\":\"Unauthorized\"}");
    return;
  }

  if (!mode) {
    sendJson(400, "{\"error\":\"Pump can only be controlled manually in manual mode\"}");
    return;
  }

  bool newState = relayStatus;
  if (server.hasArg("action")) {
    newState = server.arg("action") == "true";
  } else if (server.hasArg("plain")) {
    String body = server.arg("plain");
    newState = body.indexOf("true") != -1;
  }

  relayStatus = newState;
  digitalWrite(relayPin, relayStatus ? HIGH : LOW);

  sendJson(200, String("{\"status\":\"ok\",\"relayStatus\":") + (relayStatus ? "true" : "false") + "}");
}

// ===== Setup =====
void setup() {
  Serial.begin(115200);
  pinMode(relayPin, OUTPUT);
  digitalWrite(relayPin, LOW);

  EEPROM.begin(EEPROM_SIZE);

  wifiSSID = readEEPROMString(ADDR_SSID, 32);
  wifiPassword = readEEPROMString(ADDR_PASS, 32);

  threshold = EEPROM.read(ADDR_THRESHOLD) | (EEPROM.read(ADDR_THRESHOLD + 1) << 8);
  if (threshold == 0 || threshold > 1023) threshold = 700;

  mode = EEPROM.read(ADDR_MODE) == 1;

  WiFi.mode(WIFI_STA);
  if (wifiSSID.length() > 0) {
    WiFi.config(staticIP, gateway, subnet);
    WiFi.begin(wifiSSID.c_str(), wifiPassword.c_str());
    Serial.print("Connecting to Wi-Fi");
    unsigned long start = millis();
    while (WiFi.status() != WL_CONNECTED && millis() - start < 10000) {
      delay(500);
      Serial.print(".");
    }
  }

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("\nStarting AP for setup...");
    WiFi.mode(WIFI_AP);
    WiFi.softAP("IrrigationSetupAP", "12345678");
    Serial.print("AP IP: ");
    Serial.println(WiFi.softAPIP());
    server.on("/", HTTP_GET, handleWiFiRoot);
    server.on("/saveWifi", HTTP_POST, handleSaveWiFi);
  } else {
    Serial.println("\nConnected! IP: " + WiFi.localIP().toString());
    server.on("/sse", HTTP_GET, handleSSE);
    server.on("/setThreshold", HTTP_POST, handleSetThreshold);
    server.on("/reset", HTTP_POST, handleReset);
    server.on("/saveWifi", HTTP_POST, handleSaveWiFi);

    // ===== New Routes =====
    server.on("/setMode", HTTP_POST, handleSetMode);
    server.on("/setPump", HTTP_POST, handleSetPump);
  }

  server.begin();
}

// ===== Loop =====
void loop() {
  server.handleClient();

  static unsigned long lastUpdate = 0;
  static unsigned long lastSseHeartbeat = 0;

  if (millis() - lastUpdate >= 1000) {
    lastUpdate = millis();

    int soilMoisture = analogRead(soilMoisturePin);

    if (!mode) {
      // Auto mode
      relayStatus = (soilMoisture < threshold);
      digitalWrite(relayPin, relayStatus ? HIGH : LOW);
    }

    String json = "{";
    json += "\"soilMoisture\":" + String(soilMoisture) + ",";
    json += "\"relayStatus\":" + String(relayStatus ? "true" : "false") + ",";
    json += "\"threshold\":" + String(threshold) + ",";
    json += "\"mode\":" + String(mode ? "true" : "false");
    json += "}";

    Serial.println(json);

    if (sseClient && sseClient.connected()) {
      sseClient.print("data: ");
      sseClient.print(json);
      sseClient.print("\r\n\r\n");
      sseClient.flush();
    } else {
      if (sseClient) {
        sseClient.stop();
        sseClient = WiFiClient();
      }
    }
  }

  if (millis() - lastSseHeartbeat >= 15000) {
    lastSseHeartbeat = millis();
    if (sseClient && sseClient.connected()) {
      sseClient.print(": ping\r\n\r\n");
      sseClient.flush();
    }
  }
}
