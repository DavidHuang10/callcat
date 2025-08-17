package com.callcat.backend.service;

import com.callcat.backend.dto.CallListResponse;
import com.callcat.backend.dto.CallResponse;
import com.callcat.backend.dto.CallRequest;
import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.entity.User;
import com.callcat.backend.repository.CallRecordRepository;
import com.callcat.backend.repository.UserRepository;
import com.callcat.backend.util.PhoneNumberValidator;
import com.callcat.backend.util.BeanUpdateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CallService {

    private final CallRecordRepository callRecordRepository;
    private final UserRepository userRepository;

    @Autowired
    public CallService(CallRecordRepository callRecordRepository, UserRepository userRepository) {
        this.callRecordRepository = callRecordRepository;
        this.userRepository = userRepository;
    }

    public CallResponse createCall(String userEmail, CallRequest request) {
        User user = userRepository.findByEmailAndIsActive(userEmail, true)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PhoneNumberValidator.validatePhoneNumber(request.getPhoneNumber());

        if (request.getScheduledFor() != null && request.getScheduledFor() <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }

        long currentTime = System.currentTimeMillis();
        
        CallRecord callRecord = new CallRecord();
        BeanUpdateUtils.copyNonNullProperties(request, callRecord);
        
        callRecord.setUserId(user.getId());
        callRecord.setCallId(UUID.randomUUID().toString());
        callRecord.setStatus("SCHEDULED");
        callRecord.setCreatedAt(currentTime);
        callRecord.setUpdatedAt(currentTime);

        callRecordRepository.save(callRecord);
        return mapToCallResponse(callRecord);
    }

    public CallListResponse getCalls(String userEmail, String status, Integer limit) {
        User user = userRepository.findByEmailAndIsActive(userEmail, true)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CallRecord> calls;
        if (status != null) {
            calls = callRecordRepository.findByUserIdAndStatus(user.getId(), status, limit);
        } else {
            List<CallRecord> upcoming = callRecordRepository.findUpcomingCallsByUserId(user.getId(), limit / 2);
            List<CallRecord> completed = callRecordRepository.findCompletedCallsByUserId(user.getId(), limit / 2);
            calls = upcoming;
            calls.addAll(completed);
        }

        List<CallResponse> callResponses = calls.stream()
                .map(this::mapToCallResponse)
                .collect(Collectors.toList());

        return new CallListResponse(callResponses, null);
    }

    public CallResponse getCall(String userEmail, String callId) {
        User user = userRepository.findByEmailAndIsActive(userEmail, true)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CallRecord callRecord = callRecordRepository.findByUserIdAndCallId(user.getId(), callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        return mapToCallResponse(callRecord);
    }

    public CallResponse updateCall(String userEmail, String callId, CallRequest request) {
        User user = userRepository.findByEmailAndIsActive(userEmail, true)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CallRecord callRecord = callRecordRepository.findByUserIdAndCallId(user.getId(), callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        if (request.getPhoneNumber() != null) {
            PhoneNumberValidator.validatePhoneNumber(request.getPhoneNumber());
        }


        if (request.getScheduledFor() != null && request.getScheduledFor() <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }

        BeanUpdateUtils.copyNonNullProperties(request, callRecord);
        callRecord.setUpdatedAt(System.currentTimeMillis());

        callRecordRepository.save(callRecord);
        return mapToCallResponse(callRecord);
    }

    public void deleteCall(String userEmail, String callId) {
        User user = userRepository.findByEmailAndIsActive(userEmail, true)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CallRecord callRecord = callRecordRepository.findByUserIdAndCallId(user.getId(), callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        if (!"SCHEDULED".equals(callRecord.getStatus())) {
            throw new IllegalArgumentException("Only scheduled calls can be deleted");
        }

        callRecordRepository.delete(callRecord);
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if ("SCHEDULED".equals(currentStatus) && !"IN_PROGRESS".equals(newStatus) && !"CANCELED".equals(newStatus)) {
            throw new IllegalArgumentException("SCHEDULED calls can only transition to IN_PROGRESS or CANCELED");
        }
        if ("IN_PROGRESS".equals(currentStatus) && !"COMPLETED".equals(newStatus) && !"FAILED".equals(newStatus)) {
            throw new IllegalArgumentException("IN_PROGRESS calls can only transition to COMPLETED or FAILED");
        }
        if ("COMPLETED".equals(currentStatus) || "FAILED".equals(currentStatus) || "CANCELED".equals(currentStatus)) {
            throw new IllegalArgumentException("Cannot change status of a finalized call");
        }
    }


    private CallResponse mapToCallResponse(CallRecord callRecord) {
        CallResponse response = new CallResponse();
        BeanUtils.copyProperties(callRecord, response);
        return response;
    }

    public void updateCallStatusWithRetellData(String callId, String status, Long callStartedAt, Long completedAt, String retellCallId) {
        // Extract userId from callId to make efficient lookup (we need to parse callId or get from user context)
        // For now, we'll need to find by scanning, but this is much more targeted
        CallRecord callRecord = findCallByCallId(callId);
        
        validateStatusTransition(callRecord.getStatus(), status);
        
        callRecord.setStatus(status);
        callRecord.setProviderId(retellCallId); // Store Retell's ID for future reference
        
        if (callStartedAt != null) {
            callRecord.setCallStartedAt(callStartedAt);
        }
        if (completedAt != null) {
            callRecord.setCompletedAt(completedAt);
        }
        callRecord.setUpdatedAt(System.currentTimeMillis());
        
        callRecordRepository.save(callRecord);
    }

    public void updateRetellCallData(String callId, JsonNode retellData) {
        CallRecord callRecord = findCallByCallId(callId);
        
        
        callRecord.setRetellCallData(retellData.toString());
        callRecord.setUpdatedAt(System.currentTimeMillis());
        
        callRecordRepository.save(callRecord);
    }
    
    private CallRecord findCallByCallId(String callId) {
        return callRecordRepository.findByCallId(callId)
                .orElseThrow(() -> new RuntimeException("Call not found with ID: " + callId));
    }

    public CallRecord findCallByProviderId(String providerId) {
        return callRecordRepository.findByProviderId(providerId)
                .orElseThrow(() -> new RuntimeException("Call not found with provider ID: " + providerId));
    }
}