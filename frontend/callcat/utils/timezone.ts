/**
 * Timezone utilities for call scheduling
 */

/**
 * Get the user's current timezone
 * @returns The user's timezone identifier (e.g., 'America/New_York')
 */
export const getUserTimezone = (): string => {
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone;
  } catch (error) {
    console.error('Failed to detect user timezone:', error);
    return 'UTC'; // Fallback to UTC
  }
};

/**
 * Format a date and time for display in the user's timezone
 * @param date - The date to format
 * @param timezone - Optional timezone, defaults to user's timezone
 * @returns Formatted date/time string
 */
export const formatDateTimeForTimezone = (
  date: Date, 
  timezone?: string
): string => {
  const tz = timezone || getUserTimezone();
  
  return date.toLocaleDateString('en-US', {
    timeZone: tz,
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    timeZoneName: 'short'
  });
};

/**
 * Convert local date/time input values to UTC Unix timestamp
 * @param dateValue - Date string in YYYY-MM-DD format
 * @param timeValue - Time string in HH:MM format  
 * @param timezone - Optional timezone, defaults to user's timezone
 * @returns Unix timestamp in milliseconds
 */
export const convertLocalToUTC = (
  dateValue: string,
  timeValue: string,
  timezone?: string
): number => {
  const tz = timezone || getUserTimezone();
  
  // Create datetime string in user's timezone
  const dateTimeString = `${dateValue}T${timeValue}:00`;
  
  // Parse the date assuming it's in the user's timezone
  const date = new Date(dateTimeString);
  
  // Get the timezone offset for the user's timezone at this specific date
  const userDate = new Date(date.toLocaleString('en-US', { timeZone: tz }));
  const utcDate = new Date(date.toLocaleString('en-US', { timeZone: 'UTC' }));
  const timezoneOffset = userDate.getTime() - utcDate.getTime();
  
  // Adjust for timezone and return timestamp
  return date.getTime() - timezoneOffset;
};

/**
 * Convert UTC timestamp to local date/time values for form inputs
 * @param timestamp - Unix timestamp in milliseconds
 * @param timezone - Optional timezone, defaults to user's timezone
 * @returns Object with date and time strings for form inputs
 */
export const convertUTCToLocal = (
  timestamp: number,
  timezone?: string
): { dateValue: string; timeValue: string } => {
  const tz = timezone || getUserTimezone();
  const date = new Date(timestamp);
  
  // Format date in user's timezone
  const localDateStr = date.toLocaleDateString('en-CA', { timeZone: tz }); // YYYY-MM-DD format
  const localTimeStr = date.toLocaleTimeString('en-GB', { 
    timeZone: tz,
    hour12: false,
    hour: '2-digit',
    minute: '2-digit'
  }); // HH:MM format
  
  return {
    dateValue: localDateStr,
    timeValue: localTimeStr
  };
};

/**
 * Check if a given date/time is in the future
 * @param dateValue - Date string in YYYY-MM-DD format
 * @param timeValue - Time string in HH:MM format
 * @param timezone - Optional timezone, defaults to user's timezone
 * @returns True if the date/time is in the future
 */
export const isDateTimeInFuture = (
  dateValue: string,
  timeValue: string,
  timezone?: string
): boolean => {
  const timestamp = convertLocalToUTC(dateValue, timeValue, timezone);
  return timestamp > Date.now();
};

/**
 * Get a formatted timezone name for display
 * @param timezone - Optional timezone, defaults to user's timezone
 * @returns User-friendly timezone name
 */
export const getTimezoneDisplayName = (timezone?: string): string => {
  const tz = timezone || getUserTimezone();
  
  try {
    // Get timezone abbreviation (e.g., "EST", "PST")
    const shortName = new Date().toLocaleDateString('en-US', {
      timeZone: tz,
      timeZoneName: 'short'
    }).split(', ').pop() || tz;
    
    // Get timezone offset (e.g., "GMT-5")
    const offset = new Date().toLocaleDateString('en-US', {
      timeZone: tz,
      timeZoneName: 'longOffset'
    }).split(', ').pop() || '';
    
    return `${shortName} (${offset})`;
  } catch (error) {
    console.error('Failed to format timezone name:', error);
    return tz;
  }
};

/**
 * Get minimum datetime values for scheduling (must be in future)
 * @returns Object with minimum date and time values for form inputs
 */
export const getMinimumDateTime = (): { minDate: string; minTime?: string } => {
  const now = new Date();
  const userTimezone = getUserTimezone();
  
  // Add 5 minutes buffer to current time
  const minDateTime = new Date(now.getTime() + 5 * 60 * 1000);
  
  const { dateValue, timeValue } = convertUTCToLocal(minDateTime.getTime(), userTimezone);
  
  // If minimum time is today, return the time. Otherwise, allow any time.
  const todayDate = convertUTCToLocal(now.getTime(), userTimezone).dateValue;
  const isToday = dateValue === todayDate;
  
  return {
    minDate: dateValue,
    minTime: isToday ? timeValue : undefined
  };
};

/**
 * Get default datetime values for scheduling (current time + 1 hour)
 * @returns Object with default date and time values for form inputs
 */
export const getDefaultDateTime = (): { dateValue: string; timeValue: string } => {
  const now = new Date();
  const userTimezone = getUserTimezone();
  
  // Add 1 hour to current time as default
  const defaultDateTime = new Date(now.getTime() + 60 * 60 * 1000);
  
  const { dateValue, timeValue } = convertUTCToLocal(defaultDateTime.getTime(), userTimezone);
  
  return {
    dateValue,
    timeValue
  };
};