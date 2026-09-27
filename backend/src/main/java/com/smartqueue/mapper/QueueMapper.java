package com.smartqueue.mapper;

import com.smartqueue.dto.response.QueueDetailResponse;
import com.smartqueue.dto.response.QueueResponse;
import com.smartqueue.entity.ServiceQueue;
import com.smartqueue.entity.QueueSettings;import org.springframework.stereotype.Component;

@Component
public class QueueMapper {

    public QueueResponse toQueueResponse(ServiceQueue serviceQueue, QueueSettings queueSettings, int peopleWaiting, String currentTokenNumber, int estimatedWait) {
        QueueResponse response = new QueueResponse();
        // Assuming getters from ServiceQueue and QueueSettings
        if (serviceQueue != null) {
            response.setId(serviceQueue.getId());
            response.setQueueId(serviceQueue.getQueueId());
            response.setName(serviceQueue.getName());
            response.setServiceType(serviceQueue.getServiceType());
            response.setDescription(serviceQueue.getDescription());
            response.setStatus(serviceQueue.getStatus() != null ? serviceQueue.getStatus().name() : null);
            // ... (fill remaining fields)
        }
        if (queueSettings != null) {
            response.setMaxCapacity(queueSettings.getMaxCapacity());
        }
        response.setPeopleWaiting(peopleWaiting);
        response.setCurrentTokenNumber(currentTokenNumber);
        response.setEstimatedWaitMinutes(estimatedWait);
        return response;
    }

    public QueueDetailResponse toQueueDetailResponse(ServiceQueue serviceQueue, QueueSettings queueSettings, int peopleWaiting, String currentTokenNumber, int estimatedWait, String frontendUrl) {
        QueueDetailResponse response = new QueueDetailResponse();
        if (serviceQueue != null) {
            response.setId(serviceQueue.getId());
            response.setQueueId(serviceQueue.getQueueId());
            response.setName(serviceQueue.getName());
            response.setServiceType(serviceQueue.getServiceType());
            response.setDescription(serviceQueue.getDescription());
            response.setStatus(serviceQueue.getStatus() != null ? serviceQueue.getStatus().name() : null);
        }
        if (queueSettings != null) {
            response.setMaxCapacity(queueSettings.getMaxCapacity());
            response.setAvgTimePerPerson(queueSettings.getAvgTimePerPersonMinutes());
            response.setHoldTimeMinutes(queueSettings.getHoldTimeMinutes());
            response.setWorkingHoursStart(queueSettings.getWorkingHoursStart());
            response.setWorkingHoursEnd(queueSettings.getWorkingHoursEnd());
            response.setHoldExpiryBehavior(queueSettings.getHoldExpiryBehavior());
        }
        response.setPeopleWaiting(peopleWaiting);
        response.setCurrentTokenNumber(currentTokenNumber);
        response.setEstimatedWaitMinutes(estimatedWait);
        // URLs can be generated based on frontendUrl and queueId
        return response;
    }
}
