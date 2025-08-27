package com.callcat.backend.service;

import com.callcat.backend.dto.CallListResponse;
import com.callcat.backend.dto.CallResponse;
import com.callcat.backend.dto.CallRequest;
import com.callcat.backend.dto.UpdateCallRequest;
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
    private final EventBridgeService eventBridgeService;

    @Autowired
    public CallService(CallRecordRepository callRecordRepository, UserRepository userRepository, EventBridgeService eventBridgeService) {
        this.callRecordRepository = callRecordRepository;
        this.userRepository = userRepository;
        this.eventBridgeService = eventBridgeService;
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
        
        // Default AI language to English if not provided
        if (callRecord.getAiLanguage() == null) {
            callRecord.setAiLanguage("en");
        }
        
        callRecord.setUserId(user.getId().toString());
        callRecord.setCallId(UUID.randomUUID().toString());
        callRecord.setStatus("SCHEDULED");
        callRecord.setCreatedAt(currentTime);
        callRecord.setUpdatedAt(currentTime);

        callRecordRepository.save(callRecord);
        
        if (callRecord.getScheduledFor() != null) {
            eventBridgeService.scheduleCall(callRecord.getCallId(), callRecord.getScheduledFor());
        }
        
        CallResponse response = new CallResponse();
        BeanUtils.copyProperties(callRecord, response);
        return response;
    }

    public CallListResponse getCalls(String userEmail, String status, Integer limit) {
        User user = userRepository.findByEmailAndIsActive(userEmail, true)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (status == null) {
            throw new IllegalArgumentException("Status parameter is required");
        }

        List<CallRecord> calls = callRecordRepository.findByUserIdAndStatus(user.getId().toString(), status, limit);

        List<CallResponse> callResponses = calls.stream()
                .map(callRecord -> {
                    CallResponse response = new CallResponse();
                    BeanUtils.copyProperties(callRecord, response);
                    return response;
                })
                .collect(Collectors.toList());

        return new CallListResponse(callResponses);
    }

    public CallResponse getCall(String callId) {
        CallRecord callRecord = findCallByCallId(callId);

        CallResponse response = new CallResponse();
        BeanUtils.copyProperties(callRecord, response);
        return response;
    }

    public CallResponse updateCall(String callId, UpdateCallRequest request) {
        CallRecord callRecord = findCallByCallId(callId);

        if (request.getPhoneNumber() != null) {
            PhoneNumberValidator.validatePhoneNumber(request.getPhoneNumber());
        }

        if (request.getScheduledFor() != null && request.getScheduledFor() <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }

        BeanUpdateUtils.copyNonNullProperties(request, callRecord);
        callRecord.setUpdatedAt(System.currentTimeMillis());

        callRecordRepository.save(callRecord);
        
        CallResponse response = new CallResponse();
        BeanUtils.copyProperties(callRecord, response);
        return response;
    }

    public void deleteCall(String callId) {
        CallRecord callRecord = findCallByCallId(callId);

        if (!"SCHEDULED".equals(callRecord.getStatus())) {
            throw new IllegalArgumentException("Only scheduled calls can be deleted");
        }

        callRecordRepository.delete(callRecord);
    }

    public void updateCallStatusWithRetellData(String callId, String status, Long completedAt, String retellCallId, Boolean dialSuccessful) {
        CallRecord callRecord = findCallByCallId(callId);
        
        callRecord.setStatus(status); // CallRecord.setStatus() handles validation
        callRecord.setProviderId(retellCallId); // Store Retell's ID for future reference
        
        if (completedAt != null) {
            callRecord.setCompletedAt(completedAt);
        }
        if (dialSuccessful != null) {
            callRecord.setDialSuccessful(dialSuccessful);
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
    
    public CallRecord findCallByCallId(String callId) {
        return callRecordRepository.findByCallId(callId)
                .orElseThrow(() -> new RuntimeException("Call not found with ID: " + callId));
    }
    
    public CallRecord findCallByProviderId(String providerId) {
        return callRecordRepository.findByProviderId(providerId)
                .orElseThrow(() -> new RuntimeException("Call not found with provider ID: " + providerId));
    }
    
    public void saveCallRecord(CallRecord callRecord) {
        callRecordRepository.save(callRecord);
    }
}