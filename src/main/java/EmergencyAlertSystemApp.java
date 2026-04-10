import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// ==================== MODEL CLASSES ====================

enum AlertStatus {
    PENDING, GENERATED, SENT, FAILED, CONFIRMED, CANCELLED
}

enum EmergencyType {
    EARTHQUAKE, FIRE, FLOOD, CYBER_ATTACK, TSUNAMI, TERRORISM, HURRICANE
}

enum Severity {
    LOW, MEDIUM, HIGH, CRITICAL
}

class Alert {
    private String id;
    private String title;
    private String message;
    private Severity severity;
    private EmergencyType type;
    private String location;
    private LocalDateTime timestamp;
    private AlertStatus status;
    private int retryCount;
    private List<String> recipients;

    public Alert(String title, String message, Severity severity, EmergencyType type, String location) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.type = type;
        this.location = location;
        this.timestamp = LocalDateTime.now();
        this.status = AlertStatus.PENDING;
        this.retryCount = 0;
        this.recipients = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Severity getSeverity() { return severity; }
    public EmergencyType getType() { return type; }
    public String getLocation() { return location; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }
    public int getRetryCount() { return retryCount; }
    public void incrementRetryCount() { this.retryCount++; }
    public List<String> getRecipients() { return recipients; }
    public void setRecipients(List<String> recipients) { this.recipients = recipients; }
    
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public String toJSON() {
        return String.format("{" +
            "\"id\":\"%s\"," +
            "\"title\":\"%s\"," +
            "\"message\":\"%s\"," +
            "\"severity\":\"%s\"," +
            "\"type\":\"%s\"," +
            "\"location\":\"%s\"," +
            "\"formattedTimestamp\":\"%s\"," +
            "\"status\":\"%s\"," +
            "\"recipientsCount\":%d" +
            "}", 
            id, escapeJSON(title), escapeJSON(message), severity, type, 
            escapeJSON(location), getFormattedTimestamp(), status, recipients.size());
    }
    
    private String escapeJSON(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}

// ==================== ALERT TRIGGER CONDITIONS ====================

class AlertTriggerCondition {
    private static final double EARTHQUAKE_MAGNITUDE_THRESHOLD = 4.5;
    private static final int FIRE_TEMPERATURE_THRESHOLD = 100;
    private static final double FLOOD_WATER_LEVEL_THRESHOLD = 5.0;
    private static final int CYBER_ATTACK_SEVERITY_THRESHOLD = 7;
    private static final double HURRICANE_WIND_SPEED_THRESHOLD = 74;
    private static final double TSUNAMI_WAVE_HEIGHT_THRESHOLD = 1.0;
    
    public boolean shouldTriggerAlert(EmergencyType type, Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) return false;
        
        switch (type) {
            case EARTHQUAKE: return getDouble(parameters, "magnitude") >= EARTHQUAKE_MAGNITUDE_THRESHOLD;
            case FIRE: return getInteger(parameters, "temperature") >= FIRE_TEMPERATURE_THRESHOLD;
            case FLOOD: return getDouble(parameters, "waterLevel") >= FLOOD_WATER_LEVEL_THRESHOLD;
            case CYBER_ATTACK: return getInteger(parameters, "severity") >= CYBER_ATTACK_SEVERITY_THRESHOLD;
            case TSUNAMI: return getDouble(parameters, "waveHeight") >= TSUNAMI_WAVE_HEIGHT_THRESHOLD;
            case HURRICANE: return getDouble(parameters, "windSpeed") >= HURRICANE_WIND_SPEED_THRESHOLD;
            case TERRORISM: return Boolean.TRUE.equals(getBoolean(parameters, "confirmed"));
            default: return false;
        }
    }
    
    public Severity determineSeverity(EmergencyType type, Map<String, Object> parameters) {
        switch (type) {
            case EARTHQUAKE:
                Double mag = getDouble(parameters, "magnitude");
                if (mag == null) return Severity.MEDIUM;
                if (mag >= 7.0) return Severity.CRITICAL;
                if (mag >= 5.5) return Severity.HIGH;
                if (mag >= 4.5) return Severity.MEDIUM;
                return Severity.LOW;
            case FIRE:
                Integer temp = getInteger(parameters, "temperature");
                if (temp == null) return Severity.MEDIUM;
                if (temp >= 300) return Severity.CRITICAL;
                if (temp >= 200) return Severity.HIGH;
                if (temp >= 100) return Severity.MEDIUM;
                return Severity.LOW;
            case HURRICANE:
                Double wind = getDouble(parameters, "windSpeed");
                if (wind == null) return Severity.MEDIUM;
                if (wind >= 157) return Severity.CRITICAL;
                if (wind >= 130) return Severity.HIGH;
                if (wind >= 111) return Severity.MEDIUM;
                return Severity.LOW;
            default: return Severity.MEDIUM;
        }
    }
    
    private Double getDouble(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return (value instanceof Number) ? ((Number) value).doubleValue() : null;
    }
    
    private Integer getInteger(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return (value instanceof Number) ? ((Number) value).intValue() : null;
    }
    
    private Boolean getBoolean(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return (value instanceof Boolean) ? (Boolean) value : null;
    }
}

// ==================== NOTIFICATION CHANNELS ====================

interface NotificationChannel {
    boolean send(Alert alert, String recipient);
    String getChannelName();
}

class EmailNotification implements NotificationChannel {
    private final Random random = new Random();
    private final double reliabilityRate;
    public EmailNotification(double reliabilityRate) { this.reliabilityRate = reliabilityRate; }
    
    @Override
    public boolean send(Alert alert, String recipient) {
        boolean success = random.nextDouble() * 100 < reliabilityRate;
        System.out.println(success ? "  📧 EMAIL sent to " + recipient : "  ❌ EMAIL failed to " + recipient);
        return success;
    }
    
    @Override
    public String getChannelName() { return "Email"; }
}

class SMSNotification implements NotificationChannel {
    private final Random random = new Random();
    private final double reliabilityRate;
    public SMSNotification(double reliabilityRate) { this.reliabilityRate = reliabilityRate; }
    
    @Override
    public boolean send(Alert alert, String recipient) {
        boolean success = random.nextDouble() * 100 < reliabilityRate;
        System.out.println(success ? "  📱 SMS sent to " + recipient : "  ❌ SMS failed to " + recipient);
        return success;
    }
    
    @Override
    public String getChannelName() { return "SMS"; }
}

class PushNotification implements NotificationChannel {
    private final Random random = new Random();
    private final double reliabilityRate;
    public PushNotification(double reliabilityRate) { this.reliabilityRate = reliabilityRate; }
    
    @Override
    public boolean send(Alert alert, String recipient) {
        boolean success = random.nextDouble() * 100 < reliabilityRate;
        System.out.println(success ? "  🔔 PUSH sent to " + recipient : "  ❌ PUSH failed to " + recipient);
        return success;
    }
    
    @Override
    public String getChannelName() { return "Push"; }
}

class SirenNotification implements NotificationChannel {
    @Override
    public boolean send(Alert alert, String recipient) {
        System.out.println("  🚨 SIREN activated at " + recipient);
        return true;
    }
    @Override
    public String getChannelName() { return "Siren"; }
}

// ==================== ALERT GENERATOR ====================

class AlertGenerator {
    private final AlertTriggerCondition triggerCondition;
    private final List<Alert> alertHistory;
    private final AtomicInteger alertCounter;
    
    public AlertGenerator() {
        this.triggerCondition = new AlertTriggerCondition();
        this.alertHistory = new CopyOnWriteArrayList<>();
        this.alertCounter = new AtomicInteger(0);
    }
    
    public Alert generateAlert(EmergencyType type, String title, String message, String location, Map<String, Object> parameters) {
        if (!triggerCondition.shouldTriggerAlert(type, parameters)) {
            System.out.println("Conditions not met for alert: " + type);
            return null;
        }
        
        Severity severity = triggerCondition.determineSeverity(type, parameters);
        Alert alert = new Alert(title, message, severity, type, location);
        alert.setStatus(AlertStatus.GENERATED);
        alert.setRecipients(determineRecipients(location, severity));
        
        alertHistory.add(alert);
        alertCounter.incrementAndGet();
        
        System.out.println("\n✅ ALERT GENERATED: " + alert.getTitle() + " (Severity: " + severity + ")");
        return alert;
    }
    
    private List<String> determineRecipients(String location, Severity severity) {
        List<String> recipients = new ArrayList<>();
        String locationKey = location.toLowerCase().replaceAll("\\s+", "");
        recipients.add("dispatch@" + locationKey + ".gov");
        recipients.add("+1-800-EMERGENCY");
        
        switch (severity) {
            case CRITICAL:
                recipients.addAll(Arrays.asList("all-residents@" + locationKey + ".gov", "national-guard@emergency.gov"));
                break;
            case HIGH:
                recipients.addAll(Arrays.asList("local-authorities@" + locationKey + ".gov", "emergency-services@response.gov"));
                break;
            case MEDIUM:
                recipients.add("alert-subscribers@local.gov");
                break;
            default:
                recipients.add("info@alerts.gov");
        }
        return recipients;
    }
    
    public List<Alert> getAlertHistory() { return alertHistory; }
    public int getTotalAlertsGenerated() { return alertCounter.get(); }
}

// ==================== NOTIFICATION SERVICE ====================

class NotificationService {
    private final List<NotificationChannel> channels;
    private final int maxRetries;
    private final ExecutorService executorService;
    
    public NotificationService(int maxRetries, double reliabilityRate) {
        this.maxRetries = maxRetries;
        this.executorService = Executors.newFixedThreadPool(10);
        this.channels = Arrays.asList(
            new EmailNotification(reliabilityRate), 
            new SMSNotification(reliabilityRate), 
            new PushNotification(reliabilityRate), 
            new SirenNotification()
        );
    }
    
    public void sendAlert(Alert alert) {
        CountDownLatch latch = new CountDownLatch(alert.getRecipients().size() * channels.size());
        
        for (String recipient : alert.getRecipients()) {
            for (NotificationChannel channel : channels) {
                executorService.submit(() -> {
                    sendWithRetry(alert, channel, recipient);
                    latch.countDown();
                });
            }
        }
        
        try { latch.await(30, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        alert.setStatus(AlertStatus.SENT);
        System.out.println("📊 Notifications completed for alert: " + alert.getId().substring(0, 8));
    }
    
    private void sendWithRetry(Alert alert, NotificationChannel channel, String recipient) {
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            if (channel.send(alert, recipient)) return;
            alert.incrementRetryCount();
            try { Thread.sleep(1000L * (attempt + 1)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
    
    public void shutdown() { executorService.shutdown(); }
}

// ==================== EMERGENCY ALERT SYSTEM ====================

class EmergencyAlertSystem {
    private final AlertGenerator generator;
    private final NotificationService notificationService;
    private final List<Alert> activeAlerts;
    
    public EmergencyAlertSystem() {
        this.generator = new AlertGenerator();
        this.notificationService = new NotificationService(3, 85.0);
        this.activeAlerts = new CopyOnWriteArrayList<>();
    }
    
    public Alert triggerAlert(EmergencyType type, String title, String message, String location, Map<String, Object> parameters) {
        Alert alert = generator.generateAlert(type, title, message, location, parameters);
        if (alert != null) {
            activeAlerts.add(alert);
            notificationService.sendAlert(alert);
        }
        return alert;
    }
    
    public void cancelAlert(String alertId) {
        activeAlerts.stream().filter(a -> a.getId().equals(alertId)).findFirst().ifPresent(a -> a.setStatus(AlertStatus.CANCELLED));
    }
    
    public String getSystemStatusJSON() {
        return String.format("{" +
            "\"totalAlerts\":%d," +
            "\"activeAlerts\":%d," +
            "\"timestamp\":\"%s\"" +
            "}", 
            generator.getTotalAlertsGenerated(), 
            activeAlerts.size(),
            LocalDateTime.now().toString());
    }
    
    public String getAlertHistoryJSON() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < generator.getAlertHistory().size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(generator.getAlertHistory().get(i).toJSON());
        }
        sb.append("]");
        return sb.toString();
    }
    
    public void shutdown() { notificationService.shutdown(); }
}

// ==================== MAIN APPLICATION WITH WEB SERVER ====================

public class EmergencyAlertSystemApp {
    private static HttpServer server;
    private static EmergencyAlertSystem alertSystem;

    public static void main(String[] args) throws IOException {
        System.out.println("🚨 EMERGENCY ALERT SYSTEM v2.0");
        System.out.println("=".repeat(60));
        
        alertSystem = new EmergencyAlertSystem();
        
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        
        server.createContext("/", new RootHandler());
        server.createContext("/dashboard", new DashboardHandler());
        server.createContext("/api/alerts", new AlertsHandler());
        server.createContext("/api/alerts/trigger", new TriggerAlertHandler());
        server.createContext("/api/alerts/history", new AlertHistoryHandler());
        server.createContext("/api/system/status", new SystemStatusHandler());
        server.createContext("/api/health", new HealthHandler());
        
        server.start();
        
        System.out.println("✅ Server started on http://localhost:8080");
        System.out.println("📊 Dashboard: http://localhost:8080/dashboard");
        System.out.println("💚 Health Check: http://localhost:8080/api/health");
        System.out.println("\nPress Ctrl+C to stop\n");
        
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Server stopped");
            Thread.currentThread().interrupt();
        }
    }
    
    static class RootHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"name\":\"Emergency Alert System\",\"version\":\"2.0\",\"endpoints\":[\"/dashboard\",\"/api/alerts\",\"/api/health\"]}";
            sendJSONResponse(exchange, 200, response);
        }
    }
    
    static class HealthHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            sendJSONResponse(exchange, 200, "{\"status\":\"healthy\",\"timestamp\":\"" + LocalDateTime.now() + "\"}");
        }
    }
    
    static class SystemStatusHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            sendJSONResponse(exchange, 200, alertSystem.getSystemStatusJSON());
        }
    }
    
    static class AlertHistoryHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            sendJSONResponse(exchange, 200, alertSystem.getAlertHistoryJSON());
        }
    }
    
    static class AlertsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            sendJSONResponse(exchange, 200, alertSystem.getAlertHistoryJSON());
        }
    }
    
    static class TriggerAlertHandler implements HttpHandler {
        @SuppressWarnings("unchecked")
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJSONResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                Map<String, Object> request = parseJSON(body);
                EmergencyType type = EmergencyType.valueOf(((String) request.get("type")).toUpperCase());
                String title = (String) request.get("title");
                String message = (String) request.get("message");
                String location = (String) request.get("location");
                Map<String, Object> parameters = (Map<String, Object>) request.getOrDefault("parameters", new HashMap<>());
                
                Alert alert = alertSystem.triggerAlert(type, title, message, location, parameters);
                if (alert != null) {
                    sendJSONResponse(exchange, 201, alert.toJSON());
                } else {
                    sendJSONResponse(exchange, 400, "{\"error\":\"Conditions not met for alert\"}");
                }
            } catch (Exception e) {
                sendJSONResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
        
        private Map<String, Object> parseJSON(String json) {
            Map<String, Object> map = new HashMap<>();
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                        String value = keyValue[1].trim().replaceAll("^\"|\"$", "");
                        map.put(key, value);
                    }
                }
            }
            return map;
        }
    }
    
    static class DashboardHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String html = getDashboardHTML();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
    
    private static void sendJSONResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    private static String getDashboardHTML() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Emergency Alert System</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); min-height: 100vh; color: #fff; }
        .container { max-width: 1400px; margin: 0 auto; padding: 20px; }
        .header { text-align: center; padding: 30px 0; border-bottom: 2px solid #e94560; margin-bottom: 30px; }
        .header h1 { font-size: 2.5em; margin-bottom: 10px; }
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .stat-card { background: rgba(255,255,255,0.1); backdrop-filter: blur(10px); border-radius: 15px; padding: 20px; text-align: center; transition: transform 0.3s; }
        .stat-card:hover { transform: translateY(-5px); }
        .stat-value { font-size: 2.5em; font-weight: bold; color: #e94560; }
        .form-section, .alerts-section { background: rgba(255,255,255,0.1); backdrop-filter: blur(10px); border-radius: 15px; padding: 25px; margin-bottom: 30px; }
        .form-section h2, .alerts-section h2 { margin-bottom: 20px; color: #e94560; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; font-weight: 500; }
        input, select, textarea { width: 100%; padding: 10px; border: none; border-radius: 8px; background: rgba(255,255,255,0.2); color: #fff; font-size: 14px; }
        input:focus, select:focus, textarea:focus { outline: none; background: rgba(255,255,255,0.3); }
        textarea { min-height: 80px; resize: vertical; }
        .param-group { background: rgba(0,0,0,0.3); padding: 15px; border-radius: 10px; margin-bottom: 15px; }
        button { background: #e94560; color: white; border: none; padding: 12px 30px; border-radius: 8px; cursor: pointer; font-size: 16px; font-weight: bold; transition: all 0.3s; }
        button:hover { background: #c73e56; transform: scale(1.02); }
        .alert-item { background: rgba(255,255,255,0.05); border-radius: 10px; padding: 15px; margin-bottom: 10px; border-left: 4px solid; transition: all 0.3s; }
        .alert-item:hover { background: rgba(255,255,255,0.1); }
        .alert-critical { border-left-color: #ff0000; }
        .alert-high { border-left-color: #ff6600; }
        .alert-medium { border-left-color: #ffcc00; }
        .alert-low { border-left-color: #00cc66; }
        .alert-title { font-weight: bold; font-size: 1.1em; }
        .alert-details { font-size: 0.85em; opacity: 0.7; margin-top: 8px; }
        .status-badge { display: inline-block; padding: 4px 8px; border-radius: 4px; font-size: 0.75em; font-weight: bold; margin-left: 10px; }
        .status-PENDING { background: #ffcc00; color: #333; }
        .status-GENERATED { background: #3498db; color: white; }
        .status-SENT { background: #27ae60; color: white; }
        .status-FAILED { background: #e74c3c; color: white; }
        .toast { position: fixed; bottom: 20px; right: 20px; background: #27ae60; color: white; padding: 15px 25px; border-radius: 8px; display: none; z-index: 1000; animation: slideIn 0.3s ease; }
        .toast.error { background: #e74c3c; }
        @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
        .refresh-btn { float: right; background: #3498db; padding: 8px 15px; font-size: 14px; }
        .clearfix::after { content: ""; clear: both; display: table; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🚨 Emergency Alert System</h1>
            <div>Real-time Emergency Management Dashboard</div>
        </div>
        
        <div class="stats-grid">
            <div class="stat-card"><div class="stat-value" id="totalAlerts">0</div><div>Total Alerts</div></div>
            <div class="stat-card"><div class="stat-value" id="activeAlerts">0</div><div>Active Alerts</div></div>
            <div class="stat-card"><div class="stat-value" id="systemReliability">95%</div><div>System Reliability</div></div>
        </div>
        
        <div class="form-section">
            <h2>🚨 Trigger New Alert</h2>
            <form id="alertForm">
                <div class="form-group">
                    <label>Emergency Type</label>
                    <select id="emergencyType" required>
                        <option value="EARTHQUAKE">🌍 Earthquake</option>
                        <option value="FIRE">🔥 Fire</option>
                        <option value="FLOOD">🌊 Flood</option>
                        <option value="CYBER_ATTACK">💻 Cyber Attack</option>
                        <option value="TSUNAMI">🌊 Tsunami</option>
                        <option value="TERRORISM">⚠️ Terrorism</option>
                        <option value="HURRICANE">🌀 Hurricane</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Title</label>
                    <input type="text" id="title" placeholder="e.g., Strong Earthquake Detected" required>
                </div>
                <div class="form-group">
                    <label>Message</label>
                    <textarea id="message" placeholder="Detailed emergency message..." required></textarea>
                </div>
                <div class="form-group">
                    <label>Location</label>
                    <input type="text" id="location" placeholder="e.g., Los Angeles, CA" required>
                </div>
                <div id="paramContainer" class="param-group">
                    <h3>Emergency Parameters</h3>
                    <div id="dynamicParams"></div>
                </div>
                <button type="submit">🚨 Trigger Alert</button>
            </form>
        </div>
        
        <div class="alerts-section">
            <div class="clearfix"><h2 style="float: left;">📋 Recent Alerts</h2><button class="refresh-btn" onclick="loadAlerts()">🔄 Refresh</button></div>
            <div id="alertsList"><p style="text-align: center; opacity: 0.7;">Loading alerts...</p></div>
        </div>
    </div>
    <div id="toast" class="toast"></div>
    
    <script>
        const paramConfig = {
            EARTHQUAKE: [{ name: "magnitude", label: "Magnitude", type: "number", step: "0.1", placeholder: "e.g., 6.2" }],
            FIRE: [{ name: "temperature", label: "Temperature (°C)", type: "number", placeholder: "e.g., 250" }],
            FLOOD: [{ name: "waterLevel", label: "Water Level (m)", type: "number", step: "0.1", placeholder: "e.g., 5.5" }],
            CYBER_ATTACK: [{ name: "severity", label: "Severity (1-10)", type: "number", placeholder: "e.g., 8" }],
            TSUNAMI: [{ name: "waveHeight", label: "Wave Height (m)", type: "number", step: "0.1", placeholder: "e.g., 2.5" }],
            HURRICANE: [{ name: "windSpeed", label: "Wind Speed (mph)", type: "number", placeholder: "e.g., 145" }],
            TERRORISM: [{ name: "confirmed", label: "Confirmed Threat", type: "checkbox" }]
        };
        
        function updateDynamicParams() {
            const type = document.getElementById('emergencyType').value;
            const container = document.getElementById('dynamicParams');
            const params = paramConfig[type] || [];
            container.innerHTML = '';
            params.forEach(param => {
                const div = document.createElement('div');
                div.style.marginBottom = '10px';
                div.innerHTML = `<label>${param.label}</label><input type="${param.type}" id="param_${param.name}" name="${param.name}" step="${param.step || '1'}" placeholder="${param.placeholder || ''}" ${param.type === 'checkbox' ? '' : 'value=""'}>`;
                container.appendChild(div);
            });
        }
        
        function collectParameters() {
            const type = document.getElementById('emergencyType').value;
            const params = paramConfig[type] || [];
            const parameters = {};
            params.forEach(param => {
                const element = document.getElementById(`param_${param.name}`);
                if (element) {
                    if (param.type === 'checkbox') parameters[param.name] = element.checked;
                    else if (param.type === 'number') parameters[param.name] = parseFloat(element.value);
                    else parameters[param.name] = element.value;
                }
            });
            return parameters;
        }
        
        async function triggerAlert(event) {
            event.preventDefault();
            const alertData = {
                type: document.getElementById('emergencyType').value,
                title: document.getElementById('title').value,
                message: document.getElementById('message').value,
                location: document.getElementById('location').value,
                parameters: collectParameters()
            };
            
            try {
                const response = await fetch('/api/alerts/trigger', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(alertData)
                });
                if (response.ok) {
                    showToast('✅ Alert triggered successfully!', 'success');
                    document.getElementById('alertForm').reset();
                    updateDynamicParams();
                    loadAlerts();
                    loadStats();
                } else {
                    showToast('❌ Failed to trigger alert', 'error');
                }
            } catch (error) {
                showToast('❌ Error: ' + error.message, 'error');
            }
        }
        
        async function loadAlerts() {
            try {
                const response = await fetch('/api/alerts/history');
                const alerts = await response.json();
                const container = document.getElementById('alertsList');
                if (alerts.length === 0) {
                    container.innerHTML = '<p style="text-align: center; opacity: 0.7;">No alerts found</p>';
                    return;
                }
                container.innerHTML = alerts.slice().reverse().map(alert => `
                    <div class="alert-item alert-${alert.severity.toLowerCase()}">
                        <div class="alert-title">${escapeHtml(alert.title)}<span class="status-badge status-${alert.status}">${alert.status}</span></div>
                        <div>${escapeHtml(alert.message)}</div>
                        <div class="alert-details">Type: ${alert.type} | Severity: ${alert.severity} | Location: ${escapeHtml(alert.location)}<br>Time: ${alert.formattedTimestamp} | Recipients: ${alert.recipientsCount}</div>
                    </div>
                `).join('');
            } catch (error) { console.error('Failed to load alerts:', error); }
        }
        
        async function loadStats() {
            try {
                const response = await fetch('/api/system/status');
                const stats = await response.json();
                document.getElementById('totalAlerts').textContent = stats.totalAlerts || 0;
                document.getElementById('activeAlerts').textContent = stats.activeAlerts || 0;
            } catch (error) { console.error('Failed to load stats:', error); }
        }
        
        function showToast(message, type) {
            const toast = document.getElementById('toast');
            toast.textContent = message;
            toast.className = 'toast ' + (type === 'error' ? 'error' : '');
            toast.style.display = 'block';
            setTimeout(() => { toast.style.display = 'none'; }, 3000);
        }
        
        function escapeHtml(text) { const div = document.createElement('div'); div.textContent = text; return div.innerHTML; }
        
        document.getElementById('alertForm').addEventListener('submit', triggerAlert);
        document.getElementById('emergencyType').addEventListener('change', updateDynamicParams);
        updateDynamicParams();
        loadAlerts();
        loadStats();
        setInterval(() => { loadAlerts(); loadStats(); }, 5000);
    </script>
</body>
</html>
""";
    }
}