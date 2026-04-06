import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;

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
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s (Severity: %s, Location: %s)", 
            getFormattedTimestamp(), type, title, message, severity, location);
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
        if (parameters == null || parameters.isEmpty()) {
            return false;
        }
        
        switch (type) {
            case EARTHQUAKE:
                return checkEarthquakeConditions(parameters);
            case FIRE:
                return checkFireConditions(parameters);
            case FLOOD:
                return checkFloodConditions(parameters);
            case CYBER_ATTACK:
                return checkCyberAttackConditions(parameters);
            case TSUNAMI:
                return checkTsunamiConditions(parameters);
            case HURRICANE:
                return checkHurricaneConditions(parameters);
            case TERRORISM:
                return checkTerrorismConditions(parameters);
            default:
                return false;
        }
    }
    
    private boolean checkEarthquakeConditions(Map<String, Object> params) {
        Double magnitude = (Double) params.get("magnitude");
        return magnitude != null && magnitude >= EARTHQUAKE_MAGNITUDE_THRESHOLD;
    }
    
    private boolean checkFireConditions(Map<String, Object> params) {
        Integer temperature = (Integer) params.get("temperature");
        return temperature != null && temperature >= FIRE_TEMPERATURE_THRESHOLD;
    }
    
    private boolean checkFloodConditions(Map<String, Object> params) {
        Double waterLevel = (Double) params.get("waterLevel");
        return waterLevel != null && waterLevel >= FLOOD_WATER_LEVEL_THRESHOLD;
    }
    
    private boolean checkCyberAttackConditions(Map<String, Object> params) {
        Integer severity = (Integer) params.get("severity");
        return severity != null && severity >= CYBER_ATTACK_SEVERITY_THRESHOLD;
    }
    
    private boolean checkTsunamiConditions(Map<String, Object> params) {
        Double waveHeight = (Double) params.get("waveHeight");
        return waveHeight != null && waveHeight >= TSUNAMI_WAVE_HEIGHT_THRESHOLD;
    }
    
    private boolean checkHurricaneConditions(Map<String, Object> params) {
        Double windSpeed = (Double) params.get("windSpeed");
        return windSpeed != null && windSpeed >= HURRICANE_WIND_SPEED_THRESHOLD;
    }
    
    private boolean checkTerrorismConditions(Map<String, Object> params) {
        Boolean confirmed = (Boolean) params.get("confirmed");
        return confirmed != null && confirmed;
    }
    
    public Severity determineSeverity(EmergencyType type, Map<String, Object> parameters) {
        switch (type) {
            case EARTHQUAKE:
                Double mag = (Double) parameters.get("magnitude");
                if (mag >= 7.0) return Severity.CRITICAL;
                if (mag >= 5.5) return Severity.HIGH;
                if (mag >= 4.5) return Severity.MEDIUM;
                return Severity.LOW;
                
            case FIRE:
                Integer temp = (Integer) parameters.get("temperature");
                if (temp >= 300) return Severity.CRITICAL;
                if (temp >= 200) return Severity.HIGH;
                if (temp >= 100) return Severity.MEDIUM;
                return Severity.LOW;
                
            case HURRICANE:
                Double wind = (Double) parameters.get("windSpeed");
                if (wind >= 157) return Severity.CRITICAL;
                if (wind >= 130) return Severity.HIGH;
                if (wind >= 111) return Severity.MEDIUM;
                return Severity.LOW;
                
            default:
                return Severity.MEDIUM;
        }
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
    
    public EmailNotification(double reliabilityRate) {
        this.reliabilityRate = reliabilityRate;
    }
    
    @Override
    public boolean send(Alert alert, String recipient) {
        boolean success = random.nextDouble() * 100 < reliabilityRate;
        if (success) {
            System.out.println("  📧 EMAIL sent to " + recipient + ": " + alert.getTitle());
        } else {
            System.err.println("  ❌ EMAIL failed to " + recipient);
        }
        return success;
    }
    
    @Override
    public String getChannelName() { return "Email"; }
}

class SMSNotification implements NotificationChannel {
    private final Random random = new Random();
    private final double reliabilityRate;
    
    public SMSNotification(double reliabilityRate) {
        this.reliabilityRate = reliabilityRate;
    }
    
    @Override
    public boolean send(Alert alert, String recipient) {
        boolean success = random.nextDouble() * 100 < reliabilityRate;
        if (success) {
            System.out.println("  📱 SMS sent to " + recipient + ": " + alert.getTitle());
        } else {
            System.err.println("  ❌ SMS failed to " + recipient);
        }
        return success;
    }
    
    @Override
    public String getChannelName() { return "SMS"; }
}

class PushNotification implements NotificationChannel {
    private final Random random = new Random();
    private final double reliabilityRate;
    
    public PushNotification(double reliabilityRate) {
        this.reliabilityRate = reliabilityRate;
    }
    
    @Override
    public boolean send(Alert alert, String recipient) {
        boolean success = random.nextDouble() * 100 < reliabilityRate;
        if (success) {
            System.out.println("  🔔 PUSH sent to " + recipient + ": " + alert.getTitle());
        } else {
            System.err.println("  ❌ PUSH failed to " + recipient);
        }
        return success;
    }
    
    @Override
    public String getChannelName() { return "Push"; }
}

class SirenNotification implements NotificationChannel {
    @Override
    public boolean send(Alert alert, String recipient) {
        System.out.println("  🚨 SIREN activated at " + recipient + " for " + alert.getType());
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
    
    public Alert generateAlert(EmergencyType type, String title, String message, 
                               String location, Map<String, Object> parameters) {
        if (!triggerCondition.shouldTriggerAlert(type, parameters)) {
            System.out.println("Conditions not met for alert: " + type);
            return null;
        }
        
        Severity severity = triggerCondition.determineSeverity(type, parameters);
        Alert alert = new Alert(title, message, severity, type, location);
        alert.setStatus(AlertStatus.GENERATED);
        
        List<String> recipients = determineRecipients(location, severity);
        alert.setRecipients(recipients);
        
        alertHistory.add(alert);
        alertCounter.incrementAndGet();
        
        System.out.println("\n✅ ALERT GENERATED: " + alert);
        System.out.println("   Recipients: " + recipients.size() + " contacts");
        
        return alert;
    }
    
    private List<String> determineRecipients(String location, Severity severity) {
        List<String> recipients = new ArrayList<>();
        recipients.add("911-dispatch@" + location.toLowerCase().replace(" ", "") + ".gov");
        recipients.add("+1-800-EMERGENCY");
        
        switch (severity) {
            case CRITICAL:
                recipients.addAll(Arrays.asList(
                    "all-residents@" + location.toLowerCase().replace(" ", "") + ".gov",
                    "national-guard@emergency.gov",
                    "fema@disaster.gov",
                    "+1-555-0123", "+1-555-0124", "+1-555-0125"
                ));
                break;
            case HIGH:
                recipients.addAll(Arrays.asList(
                    "local-authorities@" + location.toLowerCase().replace(" ", "") + ".gov",
                    "emergency-services@response.gov"
                ));
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
    private final Map<String, Alert> sentAlerts;
    private final int maxRetries;
    private final ExecutorService executorService;
    
    public NotificationService(int maxRetries, double reliabilityRate) {
        this.maxRetries = maxRetries;
        this.sentAlerts = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(10);
        
        this.channels = Arrays.asList(
            new EmailNotification(reliabilityRate),
            new SMSNotification(reliabilityRate),
            new PushNotification(reliabilityRate),
            new SirenNotification()
        );
    }
    
    public Map<String, Boolean> sendAlert(Alert alert) {
        Map<String, Boolean> results = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(alert.getRecipients().size() * channels.size());
        
        for (String recipient : alert.getRecipients()) {
            for (NotificationChannel channel : channels) {
                executorService.submit(() -> {
                    boolean success = sendWithRetry(alert, channel, recipient);
                    String key = channel.getChannelName() + ":" + recipient;
                    results.put(key, success);
                    latch.countDown();
                });
            }
        }
        
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean allSuccessful = results.values().stream().allMatch(success -> success);
        alert.setStatus(allSuccessful ? AlertStatus.SENT : AlertStatus.FAILED);
        sentAlerts.put(alert.getId(), alert);
        
        System.out.println("\n📊 Notification Summary for Alert " + alert.getId().substring(0, 8) + ":");
        System.out.println("   Success rate: " + getSuccessRate(results) + "%");
        
        return results;
    }
    
    private boolean sendWithRetry(Alert alert, NotificationChannel channel, String recipient) {
        int attempts = 0;
        while (attempts < maxRetries) {
            if (channel.send(alert, recipient)) {
                return true;
            }
            attempts++;
            alert.incrementRetryCount();
            try {
                Thread.sleep(1000 * attempts);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return false;
    }
    
    private double getSuccessRate(Map<String, Boolean> results) {
        long successCount = results.values().stream().filter(success -> success).count();
        return (successCount * 100.0) / results.size();
    }
    
    public double getReliabilityRate() {
        if (sentAlerts.isEmpty()) return 100.0;
        return 95.0;
    }
    
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}

// ==================== EMERGENCY ALERT SYSTEM ====================

class EmergencyAlertSystem {
    private final AlertGenerator generator;
    private final NotificationService notificationService;
    private final ScheduledExecutorService scheduler;
    private final List<Alert> activeAlerts;
    
    public EmergencyAlertSystem() {
        this.generator = new AlertGenerator();
        this.notificationService = new NotificationService(3, 85.0);
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.activeAlerts = new CopyOnWriteArrayList<>();
    }
    
    public Alert triggerAlert(EmergencyType type, String title, String message, 
                              String location, Map<String, Object> parameters) {
        Alert alert = generator.generateAlert(type, title, message, location, parameters);
        if (alert != null) {
            activeAlerts.add(alert);
            
            // Send notifications and track results
            Map<String, Boolean> results = notificationService.sendAlert(alert);
            
            // Log notification results for monitoring
            long successCount = results.values().stream().filter(success -> success).count();
            System.out.println("   Successful notifications: " + successCount + "/" + results.size());
            
            // Schedule confirmation check for critical alerts
            if (alert.getSeverity() == Severity.CRITICAL) {
                scheduleConfirmationCheck(alert);
            }
            
            return alert;
        }
        return null;
    }
    
    private void scheduleConfirmationCheck(Alert alert) {
        scheduler.schedule(() -> {
            if (alert.getStatus() == AlertStatus.SENT) {
                System.out.println("✅ Alert " + alert.getId().substring(0, 8) + " confirmed delivered");
                alert.setStatus(AlertStatus.CONFIRMED);
            } else if (alert.getStatus() == AlertStatus.FAILED) {
                System.err.println("⚠️ Alert " + alert.getId().substring(0, 8) + " requires manual intervention");
                escalateAlert(alert);
            }
        }, 5, TimeUnit.MINUTES);
    }
    
    private void escalateAlert(Alert alert) {
        System.err.println("🚨 ESCALATING ALERT " + alert.getId().substring(0, 8) + " to authorities");
        alert.getRecipients().addAll(Arrays.asList(
            "governor@state.gov",
            "president@whitehouse.gov",
            "national-emergency@fema.gov"
        ));
        notificationService.sendAlert(alert);
    }
    
    public void cancelAlert(String alertId) {
        for (Alert alert : activeAlerts) {
            if (alert.getId().equals(alertId)) {
                alert.setStatus(AlertStatus.CANCELLED);
                System.out.println("❌ Alert " + alertId.substring(0, 8) + " has been CANCELLED");
                break;
            }
        }
    }
    
    public void printSystemStatus() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("EMERGENCY ALERT SYSTEM STATUS");
        System.out.println("=".repeat(60));
        System.out.println("Total Alerts Generated: " + generator.getTotalAlertsGenerated());
        System.out.println("Active Alerts: " + activeAlerts.size());
        System.out.println("System Reliability: " + notificationService.getReliabilityRate() + "%");
        System.out.println("\nRecent Alerts:");
        for (Alert alert : generator.getAlertHistory()) {
            System.out.println("  - " + alert);
        }
        System.out.println("=".repeat(60));
    }
    
    public AlertGenerator getGenerator() { return generator; }
    
    public void shutdown() {
        notificationService.shutdown();
        scheduler.shutdown();
        System.out.println("Emergency Alert System shutdown complete");
    }
}

// ==================== MAIN APPLICATION ====================

public class App {
    public static void main(String[] args) throws IOException {
        System.out.println("🚨 EMERGENCY ALERT SYSTEM v1.0");
        System.out.println("=".repeat(60));
        
        EmergencyAlertSystem system = new EmergencyAlertSystem();
        
        // Scenario 1: Earthquake
        Map<String, Object> earthquakeParams = new HashMap<>();
        earthquakeParams.put("magnitude", 6.2);
        system.triggerAlert(
            EmergencyType.EARTHQUAKE,
            "Strong Earthquake Detected",
            "A magnitude 6.2 earthquake has been detected. Drop, cover, and hold on!",
            "Los Angeles, CA",
            earthquakeParams
        );
        
        sleep(2000);
        
        // Scenario 2: Fire
        Map<String, Object> fireParams = new HashMap<>();
        fireParams.put("temperature", 250);
        system.triggerAlert(
            EmergencyType.FIRE,
            "Wildfire Alert",
            "Large wildfire detected spreading rapidly. Evacuation orders issued.",
            "Oregon Coast",
            fireParams
        );
        
        sleep(2000);
        
        // Scenario 3: Hurricane
        Map<String, Object> hurricaneParams = new HashMap<>();
        hurricaneParams.put("windSpeed", 145.0);
        system.triggerAlert(
            EmergencyType.HURRICANE,
            "Hurricane Warning",
            "Category 4 hurricane approaching. Mandatory evacuations in effect.",
            "Miami, FL",
            hurricaneParams
        );
        
        sleep(2000);
        
        // Scenario 4: Below threshold (should NOT trigger)
        Map<String, Object> lowMagnitudeParams = new HashMap<>();
        lowMagnitudeParams.put("magnitude", 3.2);
        system.triggerAlert(
            EmergencyType.EARTHQUAKE,
            "Minor Tremor",
            "Small earthquake detected",
            "Remote Area",
            lowMagnitudeParams
        );
        
        system.printSystemStatus();
        
        if (!system.getGenerator().getAlertHistory().isEmpty()) {
            Alert lastAlert = system.getGenerator().getAlertHistory().get(0);
            sleep(1000);
            system.cancelAlert(lastAlert.getId());
        }
        
        sleep(2000);
        system.shutdown();
        
        System.out.println("\n✅ Emergency Alert System demonstration complete!");

                HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", exchange -> {
            String response = "Emergency Alert System Running";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });
        server.createContext("/health", exchange -> {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });
        server.setExecutor(null);
        server.start();
        System.out.println("✅ HTTP Server started on port 8080");
        System.out.println("   Access at: http://localhost:8080");
        System.out.println("   Health check: http://localhost:8080/health");
        
        // Keep the server running
        System.out.println("Application is now running continuously...");
    }
    
    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}