package com.callcat.backend.service;

import com.callcat.backend.dto.CallListResponse;
import com.callcat.backend.dto.CallResponse;
import com.callcat.backend.dto.CreateCallRequest;
import com.callcat.backend.dto.UpdateCallRequest;
import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.entity.User;
import com.callcat.backend.repository.CallRecordRepository;
import com.callcat.backend.repository.UserRepository;
import com.callcat.backend.util.PhoneNumberValidator;
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

    public CallResponse createCall(String userEmail, CreateCallRequest request) {
        User user = userRepository.findByEmailAndIsActive(userEmail, true)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PhoneNumberValidator.validatePhoneNumber(request.getPhoneNumber());

        if (request.getScheduledAt() != null && request.getScheduledAt() <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }

        CallRecord callRecord = new CallRecord();
        callRecord.setUserId(user.getId());
        callRecord.setCallId(UUID.randomUUID().toString());
        callRecord.setCalleeName(request.getCalleeName());
        callRecord.setPhoneNumber(request.getPhoneNumber());
        callRecord.setSubject(request.getSubject());
        callRecord.setPrompt(request.getPrompt());
        callRecord.setStatus("SCHEDULED");
        callRecord.setScheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : System.currentTimeMillis());
        callRecord.setAiLanguage(request.getAiLanguage() != null ? request.getAiLanguage() : "en");
        callRecord.setVoiceId(request.getVoiceId());
        callRecord.setCreatedAt(System.currentTimeMillis());
        callRecord.setUpdatedAt(System.currentTimeMillis());

        CallRecord savedCall = callRecordRepository.save(callRecord);
        return mapToCallResponse(savedCall);
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

    public CallResponse updateCall(String userEmail, String callId, UpdateCallRequest request) {
        User user = userRepository.findByEmailAndIsActive(userEmail, true)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CallRecord callRecord = callRecordRepository.findByUserIdAndCallId(user.getId(), callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        if (request.getPhoneNumber() != null) {
            PhoneNumberValidator.validatePhoneNumber(request.getPhoneNumber());
        }

        if (request.getStatus() != null) {
            validateStatusTransition(callRecord.getStatus(), request.getStatus());
        }

        if (request.getScheduledAt() != null && request.getScheduledAt() <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }

        updateCallFields(callRecord, request);
        callRecord.setUpdatedAt(System.currentTimeMillis());

        CallRecord updatedCall = callRecordRepository.save(callRecord);
        return mapToCallResponse(updatedCall);
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

    private void updateCallFields(CallRecord callRecord, UpdateCallRequest request) {
        if (request.getCalleeName() != null) {
            callRecord.setCalleeName(request.getCalleeName());
        }
        if (request.getPhoneNumber() != null) {
            callRecord.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getSubject() != null) {
            callRecord.setSubject(request.getSubject());
        }
        if (request.getPrompt() != null) {
            callRecord.setPrompt(request.getPrompt());
        }
        if (request.getScheduledAt() != null) {
            callRecord.setScheduledAt(request.getScheduledAt());
        }
        if (request.getStatus() != null) {
            callRecord.setStatus(request.getStatus());
        }
        if (request.getAiLanguage() != null) {
            callRecord.setAiLanguage(request.getAiLanguage());
        }
        if (request.getVoiceId() != null) {
            callRecord.setVoiceId(request.getVoiceId());
        }
    }

    private CallResponse mapToCallResponse(CallRecord callRecord) {
        CallResponse response = new CallResponse();
        response.setCallId(callRecord.getCallId());
        response.setCalleeName(callRecord.getCalleeName());
        response.setPhoneNumber(callRecord.getPhoneNumber());
        response.setCallerNumber(callRecord.getCallerNumber());
        response.setSubject(callRecord.getSubject());
        response.setPrompt(callRecord.getPrompt());
        response.setStatus(callRecord.getStatus());
        response.setScheduledAt(callRecord.getScheduledAt());
        response.setCallAt(callRecord.getCallAt());
        response.setProviderId(callRecord.getProviderId());
        response.setAiLanguage(callRecord.getAiLanguage());
        response.setVoiceId(callRecord.getVoiceId());
        response.setCreatedAt(callRecord.getCreatedAt());
        response.setUpdatedAt(callRecord.getUpdatedAt());
        response.setSummary(callRecord.getSummary());
        response.setDurationSec(callRecord.getDurationSec());
        response.setOutcome(callRecord.getOutcome());
        response.setTranscriptUrl(callRecord.getTranscriptUrl());
        response.setAudioRecordingUrl(callRecord.getAudioRecordingUrl());
        response.setCompletedAt(callRecord.getCompletedAt());
        return response;
    }
}