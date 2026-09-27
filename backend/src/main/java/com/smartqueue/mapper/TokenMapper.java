package com.smartqueue.mapper;

import com.smartqueue.dto.response.ActiveQueueResponse;
import com.smartqueue.dto.response.HistoryResponse;
import com.smartqueue.dto.response.TokenResponse;
import com.smartqueue.entity.Token;
import org.springframework.stereotype.Component;

@Component
public class TokenMapper {

    public TokenResponse toTokenResponse(Token token, int peopleAhead, int estimatedWait) {
        TokenResponse response = new TokenResponse();
        if (token != null) {
            response.setId(token.getId());
            response.setTokenNumber(token.getTokenNumber());
            if (token.getQueue() != null) {
                response.setQueueId(token.getQueue().getQueueId());
                response.setQueueName(token.getQueue().getName());
            }
            if (token.getUser() != null) {
                response.setUserName(token.getUser().getName());
            }
            response.setStatus(token.getStatus() != null ? token.getStatus().name() : null);
            response.setPriority(token.getPriority() != null ? token.getPriority().name() : null);
            response.setPriorityReason(token.getPriorityReason());
            response.setQueuePosition(token.getQueuePosition());
            response.setJoinTime(token.getJoinTime());
            response.setCalledTime(token.getCalledTime());
            response.setServingStartTime(token.getServingStartTime());
            response.setCompletedTime(token.getCompletedTime());
            response.setHoldStartTime(token.getHoldStartTime());
            response.setHoldExpiryTime(token.getHoldExpiryTime());
            response.setSkipCount(token.getSkipCount());
        }
        response.setPeopleAhead(peopleAhead);
        response.setEstimatedWaitMinutes(estimatedWait);
        return response;
    }

    public ActiveQueueResponse toActiveQueueResponse(Token token, String currentToken, int peopleAhead, int estimatedWait) {
        ActiveQueueResponse response = new ActiveQueueResponse();
        if (token != null) {
            response.setTokenId(token.getId());
            response.setTokenNumber(token.getTokenNumber());
            if (token.getQueue() != null) {
                response.setQueueId(token.getQueue().getQueueId());
                response.setQueueName(token.getQueue().getName());
                response.setQueueDescription(token.getQueue().getDescription());
            }
            response.setStatus(token.getStatus() != null ? token.getStatus().name() : null);
            response.setPriority(token.getPriority() != null ? token.getPriority().name() : null);
            response.setQueuePosition(token.getQueuePosition());
            response.setHoldStartTime(token.getHoldStartTime());
            response.setHoldExpiryTime(token.getHoldExpiryTime());
            response.setJoinTime(token.getJoinTime());
        }
        response.setCurrentToken(currentToken);
        response.setPeopleAhead(peopleAhead);
        response.setEstimatedWaitMinutes(estimatedWait);
        return response;
    }

    public HistoryResponse toHistoryResponse(Token token) {
        HistoryResponse response = new HistoryResponse();
        if (token != null) {
            response.setTokenNumber(token.getTokenNumber());
            response.setDate(token.getJoinTime());
            response.setJoinTime(token.getJoinTime());
            response.setCalledTime(token.getCalledTime());
            response.setCompletedTime(token.getCompletedTime());
            response.setStatus(token.getStatus() != null ? token.getStatus().name() : null);
        }
        return response;
    }
}
