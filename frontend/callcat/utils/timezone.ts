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
 * Generate comprehensive timezone options for select dropdown
 * @returns Array of timezone options with user-friendly labels and GMT offsets
 */
export const generateTimezoneOptions = (): { value: string; label: string; offset: number }[] => {
  try {
    // Get all supported timezones
    const supportedTimezones = Intl.supportedValuesOf('timeZone');
    
    // Popular timezones to show at the top
    const popularTimezones = [
      'UTC',
      'America/New_York',
      'America/Chicago', 
      'America/Denver',
      'America/Los_Angeles',
      'Europe/London',
      'Europe/Paris',
      'Europe/Berlin',
      'Asia/Tokyo',
      'Asia/Shanghai',
      'Asia/Kolkata',
      'Australia/Sydney'
    ];
    
    const now = new Date();
    const timezoneOptions = [];
    
    // Helper function to get timezone info
    const getTimezoneInfo = (tz: string) => {
      try {
        const date = new Date();
        
        // Get the timezone offset in minutes
        const utcDate = new Date(date.toLocaleString('en-US', { timeZone: 'UTC' }));
        const tzDate = new Date(date.toLocaleString('en-US', { timeZone: tz }));
        const offsetMinutes = (utcDate.getTime() - tzDate.getTime()) / (1000 * 60);
        
        // Format offset as GMT+/-X:XX
        const hours = Math.floor(Math.abs(offsetMinutes) / 60);
        const minutes = Math.abs(offsetMinutes) % 60;
        const sign = offsetMinutes <= 0 ? '+' : '-';
        const offsetString = `GMT${sign}${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
        
        // Get timezone name/abbreviation
        let displayName = tz.split('/').pop()?.replace(/_/g, ' ') || tz;
        
        // Special cases for better display names
        const specialNames: Record<string, string> = {
          'America/New_York': 'Eastern Time',
          'America/Chicago': 'Central Time',
          'America/Denver': 'Mountain Time', 
          'America/Los_Angeles': 'Pacific Time',
          'Europe/London': 'London Time',
          'Europe/Paris': 'Central European Time',
          'Europe/Berlin': 'Central European Time',
          'Asia/Tokyo': 'Japan Standard Time',
          'Asia/Shanghai': 'China Standard Time',
          'Asia/Kolkata': 'India Standard Time',
          'Australia/Sydney': 'Australian Eastern Time',
          'UTC': 'Coordinated Universal Time'
        };
        
        if (specialNames[tz]) {
          displayName = specialNames[tz];
        }
        
        return {
          value: tz,
          label: `${displayName} (${offsetString})`,
          offset: offsetMinutes,
          isPopular: popularTimezones.includes(tz)
        };
      } catch (error) {
        return null;
      }
    };
    
    // Process all timezones
    const allTimezones = supportedTimezones
      .map(tz => getTimezoneInfo(tz))
      .filter(Boolean)
      .sort((a, b) => {
        // Sort by popularity first, then by offset, then by name
        if (a!.isPopular && !b!.isPopular) return -1;
        if (!a!.isPopular && b!.isPopular) return 1;
        if (a!.offset !== b!.offset) return a!.offset - b!.offset;
        return a!.label.localeCompare(b!.label);
      });
    
    return allTimezones as { value: string; label: string; offset: number }[];
    
  } catch (error) {
    console.error('Failed to generate timezone options:', error);
    
    // Fallback to hardcoded popular timezones
    return [
      { value: 'UTC', label: 'UTC (GMT+00:00)', offset: 0 },
      { value: 'America/New_York', label: 'Eastern Time (GMT-05:00)', offset: 300 },
      { value: 'America/Chicago', label: 'Central Time (GMT-06:00)', offset: 360 },
      { value: 'America/Denver', label: 'Mountain Time (GMT-07:00)', offset: 420 },
      { value: 'America/Los_Angeles', label: 'Pacific Time (GMT-08:00)', offset: 480 },
      { value: 'Europe/London', label: 'London Time (GMT+00:00)', offset: 0 },
      { value: 'Europe/Paris', label: 'Central European Time (GMT+01:00)', offset: -60 },
      { value: 'Asia/Tokyo', label: 'Japan Standard Time (GMT+09:00)', offset: -540 },
      { value: 'Asia/Shanghai', label: 'China Standard Time (GMT+08:00)', offset: -480 },
      { value: 'Australia/Sydney', label: 'Australian Eastern Time (GMT+11:00)', offset: -660 },
    ];
  }
};

/**
 * Get timezone-aware default datetime that ensures the time is always in the future
 * @param selectedTimezone - The timezone to calculate the default time for
 * @returns Object with default date and time values for form inputs
 */
export const getTimezoneAwareDefaultTime = (selectedTimezone: string): { dateValue: string; timeValue: string } => {
  const now = new Date();
  
  // Get current time in both local browser timezone and selected timezone
  const nowInSelected = new Date(now.toLocaleString('en-US', { timeZone: selectedTimezone }));
  
  // Add 1 hour and 5 minutes buffer to current time in selected timezone
  const defaultTime = new Date(nowInSelected.getTime() + 65 * 60 * 1000);
  
  // Ensure it's at least 10 minutes from actual current time
  const minTime = new Date(now.getTime() + 10 * 60 * 1000);
  const finalTime = defaultTime > minTime ? defaultTime : new Date(now.getTime() + 70 * 60 * 1000);
  
  return convertUTCToLocal(finalTime.getTime(), selectedTimezone);
};

/**
 * Get minimum datetime values for scheduling (must be in future)
 * @param timezone - Optional timezone, defaults to user's timezone
 * @returns Object with minimum date and time values for form inputs
 */
export const getMinimumDateTime = (timezone?: string): { minDate: string; minTime?: string } => {
  const tz = timezone || getUserTimezone();
  const now = new Date();
  
  // Add 5 minutes buffer to current time
  const minDateTime = new Date(now.getTime() + 5 * 60 * 1000);
  
  const { dateValue, timeValue } = convertUTCToLocal(minDateTime.getTime(), tz);
  
  // If minimum time is today in the specified timezone, return the time. Otherwise, allow any time.
  const todayDate = convertUTCToLocal(now.getTime(), tz).dateValue;
  const isToday = dateValue === todayDate;
  
  return {
    minDate: dateValue,
    minTime: isToday ? timeValue : undefined
  };
};

/**
 * Get default datetime values for scheduling (current time + 1 hour)
 * @param timezone - Optional timezone, defaults to user's timezone
 * @returns Object with default date and time values for form inputs
 */
export const getDefaultDateTime = (timezone?: string): { dateValue: string; timeValue: string } => {
  const tz = timezone || getUserTimezone();
  
  // Get current time in the specified timezone
  const now = new Date();
  const currentTimeInTz = new Date(now.toLocaleString('en-US', { timeZone: tz }));
  
  // Add 1 hour to current time in the specified timezone
  const defaultDateTime = new Date(currentTimeInTz.getTime() + 60 * 60 * 1000);
  
  // Ensure the time is at least 5 minutes in the future from actual current time
  const minFutureTime = new Date(now.getTime() + 5 * 60 * 1000);
  const finalDateTime = defaultDateTime > minFutureTime ? defaultDateTime : new Date(now.getTime() + 65 * 60 * 1000); // 1 hour 5 minutes
  
  const { dateValue, timeValue } = convertUTCToLocal(finalDateTime.getTime(), tz);
  
  return {
    dateValue,
    timeValue
  };
};