import { CallResponse } from '@/types';

// Interface for parsed transcript messages
export interface TranscriptMessage {
  speaker: 'agent' | 'user';
  text: string;
}

/**
 * Extracts transcript from a completed call's retellCallData field
 * @param call - The call object with potential retellCallData
 * @returns transcript string or null if unavailable
 */
export function extractTranscriptFromCall(call: CallResponse): string | null {
  // Only extract from completed calls
  if (call.status !== 'COMPLETED') {
    return null;
  }

  // Check if retellCallData exists
  const retellData = call.retellCallData;
  
  if (!retellData) {
    console.log('No retellCallData found for call:', call.callId);
    return null;
  }
  
  console.log('Found retellCallData for call:', call.callId, 'Data length:', retellData.length);

  try {
    // Parse the JSON string
    const parsedData = typeof retellData === 'string' ? JSON.parse(retellData) : retellData;
    
    // Extract transcript field
    const transcript = parsedData.transcript;
    
    if (!transcript || typeof transcript !== 'string') {
      return null;
    }
    
    return transcript.trim();
  } catch (error) {
    // JSON parsing failed - return null for "unavailable" display
    return null;
  }
}

/**
 * Parses a transcript string into structured conversation messages
 * @param transcriptText - Raw transcript with Agent: and User: prefixes
 * @returns array of structured messages
 */
export function parseTranscript(transcriptText: string): TranscriptMessage[] {
  if (!transcriptText || typeof transcriptText !== 'string') {
    return [];
  }

  const messages: TranscriptMessage[] = [];
  
  // Split by newlines and process each line
  const lines = transcriptText.split('\n');
  
  for (const line of lines) {
    const trimmedLine = line.trim();
    
    if (!trimmedLine) {
      continue; // Skip empty lines
    }

    // Check for Agent: prefix
    if (trimmedLine.startsWith('Agent:')) {
      const text = trimmedLine.substring('Agent:'.length).trim();
      if (text) {
        messages.push({
          speaker: 'agent',
          text: text
        });
      }
    }
    // Check for User: prefix
    else if (trimmedLine.startsWith('User:')) {
      const text = trimmedLine.substring('User:'.length).trim();
      if (text) {
        messages.push({
          speaker: 'user',
          text: text
        });
      }
    }
    // If line doesn't have Agent: or User: prefix, append to previous message if it exists
    else if (messages.length > 0) {
      // Append to the last message (handles multi-line messages)
      messages[messages.length - 1].text += ' ' + trimmedLine;
    }
  }

  return messages;
}

/**
 * Checks if a call has an available transcript
 * @param call - The call object to check
 * @returns true if transcript is available
 */
export function hasAvailableTranscript(call: CallResponse): boolean {
  const transcript = extractTranscriptFromCall(call);
  return transcript !== null && transcript.length > 0;
}

/**
 * Gets the full parsed transcript for a call
 * @param call - The call object
 * @returns parsed transcript messages or empty array
 */
export function getCallTranscript(call: CallResponse): TranscriptMessage[] {
  const rawTranscript = extractTranscriptFromCall(call);
  
  if (!rawTranscript) {
    return [];
  }
  
  return parseTranscript(rawTranscript);
}