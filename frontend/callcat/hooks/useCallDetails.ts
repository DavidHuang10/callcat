import { useState, useEffect, useCallback } from 'react';
import { apiService } from '@/lib/api';
import { CallResponse } from '@/types';
import { getCallTranscript, TranscriptMessage } from '@/utils/transcript';

interface UseCallDetailsOptions {
  callId: string | null;
}

interface UseCallDetailsReturn {
  call: CallResponse | null;
  transcript: TranscriptMessage[];
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
}

export function useCallDetails({
  callId,
}: UseCallDetailsOptions): UseCallDetailsReturn {
  const [call, setCall] = useState<CallResponse | null>(null);
  const [transcript, setTranscript] = useState<TranscriptMessage[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchCallDetails = useCallback(async () => {
    if (!callId) return;

    try {
      setLoading(true);
      setError(null);
      const callData = await apiService.getCall(callId);
      setCall(callData);
      
      // Extract transcript using our utility
      const transcriptMessages = getCallTranscript(callData);
      setTranscript(transcriptMessages);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to fetch call details';
      setError(errorMessage);
      console.error('Error fetching call details:', err);
    } finally {
      setLoading(false);
    }
  }, [callId]);

  const refresh = useCallback(async () => {
    await fetchCallDetails();
  }, [fetchCallDetails]);

  // Fetch call details when callId changes
  useEffect(() => {
    if (callId) {
      fetchCallDetails();
    } else {
      setCall(null);
      setTranscript([]);
    }
  }, [callId, fetchCallDetails]);

  return {
    call,
    transcript,
    loading,
    error,
    refresh,
  };
}