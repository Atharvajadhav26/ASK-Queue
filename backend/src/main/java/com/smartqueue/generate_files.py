import os

base_path = r"c:\Users\ATHARVA\Downloads\ASK Queue\smart-queue-system\backend\src\main\java\com\smartqueue"
dirs = ["exception", "dto/request", "dto/response", "mapper"]

for d in dirs:
    os.makedirs(os.path.join(base_path, d.replace('/', '\\')), exist_ok=True)

files = {}

# 1-9. Exceptions
exceptions = [
    "QueueNotFoundException", "QueueClosedException", "QueueFullException",
    "TokenNotFoundException", "InvalidTokenStateException", "UnauthorizedException",
    "DuplicateResourceException", "ConcurrentQueueOperationException", "BadRequestException"
]

for exc in exceptions:
    files[f"exception/{exc}.java"] = f"""package com.smartqueue.exception;

public class {exc} extends RuntimeException {{
    public {exc}(String message) {{
        super(message);
    }}
}}
"""

files["exception/GlobalExceptionHandler.java"] = """package com.smartqueue.exception;

import com.smartqueue.dto.response.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(QueueNotFoundException.class)
    public ResponseEntity<ApiError> handleQueueNotFoundException(QueueNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Queue Not Found", ex.getMessage(), null);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ApiError> handleTokenNotFoundException(TokenNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Token Not Found", ex.getMessage(), null);
    }

    @ExceptionHandler(QueueClosedException.class)
    public ResponseEntity<ApiError> handleQueueClosedException(QueueClosedException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Queue Closed", ex.getMessage(), null);
    }

    @ExceptionHandler(QueueFullException.class)
    public ResponseEntity<ApiError> handleQueueFullException(QueueFullException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Queue Full", ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidTokenStateException.class)
    public ResponseEntity<ApiError> handleInvalidTokenStateException(InvalidTokenStateException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Invalid Token State", ex.getMessage(), null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicateResourceException(DuplicateResourceException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Duplicate Resource", ex.getMessage(), null);
    }

    @ExceptionHandler(ConcurrentQueueOperationException.class)
    public ResponseEntity<ApiError> handleConcurrentQueueOperationException(ConcurrentQueueOperationException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Concurrent Operation", ex.getMessage(), null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorizedException(UnauthorizedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Unauthorized", ex.getMessage(), null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(BadRequestException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid request body", errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", null);
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatus status, String error, String message, Map<String, String> details) {
        ApiError apiError = new ApiError(status.value(), error, message, LocalDateTime.now(), details);
        return new ResponseEntity<>(apiError, status);
    }
}
"""

files["dto/response/ApiError.java"] = """package com.smartqueue.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public class ApiError {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> details;

    public ApiError() {}

    public ApiError(int status, String error, String message, LocalDateTime timestamp, Map<String, String> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.details = details;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Map<String, String> getDetails() { return details; }
    public void setDetails(Map<String, String> details) { this.details = details; }
}
"""

files["dto/response/AuthResponse.java"] = """package com.smartqueue.dto.response;

public class AuthResponse {
    private String token;
    private String email;
    private String name;
    private String role;
    private Long userId;

    public AuthResponse() {}

    public AuthResponse(String token, String email, String name, String role, Long userId) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.role = role;
        this.userId = userId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
"""

files["dto/response/QueueResponse.java"] = """package com.smartqueue.dto.response;

import java.time.LocalDateTime;

public class QueueResponse {
    private Long id;
    private String queueId;
    private String name;
    private String serviceType;
    private String description;
    private String status;
    private String currentTokenNumber;
    private int peopleWaiting;
    private int maxCapacity;
    private int estimatedWaitMinutes;
    private LocalDateTime createdAt;

    public QueueResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrentTokenNumber() { return currentTokenNumber; }
    public void setCurrentTokenNumber(String currentTokenNumber) { this.currentTokenNumber = currentTokenNumber; }
    public int getPeopleWaiting() { return peopleWaiting; }
    public void setPeopleWaiting(int peopleWaiting) { this.peopleWaiting = peopleWaiting; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public int getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
"""

files["dto/response/QueueDetailResponse.java"] = """package com.smartqueue.dto.response;

public class QueueDetailResponse extends QueueResponse {
    private int avgTimePerPerson;
    private int holdTimeMinutes;
    private String workingHoursStart;
    private String workingHoursEnd;
    private String holdExpiryBehavior;
    private String qrCodeUrl;
    private String queueLink;

    public QueueDetailResponse() { super(); }

    public int getAvgTimePerPerson() { return avgTimePerPerson; }
    public void setAvgTimePerPerson(int avgTimePerPerson) { this.avgTimePerPerson = avgTimePerPerson; }
    public int getHoldTimeMinutes() { return holdTimeMinutes; }
    public void setHoldTimeMinutes(int holdTimeMinutes) { this.holdTimeMinutes = holdTimeMinutes; }
    public String getWorkingHoursStart() { return workingHoursStart; }
    public void setWorkingHoursStart(String workingHoursStart) { this.workingHoursStart = workingHoursStart; }
    public String getWorkingHoursEnd() { return workingHoursEnd; }
    public void setWorkingHoursEnd(String workingHoursEnd) { this.workingHoursEnd = workingHoursEnd; }
    public String getHoldExpiryBehavior() { return holdExpiryBehavior; }
    public void setHoldExpiryBehavior(String holdExpiryBehavior) { this.holdExpiryBehavior = holdExpiryBehavior; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
    public String getQueueLink() { return queueLink; }
    public void setQueueLink(String queueLink) { this.queueLink = queueLink; }
}
"""

files["dto/response/TokenResponse.java"] = """package com.smartqueue.dto.response;

import java.time.LocalDateTime;

public class TokenResponse {
    private Long id;
    private String tokenNumber;
    private String queueId;
    private String queueName;
    private String userName;
    private String status;
    private String priority;
    private String priorityReason;
    private int queuePosition;
    private int peopleAhead;
    private int estimatedWaitMinutes;
    private LocalDateTime joinTime;
    private LocalDateTime calledTime;
    private LocalDateTime servingStartTime;
    private LocalDateTime completedTime;
    private LocalDateTime holdStartTime;
    private LocalDateTime holdExpiryTime;
    private int skipCount;

    public TokenResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }
    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getPriorityReason() { return priorityReason; }
    public void setPriorityReason(String priorityReason) { this.priorityReason = priorityReason; }
    public int getQueuePosition() { return queuePosition; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }
    public int getPeopleAhead() { return peopleAhead; }
    public void setPeopleAhead(int peopleAhead) { this.peopleAhead = peopleAhead; }
    public int getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }
    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }
    public LocalDateTime getCalledTime() { return calledTime; }
    public void setCalledTime(LocalDateTime calledTime) { this.calledTime = calledTime; }
    public LocalDateTime getServingStartTime() { return servingStartTime; }
    public void setServingStartTime(LocalDateTime servingStartTime) { this.servingStartTime = servingStartTime; }
    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    public LocalDateTime getHoldStartTime() { return holdStartTime; }
    public void setHoldStartTime(LocalDateTime holdStartTime) { this.holdStartTime = holdStartTime; }
    public LocalDateTime getHoldExpiryTime() { return holdExpiryTime; }
    public void setHoldExpiryTime(LocalDateTime holdExpiryTime) { this.holdExpiryTime = holdExpiryTime; }
    public int getSkipCount() { return skipCount; }
    public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
}
"""

files["dto/response/ActiveQueueResponse.java"] = """package com.smartqueue.dto.response;

import java.time.LocalDateTime;

public class ActiveQueueResponse {
    private Long tokenId;
    private String tokenNumber;
    private String queueId;
    private String queueName;
    private String queueDescription;
    private String currentToken;
    private String status;
    private String priority;
    private int queuePosition;
    private int peopleAhead;
    private int estimatedWaitMinutes;
    private LocalDateTime holdStartTime;
    private LocalDateTime holdExpiryTime;
    private LocalDateTime joinTime;

    public ActiveQueueResponse() {}

    public Long getTokenId() { return tokenId; }
    public void setTokenId(Long tokenId) { this.tokenId = tokenId; }
    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }
    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public String getQueueDescription() { return queueDescription; }
    public void setQueueDescription(String queueDescription) { this.queueDescription = queueDescription; }
    public String getCurrentToken() { return currentToken; }
    public void setCurrentToken(String currentToken) { this.currentToken = currentToken; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public int getQueuePosition() { return queuePosition; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }
    public int getPeopleAhead() { return peopleAhead; }
    public void setPeopleAhead(int peopleAhead) { this.peopleAhead = peopleAhead; }
    public int getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }
    public LocalDateTime getHoldStartTime() { return holdStartTime; }
    public void setHoldStartTime(LocalDateTime holdStartTime) { this.holdStartTime = holdStartTime; }
    public LocalDateTime getHoldExpiryTime() { return holdExpiryTime; }
    public void setHoldExpiryTime(LocalDateTime holdExpiryTime) { this.holdExpiryTime = holdExpiryTime; }
    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }
}
"""

files["dto/response/QueueManagementResponse.java"] = """package com.smartqueue.dto.response;

import java.util.List;

public class QueueManagementResponse {
    private String queueId;
    private String queueName;
    private String status;
    private String currentServingToken;
    private int totalWaiting;
    private int totalServed;
    private int totalCancelled;
    private List<TokenResponse> tokens;

    public QueueManagementResponse() {}

    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrentServingToken() { return currentServingToken; }
    public void setCurrentServingToken(String currentServingToken) { this.currentServingToken = currentServingToken; }
    public int getTotalWaiting() { return totalWaiting; }
    public void setTotalWaiting(int totalWaiting) { this.totalWaiting = totalWaiting; }
    public int getTotalServed() { return totalServed; }
    public void setTotalServed(int totalServed) { this.totalServed = totalServed; }
    public int getTotalCancelled() { return totalCancelled; }
    public void setTotalCancelled(int totalCancelled) { this.totalCancelled = totalCancelled; }
    public List<TokenResponse> getTokens() { return tokens; }
    public void setTokens(List<TokenResponse> tokens) { this.tokens = tokens; }
}
"""

files["dto/response/AnalyticsResponse.java"] = """package com.smartqueue.dto.response;

import java.util.Map;

public class AnalyticsResponse {
    private int activeQueues;
    private int totalCustomersWaiting;
    private int totalCustomersServed;
    private double avgWaitTimeMinutes;
    private double avgServiceTimeMinutes;
    private int completedTokens;
    private int cancelledTokens;
    private int skippedTokens;
    private int heldTokens;
    private int expiredTokens;
    private int priorityCustomers;
    private String peakHour;
    private Map<String, Integer> customersServedPerHour;
    private Map<String, Integer> tokenStatusDistribution;
    private Map<String, Integer> queueVolumeByDay;
    private Map<String, Double> avgWaitTimeByHour;

    public AnalyticsResponse() {}

    public int getActiveQueues() { return activeQueues; }
    public void setActiveQueues(int activeQueues) { this.activeQueues = activeQueues; }
    public int getTotalCustomersWaiting() { return totalCustomersWaiting; }
    public void setTotalCustomersWaiting(int totalCustomersWaiting) { this.totalCustomersWaiting = totalCustomersWaiting; }
    public int getTotalCustomersServed() { return totalCustomersServed; }
    public void setTotalCustomersServed(int totalCustomersServed) { this.totalCustomersServed = totalCustomersServed; }
    public double getAvgWaitTimeMinutes() { return avgWaitTimeMinutes; }
    public void setAvgWaitTimeMinutes(double avgWaitTimeMinutes) { this.avgWaitTimeMinutes = avgWaitTimeMinutes; }
    public double getAvgServiceTimeMinutes() { return avgServiceTimeMinutes; }
    public void setAvgServiceTimeMinutes(double avgServiceTimeMinutes) { this.avgServiceTimeMinutes = avgServiceTimeMinutes; }
    public int getCompletedTokens() { return completedTokens; }
    public void setCompletedTokens(int completedTokens) { this.completedTokens = completedTokens; }
    public int getCancelledTokens() { return cancelledTokens; }
    public void setCancelledTokens(int cancelledTokens) { this.cancelledTokens = cancelledTokens; }
    public int getSkippedTokens() { return skippedTokens; }
    public void setSkippedTokens(int skippedTokens) { this.skippedTokens = skippedTokens; }
    public int getHeldTokens() { return heldTokens; }
    public void setHeldTokens(int heldTokens) { this.heldTokens = heldTokens; }
    public int getExpiredTokens() { return expiredTokens; }
    public void setExpiredTokens(int expiredTokens) { this.expiredTokens = expiredTokens; }
    public int getPriorityCustomers() { return priorityCustomers; }
    public void setPriorityCustomers(int priorityCustomers) { this.priorityCustomers = priorityCustomers; }
    public String getPeakHour() { return peakHour; }
    public void setPeakHour(String peakHour) { this.peakHour = peakHour; }
    public Map<String, Integer> getCustomersServedPerHour() { return customersServedPerHour; }
    public void setCustomersServedPerHour(Map<String, Integer> customersServedPerHour) { this.customersServedPerHour = customersServedPerHour; }
    public Map<String, Integer> getTokenStatusDistribution() { return tokenStatusDistribution; }
    public void setTokenStatusDistribution(Map<String, Integer> tokenStatusDistribution) { this.tokenStatusDistribution = tokenStatusDistribution; }
    public Map<String, Integer> getQueueVolumeByDay() { return queueVolumeByDay; }
    public void setQueueVolumeByDay(Map<String, Integer> queueVolumeByDay) { this.queueVolumeByDay = queueVolumeByDay; }
    public Map<String, Double> getAvgWaitTimeByHour() { return avgWaitTimeByHour; }
    public void setAvgWaitTimeByHour(Map<String, Double> avgWaitTimeByHour) { this.avgWaitTimeByHour = avgWaitTimeByHour; }
}
"""

files["dto/response/DashboardResponse.java"] = """package com.smartqueue.dto.response;

import java.util.List;

public class DashboardResponse {
    private int activeQueues;
    private int customersWaiting;
    private String currentToken;
    private String avgWaitTime;
    private List<QueueResponse> recentQueues;

    public DashboardResponse() {}

    public int getActiveQueues() { return activeQueues; }
    public void setActiveQueues(int activeQueues) { this.activeQueues = activeQueues; }
    public int getCustomersWaiting() { return customersWaiting; }
    public void setCustomersWaiting(int customersWaiting) { this.customersWaiting = customersWaiting; }
    public String getCurrentToken() { return currentToken; }
    public void setCurrentToken(String currentToken) { this.currentToken = currentToken; }
    public String getAvgWaitTime() { return avgWaitTime; }
    public void setAvgWaitTime(String avgWaitTime) { this.avgWaitTime = avgWaitTime; }
    public List<QueueResponse> getRecentQueues() { return recentQueues; }
    public void setRecentQueues(List<QueueResponse> recentQueues) { this.recentQueues = recentQueues; }
}
"""

files["dto/response/CustomerDashboardResponse.java"] = """package com.smartqueue.dto.response;

import java.util.List;

public class CustomerDashboardResponse {
    private boolean hasActiveQueue;
    private ActiveQueueResponse activeQueue;
    private int unreadNotifications;
    private List<HistoryResponse> recentHistory;

    public CustomerDashboardResponse() {}

    public boolean isHasActiveQueue() { return hasActiveQueue; }
    public void setHasActiveQueue(boolean hasActiveQueue) { this.hasActiveQueue = hasActiveQueue; }
    public ActiveQueueResponse getActiveQueue() { return activeQueue; }
    public void setActiveQueue(ActiveQueueResponse activeQueue) { this.activeQueue = activeQueue; }
    public int getUnreadNotifications() { return unreadNotifications; }
    public void setUnreadNotifications(int unreadNotifications) { this.unreadNotifications = unreadNotifications; }
    public List<HistoryResponse> getRecentHistory() { return recentHistory; }
    public void setRecentHistory(List<HistoryResponse> recentHistory) { this.recentHistory = recentHistory; }
}
"""

files["dto/response/HistoryResponse.java"] = """package com.smartqueue.dto.response;

import java.time.LocalDateTime;

public class HistoryResponse {
    private String tokenNumber;
    private String queueName;
    private String serviceType;
    private LocalDateTime date;
    private LocalDateTime joinTime;
    private LocalDateTime calledTime;
    private LocalDateTime completedTime;
    private Long waitingTimeMinutes;
    private Long serviceTimeMinutes;
    private String status;

    public HistoryResponse() {}

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }
    public LocalDateTime getCalledTime() { return calledTime; }
    public void setCalledTime(LocalDateTime calledTime) { this.calledTime = calledTime; }
    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    public Long getWaitingTimeMinutes() { return waitingTimeMinutes; }
    public void setWaitingTimeMinutes(Long waitingTimeMinutes) { this.waitingTimeMinutes = waitingTimeMinutes; }
    public Long getServiceTimeMinutes() { return serviceTimeMinutes; }
    public void setServiceTimeMinutes(Long serviceTimeMinutes) { this.serviceTimeMinutes = serviceTimeMinutes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
"""

files["dto/response/NotificationResponse.java"] = """package com.smartqueue.dto.response;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private String tokenNumber;
    private String queueName;
    private LocalDateTime createdAt;

    public NotificationResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
"""

files["dto/response/ProfileResponse.java"] = """package com.smartqueue.dto.response;

public class ProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String preferredLanguage;
    private boolean notificationEmail;
    private boolean notificationSms;
    private boolean notificationPush;

    public ProfileResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public boolean isNotificationEmail() { return notificationEmail; }
    public void setNotificationEmail(boolean notificationEmail) { this.notificationEmail = notificationEmail; }
    public boolean isNotificationSms() { return notificationSms; }
    public void setNotificationSms(boolean notificationSms) { this.notificationSms = notificationSms; }
    public boolean isNotificationPush() { return notificationPush; }
    public void setNotificationPush(boolean notificationPush) { this.notificationPush = notificationPush; }
}
"""

files["dto/request/RegisterRequest.java"] = """package com.smartqueue.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String mobile;

    @NotBlank
    @Size(min = 6)
    private String password;

    public RegisterRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
"""

files["dto/request/LoginRequest.java"] = """package com.smartqueue.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    public LoginRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
"""

files["dto/request/CreateQueueRequest.java"] = """package com.smartqueue.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateQueueRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String serviceType;
    private String description;
    private String prefix;
    private Integer maxCapacity;
    private Integer avgTimePerPerson;
    private Integer holdTimeMinutes;
    private String workingHoursStart;
    private String workingHoursEnd;

    public CreateQueueRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public Integer getAvgTimePerPerson() { return avgTimePerPerson; }
    public void setAvgTimePerPerson(Integer avgTimePerPerson) { this.avgTimePerPerson = avgTimePerPerson; }
    public Integer getHoldTimeMinutes() { return holdTimeMinutes; }
    public void setHoldTimeMinutes(Integer holdTimeMinutes) { this.holdTimeMinutes = holdTimeMinutes; }
    public String getWorkingHoursStart() { return workingHoursStart; }
    public void setWorkingHoursStart(String workingHoursStart) { this.workingHoursStart = workingHoursStart; }
    public String getWorkingHoursEnd() { return workingHoursEnd; }
    public void setWorkingHoursEnd(String workingHoursEnd) { this.workingHoursEnd = workingHoursEnd; }
}
"""

files["dto/request/UpdateQueueRequest.java"] = """package com.smartqueue.dto.request;

public class UpdateQueueRequest {
    private String name;
    private String serviceType;
    private String description;
    private Integer maxCapacity;
    private Integer avgTimePerPerson;
    private Integer holdTimeMinutes;
    private String workingHoursStart;
    private String workingHoursEnd;

    public UpdateQueueRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public Integer getAvgTimePerPerson() { return avgTimePerPerson; }
    public void setAvgTimePerPerson(Integer avgTimePerPerson) { this.avgTimePerPerson = avgTimePerPerson; }
    public Integer getHoldTimeMinutes() { return holdTimeMinutes; }
    public void setHoldTimeMinutes(Integer holdTimeMinutes) { this.holdTimeMinutes = holdTimeMinutes; }
    public String getWorkingHoursStart() { return workingHoursStart; }
    public void setWorkingHoursStart(String workingHoursStart) { this.workingHoursStart = workingHoursStart; }
    public String getWorkingHoursEnd() { return workingHoursEnd; }
    public void setWorkingHoursEnd(String workingHoursEnd) { this.workingHoursEnd = workingHoursEnd; }
}
"""

files["dto/request/SkipRequest.java"] = """package com.smartqueue.dto.request;

import jakarta.validation.constraints.NotNull;

public class SkipRequest {
    @NotNull
    private Integer positions;

    public SkipRequest() {}

    public Integer getPositions() { return positions; }
    public void setPositions(Integer positions) { this.positions = positions; }
}
"""

files["dto/request/PriorityRequest.java"] = """package com.smartqueue.dto.request;

import jakarta.validation.constraints.NotNull;

public class PriorityRequest {
    @NotNull
    private String priority;
    private String reason;

    public PriorityRequest() {}

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
"""

files["dto/request/CancelRequest.java"] = """package com.smartqueue.dto.request;

public class CancelRequest {
    private String reason;

    public CancelRequest() {}

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
"""

files["dto/request/ProfileUpdateRequest.java"] = """package com.smartqueue.dto.request;

public class ProfileUpdateRequest {
    private String name;
    private String mobile;
    private String preferredLanguage;
    private Boolean notificationEmail;
    private Boolean notificationSms;
    private Boolean notificationPush;
    private String currentPassword;
    private String newPassword;

    public ProfileUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public Boolean getNotificationEmail() { return notificationEmail; }
    public void setNotificationEmail(Boolean notificationEmail) { this.notificationEmail = notificationEmail; }
    public Boolean getNotificationSms() { return notificationSms; }
    public void setNotificationSms(Boolean notificationSms) { this.notificationSms = notificationSms; }
    public Boolean getNotificationPush() { return notificationPush; }
    public void setNotificationPush(Boolean notificationPush) { this.notificationPush = notificationPush; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
"""

files["mapper/QueueMapper.java"] = """package com.smartqueue.mapper;

import com.smartqueue.dto.response.QueueDetailResponse;
import com.smartqueue.dto.response.QueueResponse;

public class QueueMapper {

    public static QueueResponse toQueueResponse(Object serviceQueue, Object queueSettings, int peopleWaiting, String currentTokenNumber, int estimatedWait) {
        // Dummy implementation since actual entity models are not provided in this context
        QueueResponse response = new QueueResponse();
        // Set fields from serviceQueue...
        return response;
    }

    public static QueueDetailResponse toQueueDetailResponse(Object serviceQueue, Object queueSettings, int peopleWaiting, String currentTokenNumber, int estimatedWait, String frontendUrl) {
        // Dummy implementation since actual entity models are not provided in this context
        QueueDetailResponse response = new QueueDetailResponse();
        // Set fields...
        return response;
    }
}
"""

files["mapper/TokenMapper.java"] = """package com.smartqueue.mapper;

import com.smartqueue.dto.response.ActiveQueueResponse;
import com.smartqueue.dto.response.HistoryResponse;
import com.smartqueue.dto.response.TokenResponse;

public class TokenMapper {

    public static TokenResponse toTokenResponse(Object token, int peopleAhead, int estimatedWait) {
        TokenResponse response = new TokenResponse();
        return response;
    }

    public static ActiveQueueResponse toActiveQueueResponse(Object token, String currentToken, int peopleAhead, int estimatedWait) {
        ActiveQueueResponse response = new ActiveQueueResponse();
        return response;
    }

    public static HistoryResponse toHistoryResponse(Object token) {
        HistoryResponse response = new HistoryResponse();
        return response;
    }
}
"""

files["mapper/NotificationMapper.java"] = """package com.smartqueue.mapper;

import com.smartqueue.dto.response.NotificationResponse;

public class NotificationMapper {
    public static NotificationResponse toNotificationResponse(Object notification) {
        NotificationResponse response = new NotificationResponse();
        return response;
    }
}
"""

files["mapper/UserMapper.java"] = """package com.smartqueue.mapper;

import com.smartqueue.dto.response.ProfileResponse;

public class UserMapper {
    public static ProfileResponse toProfileResponse(Object user) {
        ProfileResponse response = new ProfileResponse();
        return response;
    }
}
"""

for file_path, content in files.items():
    full_path = os.path.join(base_path, file_path.replace('/', '\\'))
    with open(full_path, "w", encoding="utf-8") as f:
        f.write(content)

print("Generation complete")
