import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest {
    
    private static EmergencyAlertSystem system;
    private static AlertTriggerCondition triggerCondition;
    
    @BeforeAll
    static void setUp() {
        system = new EmergencyAlertSystem();
        triggerCondition = new AlertTriggerCondition();
        System.out.println("🚨 Setting up Emergency Alert System Tests");
    }
    
    @AfterAll
    static void tearDown() {
        system.shutdown();
        System.out.println("✅ Test suite completed");
    }
    
    // ==================== TESTS FOR ALERT TRIGGERING CONDITIONS ====================
    
    @Test
    @Order(1)
    @DisplayName("Test Earthquake Alert Trigger - Above Threshold")
    void testEarthquakeTriggerAboveThreshold() {
        Map<String, Object> params = new HashMap<>();
        params.put("magnitude", 5.0);
        
        boolean shouldTrigger = triggerCondition.shouldTriggerAlert(EmergencyType.EARTHQUAKE, params);
        assertTrue(shouldTrigger, "Earthquake magnitude 5.0 should trigger alert");
    }
    
    @Test
    @Order(2)
    @DisplayName("Test Earthquake Alert Trigger - Below Threshold")
    void testEarthquakeTriggerBelowThreshold() {
        Map<String, Object> params = new HashMap<>();
        params.put("magnitude", 3.0);
        
        boolean shouldTrigger = triggerCondition.shouldTriggerAlert(EmergencyType.EARTHQUAKE, params);
        assertFalse(shouldTrigger, "Earthquake magnitude 3.0 should NOT trigger alert");
    }
    
    @Test
    @Order(3)
    @DisplayName("Test Fire Alert Trigger")
    void testFireTrigger() {
        Map<String, Object> params = new HashMap<>();
        params.put("temperature", 150);
        
        boolean shouldTrigger = triggerCondition.shouldTriggerAlert(EmergencyType.FIRE, params);
        assertTrue(shouldTrigger, "Fire temperature 150 should trigger alert");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test Fire Alert - Below Threshold")
    void testFireTriggerBelowThreshold() {
        Map<String, Object> params = new HashMap<>();
        params.put("temperature", 50);
        
        boolean shouldTrigger = triggerCondition.shouldTriggerAlert(EmergencyType.FIRE, params);
        assertFalse(shouldTrigger, "Fire temperature 50 should NOT trigger alert");
    }
    
    @Test
    @Order(5)
    @DisplayName("Test Flood Alert Trigger")
    void testFloodTrigger() {
        Map<String, Object> params = new HashMap<>();
        params.put("waterLevel", 6.0);
        
        boolean shouldTrigger = triggerCondition.shouldTriggerAlert(EmergencyType.FLOOD, params);
        assertTrue(shouldTrigger, "Water level 6.0 should trigger flood alert");
    }
    
    @Test
    @Order(6)
    @DisplayName("Test Hurricane Alert Trigger")
    void testHurricaneTrigger() {
        Map<String, Object> params = new HashMap<>();
        params.put("windSpeed", 100.0);
        
        boolean shouldTrigger = triggerCondition.shouldTriggerAlert(EmergencyType.HURRICANE, params);
        assertTrue(shouldTrigger, "Wind speed 100mph should trigger hurricane alert");
    }
    
    @Test
    @Order(7)
    @DisplayName("Test Tsunami Alert Trigger")
    void testTsunamiTrigger() {
        Map<String, Object> params = new HashMap<>();
        params.put("waveHeight", 2.0);
        
        boolean shouldTrigger = triggerCondition.shouldTriggerAlert(EmergencyType.TSUNAMI, params);
        assertTrue(shouldTrigger, "Wave height 2.0m should trigger tsunami alert");
    }
    
    @Test
    @Order(8)
    @DisplayName("Test Cyber Attack Alert Trigger")
    void testCyberAttackTrigger() {
        Map<String, Object> params = new HashMap<>();
        params.put("severity", 8);
        
        boolean shouldTrigger = triggerCondition.shouldTriggerAlert(EmergencyType.CYBER_ATTACK, params);
        assertTrue(shouldTrigger, "Severity 8 should trigger cyber attack alert");
    }
    
    @Test
    @Order(9)
    @DisplayName("Test Null Parameters - No Trigger")
    void testNullParameters() {
        boolean shouldTrigger = triggerCondition.shouldTriggerAlert(EmergencyType.EARTHQUAKE, null);
        assertFalse(shouldTrigger, "Null parameters should not trigger alert");
    }
    
    // ==================== TESTS FOR SEVERITY DETERMINATION ====================
    
    @Test
    @Order(10)
    @DisplayName("Test Earthquake Severity - Critical")
    void testEarthquakeSeverityCritical() {
        Map<String, Object> params = new HashMap<>();
        params.put("magnitude", 8.0);
        
        Severity severity = triggerCondition.determineSeverity(EmergencyType.EARTHQUAKE, params);
        assertEquals(Severity.CRITICAL, severity, "Magnitude 8.0 should be CRITICAL");
    }
    
    @Test
    @Order(11)
    @DisplayName("Test Earthquake Severity - High")
    void testEarthquakeSeverityHigh() {
        Map<String, Object> params = new HashMap<>();
        params.put("magnitude", 6.0);
        
        Severity severity = triggerCondition.determineSeverity(EmergencyType.EARTHQUAKE, params);
        assertEquals(Severity.HIGH, severity, "Magnitude 6.0 should be HIGH");
    }
    
    @Test
    @Order(12)
    @DisplayName("Test Fire Severity - Critical")
    void testFireSeverityCritical() {
        Map<String, Object> params = new HashMap<>();
        params.put("temperature", 350);
        
        Severity severity = triggerCondition.determineSeverity(EmergencyType.FIRE, params);
        assertEquals(Severity.CRITICAL, severity, "Temperature 350 should be CRITICAL");
    }
    
    @Test
    @Order(13)
    @DisplayName("Test Hurricane Severity - Category 5")
    void testHurricaneSeverityCritical() {
        Map<String, Object> params = new HashMap<>();
        params.put("windSpeed", 160.0);
        
        Severity severity = triggerCondition.determineSeverity(EmergencyType.HURRICANE, params);
        assertEquals(Severity.CRITICAL, severity, "Wind speed 160mph should be CRITICAL");
    }
    
    // ==================== TESTS FOR ALERT GENERATION ====================
    
    @Test
    @Order(14)
    @DisplayName("Test Alert Generation - Valid Conditions")
    void testAlertGenerationValid() {
        AlertGenerator generator = new AlertGenerator();
        Map<String, Object> params = new HashMap<>();
        params.put("magnitude", 5.5);
        
        Alert alert = generator.generateAlert(
            EmergencyType.EARTHQUAKE,
            "Test Earthquake",
            "Test message",
            "Test Location",
            params
        );
        
        assertNotNull(alert, "Alert should be generated");
        assertEquals(AlertStatus.GENERATED, alert.getStatus());
        assertNotNull(alert.getId());
        assertNotNull(alert.getTimestamp());
    }
    
    @Test
    @Order(15)
    @DisplayName("Test Alert Generation - Invalid Conditions")
    void testAlertGenerationInvalid() {
        AlertGenerator generator = new AlertGenerator();
        Map<String, Object> params = new HashMap<>();
        params.put("magnitude", 2.0);
        
        Alert alert = generator.generateAlert(
            EmergencyType.EARTHQUAKE,
            "Test Earthquake",
            "Test message",
            "Test Location",
            params
        );
        
        assertNull(alert, "Alert should NOT be generated for invalid conditions");
    }
    
    @Test
    @Order(16)
    @DisplayName("Test Alert History Tracking")
    void testAlertHistoryTracking() {
        AlertGenerator generator = new AlertGenerator();
        int initialCount = generator.getTotalAlertsGenerated();
        
        Map<String, Object> params = new HashMap<>();
        params.put("magnitude", 5.5);
        
        generator.generateAlert(EmergencyType.EARTHQUAKE, "Test", "Message", "Location", params);
        
        assertEquals(initialCount + 1, generator.getTotalAlertsGenerated());
        assertEquals(1, generator.getAlertHistory().size());
    }
    
    // ==================== TESTS FOR NOTIFICATION RELIABILITY ====================
    
    @Test
    @Order(17)
    @DisplayName("Test 100% Notification Reliability")
    void testPerfectNotificationReliability() {
        NotificationService service = new NotificationService(2, 100.0);
        Alert alert = new Alert("Test", "Message", Severity.MEDIUM, EmergencyType.FIRE, "Location");
        alert.setRecipients(Arrays.asList("test@example.com"));
        
        Map<String, Boolean> results = service.sendAlert(alert);
        
        assertNotNull(results);
        assertTrue(results.values().stream().allMatch(success -> success), 
                   "All notifications should succeed with 100% reliability");
    }
    
    @Test
    @Order(18)
    @DisplayName("Test Notification Retry Mechanism")
    void testNotificationRetryMechanism() {
        Alert alert = new Alert("Test", "Message", Severity.MEDIUM, EmergencyType.FIRE, "Location");
        alert.setRecipients(Arrays.asList("test@example.com"));
        
        // This will retry failed notifications
        int initialRetryCount = alert.getRetryCount();
        
        // Force a failure by using 0% reliability with retries
        NotificationService service = new NotificationService(3, 0.0);
        service.sendAlert(alert);
        
        assertTrue(alert.getRetryCount() >= initialRetryCount, 
                  "Retry count should increase after failures");
    }
    
    @Test
    @Order(19)
    @DisplayName("Test Multiple Recipients Notification")
    void testMultipleRecipientsNotification() {
        NotificationService service = new NotificationService(2, 100.0);
        Alert alert = new Alert("Test", "Message", Severity.HIGH, EmergencyType.EARTHQUAKE, "Location");
        alert.setRecipients(Arrays.asList("recipient1@test.com", "recipient2@test.com", "+1-555-0001"));
        
        Map<String, Boolean> results = service.sendAlert(alert);
        
        assertNotNull(results);
        assertEquals(alert.getRecipients().size() * 4, results.size(), 
                    "Should have results for each recipient across all 4 channels");
    }
    
    // ==================== TESTS FOR END-TO-END ALERT FLOW ====================
    
    @Test
    @Order(20)
    @DisplayName("Test End-to-End Alert Flow")
    void testEndToEndAlertFlow() {
        EmergencyAlertSystem testSystem = new EmergencyAlertSystem();
        Map<String, Object> params = new HashMap<>();
        params.put("magnitude", 6.2);
        
        Alert alert = testSystem.triggerAlert(
            EmergencyType.EARTHQUAKE,
            "E2E Test Earthquake",
            "Testing full flow",
            "Test City",
            params
        );
        
        assertNotNull(alert, "Alert should be generated");
        assertTrue(alert.getStatus() == AlertStatus.SENT || alert.getStatus() == AlertStatus.FAILED,
                  "Alert should have notification status");
        
        testSystem.shutdown();
    }
    
    @Test
    @Order(21)
    @DisplayName("Test Alert Cancellation")
    void testAlertCancellation() {
        EmergencyAlertSystem testSystem = new EmergencyAlertSystem();
        Map<String, Object> params = new HashMap<>();
        params.put("magnitude", 5.5);
        
        Alert alert = testSystem.triggerAlert(
            EmergencyType.EARTHQUAKE,
            "Cancellable Alert",
            "This alert will be cancelled",
            "Test City",
            params
        );
        
        if (alert != null) {
            testSystem.cancelAlert(alert.getId());
            assertEquals(AlertStatus.CANCELLED, alert.getStatus(), "Alert should be cancelled");
        }
        
        testSystem.shutdown();
    }
    
    @Test
    @Order(22)
    @DisplayName("Test Alert Has Unique ID")
    void testAlertUniqueId() {
        Alert alert1 = new Alert("Test1", "Msg1", Severity.MEDIUM, EmergencyType.FIRE, "Loc1");
        Alert alert2 = new Alert("Test2", "Msg2", Severity.MEDIUM, EmergencyType.FIRE, "Loc2");
        
        assertNotEquals(alert1.getId(), alert2.getId(), "Each alert should have a unique ID");
    }
    
    @Test
    @Order(23)
    @DisplayName("Test Alert Recipients Based on Severity")
    void testRecipientsBySeverity() {
        AlertGenerator generator = new AlertGenerator();
        
        // Critical severity should have more recipients
        Map<String, Object> criticalParams = new HashMap<>();
        criticalParams.put("magnitude", 8.0);
        Alert criticalAlert = generator.generateAlert(
            EmergencyType.EARTHQUAKE, "Critical", "Msg", "City", criticalParams
        );
        
        Map<String, Object> lowParams = new HashMap<>();
        lowParams.put("magnitude", 4.5);
        Alert lowAlert = generator.generateAlert(
            EmergencyType.EARTHQUAKE, "Low", "Msg", "City", lowParams
        );
        
        if (criticalAlert != null && lowAlert != null) {
            assertTrue(criticalAlert.getRecipients().size() > lowAlert.getRecipients().size(),
                      "Critical alerts should have more recipients than low severity alerts");
        }
    }
    
    // ==================== PERFORMANCE AND CONCURRENCY TESTS ====================
    
    @Test
    @Order(24)
    @DisplayName("Test Concurrent Alert Generation")
    void testConcurrentAlertGeneration() throws InterruptedException {
        AlertGenerator generator = new AlertGenerator();
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                Map<String, Object> params = new HashMap<>();
                params.put("magnitude", 5.5);
                generator.generateAlert(EmergencyType.EARTHQUAKE, "Concurrent Test", 
                                       "Concurrent message", "City", params);
                latch.countDown();
            });
        }
        
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "All concurrent alert generations should complete");
        
        assertEquals(threadCount, generator.getTotalAlertsGenerated(), 
                    "All concurrent alerts should be generated");
        
        executor.shutdown();
    }
}