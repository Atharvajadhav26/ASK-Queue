package com.smartqueue.service;

import com.smartqueue.entity.*;
import com.smartqueue.exception.*;
import com.smartqueue.repository.*;
import com.smartqueue.util.QueueIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for queue CRUD operations (create, update, open, close).
 * Queue business logic (token operations) lives in QueueEngine.
 */
@Service
public class QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueService.class);

    private final QueueRepository queueRepository;
    private final QueueSettingsRepository queueSettingsRepository;
    private final TokenRepository tokenRepository;
    private final TokenHistoryRepository tokenHistoryRepository;
    private final NotificationRepository notificationRepository;

    public QueueService(QueueRepository queueRepository,
                        QueueSettingsRepository queueSettingsRepository,
                        TokenRepository tokenRepository,
                        TokenHistoryRepository tokenHistoryRepository,
                        NotificationRepository notificationRepository) {
        this.queueRepository = queueRepository;
        this.queueSettingsRepository = queueSettingsRepository;
        this.tokenRepository = tokenRepository;
        this.tokenHistoryRepository = tokenHistoryRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public ServiceQueue createQueue(com.smartqueue.dto.request.CreateQueueRequest request) {
        Long adminId = com.smartqueue.security.SecurityUtils.getCurrentUserId();
        return createQueue(request.getName(), request.getServiceType(), request.getDescription(),
                           request.getPrefix(), request.getMaxCapacity(), request.getAvgTimePerPerson(),
                           request.getHoldTimeMinutes(), request.getWorkingHoursStart(), request.getWorkingHoursEnd(), adminId);
    }

    @Transactional
    public ServiceQueue updateQueue(Long id, com.smartqueue.dto.request.UpdateQueueRequest request) {
        return updateQueue(id, request.getName(), request.getServiceType(), request.getDescription(),
                           request.getMaxCapacity(), request.getAvgTimePerPerson(),
                           request.getHoldTimeMinutes(), request.getWorkingHoursStart(), request.getWorkingHoursEnd());
    }

    @Transactional
    public ServiceQueue createQueue(String name, String serviceType, String description, String prefix,
                                     Integer maxCapacity, Integer avgTimePerPerson, Integer holdTimeMinutes,
                                     String workingHoursStart, String workingHoursEnd, Long adminId) {

        // Generate unique queue ID
        String queueId;
        do {
            queueId = QueueIdGenerator.generate();
        } while (queueRepository.existsByQueueId(queueId));

        // Create queue
        ServiceQueue queue = new ServiceQueue();
        queue.setQueueId(queueId);
        queue.setName(name);
        queue.setServiceType(serviceType);
        queue.setDescription(description);
        queue.setPrefix(prefix != null && !prefix.isBlank() ? prefix.toUpperCase().trim() : derivePrefix(name));
        queue.setStatus(QueueStatus.CLOSED);
        queue.setCurrentTokenSeq(0);
        queue.setCreatedBy(adminId);
        queue = queueRepository.save(queue);

        // Create settings
        QueueSettings settings = new QueueSettings();
        settings.setQueue(queue);
        settings.setMaxCapacity(maxCapacity != null ? maxCapacity : 50);
        settings.setAvgTimePerPersonMinutes(avgTimePerPerson != null ? avgTimePerPerson : 5);
        settings.setHoldTimeMinutes(holdTimeMinutes != null ? holdTimeMinutes : 10);
        settings.setWorkingHoursStart(workingHoursStart != null ? workingHoursStart : "09:00");
        settings.setWorkingHoursEnd(workingHoursEnd != null ? workingHoursEnd : "17:00");
        settings.setHoldExpiryBehavior("EXPIRE");
        queueSettingsRepository.save(settings);

        log.info("Queue created: {} ({})", name, queueId);
        return queue;
    }

    @Transactional
    public ServiceQueue updateQueue(Long queueId, String name, String serviceType, String description,
                                     Integer maxCapacity, Integer avgTimePerPerson, Integer holdTimeMinutes,
                                     String workingHoursStart, String workingHoursEnd) {
        ServiceQueue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));

        if (name != null) queue.setName(name);
        if (serviceType != null) queue.setServiceType(serviceType);
        if (description != null) queue.setDescription(description);

        queue = queueRepository.save(queue);

        // Update settings
        QueueSettings settings = queueSettingsRepository.findByQueueId(queueId)
                .orElseThrow(() -> new BadRequestException("Queue settings not found"));

        if (maxCapacity != null) settings.setMaxCapacity(maxCapacity);
        if (avgTimePerPerson != null) settings.setAvgTimePerPersonMinutes(avgTimePerPerson);
        if (holdTimeMinutes != null) settings.setHoldTimeMinutes(holdTimeMinutes);
        if (workingHoursStart != null) settings.setWorkingHoursStart(workingHoursStart);
        if (workingHoursEnd != null) settings.setWorkingHoursEnd(workingHoursEnd);
        queueSettingsRepository.save(settings);

        return queue;
    }

    @Transactional
    public ServiceQueue openQueue(Long queueId) {
        ServiceQueue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));

        if (queue.getStatus() == QueueStatus.OPEN) {
            throw new BadRequestException("Queue is already open");
        }

        queue.setStatus(QueueStatus.OPEN);
        queue = queueRepository.save(queue);
        log.info("Queue opened: {}", queue.getQueueId());
        return queue;
    }

    @Transactional
    public ServiceQueue closeQueue(Long queueId) {
        ServiceQueue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));

        if (queue.getStatus() == QueueStatus.CLOSED) {
            throw new BadRequestException("Queue is already closed");
        }

        queue.setStatus(QueueStatus.CLOSED);
        queue = queueRepository.save(queue);
        log.info("Queue closed: {}", queue.getQueueId());
        return queue;
    }

    /**
     * Delete a queue and all associated data (tokens, history, notifications, settings).
     */
    @Transactional
    public void deleteQueue(Long queueId) {
        ServiceQueue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));

        // 1. Delete token history for all tokens in this queue
        List<com.smartqueue.entity.Token> tokens = tokenRepository.findByQueue(queue);
        if (!tokens.isEmpty()) {
            tokenHistoryRepository.deleteByTokenIn(tokens);
        }

        // 2. Delete notifications for this queue
        notificationRepository.deleteByQueueId(queue.getId());

        // 3. Delete all tokens in this queue
        tokenRepository.deleteAll(tokens);

        // 4. Delete queue settings
        queueSettingsRepository.findByQueueId(queue.getId())
                .ifPresent(queueSettingsRepository::delete);

        // 5. Delete the queue itself
        queueRepository.delete(queue);

        log.info("Queue deleted: {} ({})", queue.getName(), queue.getQueueId());
    }

    public ServiceQueue getQueueById(Long id) {
        return queueRepository.findById(id)
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));
    }

    public ServiceQueue getQueueByQueueId(String queueId) {
        Optional<ServiceQueue> found = queueRepository.findByQueueId(queueId);
        if (found.isPresent()) {
            return found.get();
        }
        try {
            Long numericId = Long.parseLong(queueId);
            return queueRepository.findById(numericId)
                    .orElseThrow(() -> new QueueNotFoundException("Queue not found with ID: " + queueId));
        } catch (NumberFormatException e) {
            throw new QueueNotFoundException("Queue not found with ID: " + queueId);
        }
    }

    public Optional<QueueSettings> getQueueSettings(Long queueId) {
        return queueSettingsRepository.findByQueueId(queueId);
    }

    public List<ServiceQueue> getAdminQueues(Long adminId) {
        return queueRepository.findByCreatedBy(adminId);
    }

    public List<ServiceQueue> getOpenQueues() {
        return queueRepository.findByStatus(QueueStatus.OPEN);
    }

    /**
     * Derive a prefix from the queue name.
     * "General OPD" -> "OPD"
     * "Blood Test Lab" -> "BTL"
     */
    private String derivePrefix(String name) {
        if (name == null || name.isBlank()) return "Q";

        String[] words = name.trim().split("\\s+");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(3, words[0].length())).toUpperCase();
        }

        StringBuilder prefix = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                prefix.append(word.charAt(0));
            }
        }
        return prefix.toString().toUpperCase().substring(0, Math.min(4, prefix.length()));
    }
}
