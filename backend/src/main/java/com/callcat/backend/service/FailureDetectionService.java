package com.callcat.backend.service;

import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.repository.CallRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FailureDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(FailureDetectionService.class);

    private final CallRecordRepository callRecordRepository;

    @Value("${callcat.failure.timeout.minutes:15}")
    private int failureTimeoutMinutes;

    public FailureDetectionService(CallRecordRepository callRecordRepository) {
        this.callRecordRepository = callRecordRepository;
    }

    /**
     * Runs every 5 minutes to check for failed calls.
     * A call is considered failed if:
     * 1. Status is SCHEDULED
     * 2. scheduledFor time + timeout period has passed
     *
     * Failed calls are marked as COMPLETED with dialSuccessful = false
     */
    @Scheduled(fixedRateString = "${callcat.failure.check.interval:300000}") // 5 minutes default
    public void detectFailedCalls() {
        try {
            long currentTime = System.currentTimeMillis();
            long timeoutMillis = failureTimeoutMinutes * 60 * 1000L;

            logger.debug("Starting failure detection check at {}", currentTime);

            // Get all scheduled calls that are overdue
            List<CallRecord> scheduledCalls = callRecordRepository.findAllByStatus("SCHEDULED");

            int failedCount = 0;
            for (CallRecord call : scheduledCalls) {
                if (call.getScheduledFor() != null) {
                    long overdueTime = currentTime - call.getScheduledFor();

                    if (overdueTime > timeoutMillis) {
                        markCallAsFailed(call, overdueTime);
                        failedCount++;
                    }
                }
            }

            if (failedCount > 0) {
                logger.info("Marked {} overdue calls as failed (COMPLETED with dialSuccessful=false)", failedCount);
            } else {
                logger.debug("No overdue calls found");
            }

        } catch (Exception e) {
            logger.error("Error during failure detection: {}", e.getMessage(), e);
        }
    }

    /**
     * Mark a specific call as failed due to timeout.
     * Sets status to COMPLETED with dialSuccessful = false to indicate failure.
     */
    private void markCallAsFailed(CallRecord call, long overdueTime) {
        try {
            call.setStatus("COMPLETED");
            call.setCompletedAt(System.currentTimeMillis());
            call.setUpdatedAt(System.currentTimeMillis());
            call.setDialSuccessful(false);

            callRecordRepository.save(call);

            logger.warn("Marked call {} as failed (COMPLETED with dialSuccessful=false) - overdue by {} minutes (scheduled: {}, timeout: {} min)",
                    call.getCallId(),
                    overdueTime / (60 * 1000L),
                    call.getScheduledFor(),
                    failureTimeoutMinutes);

        } catch (Exception e) {
            logger.error("Failed to mark call {} as failed: {}", call.getCallId(), e.getMessage());
        }
    }

    /**
     * Manual method to trigger failure detection (useful for testing)
     */
    public int detectAndMarkFailedCalls() {
        List<CallRecord> initialScheduledCalls = callRecordRepository.findAllByStatus("SCHEDULED");
        int initialCount = initialScheduledCalls.size();

        detectFailedCalls();

        // Return count of newly failed calls
        List<CallRecord> remainingScheduledCalls = callRecordRepository.findAllByStatus("SCHEDULED");
        return initialCount - remainingScheduledCalls.size();
    }
}