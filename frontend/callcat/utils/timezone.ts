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
  
  // Create the date/time string and use Temporal-like approach with Intl
  // We want: "When it's 6:00 PM in EDT, what's the UTC timestamp?"
  
  // Parse the input values
  const [year, month, day] = dateValue.split('-').map(Number);
  const [hours, minutes] = timeValue.split(':').map(Number);
  
  // Use the inverse approach: find what UTC time will display as our desired local time
  // Start with our desired local time as if it were UTC
  let testTimestamp = Date.UTC(year, month - 1, day, hours, minutes, 0, 0);
  
  // Check what this timestamp displays as in our target timezone
  const testDate = new Date(testTimestamp);
  const displayedTime = new Intl.DateTimeFormat('en-CA', {
    timeZone: tz,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).format(testDate);
  
  // Parse what time it actually shows
  const [displayedDate, displayedTimeStr] = displayedTime.split(', ');
  const [dispYear, dispMonth, dispDay] = displayedDate.split('-').map(Number);
  const [dispHours, dispMinutes] = displayedTimeStr.split(':').map(Number);
  
  // Calculate the difference between what we want and what we got
  const wantedTimestamp = Date.UTC(year, month - 1, day, hours, minutes, 0, 0);
  const actuallyDisplayed = Date.UTC(dispYear, dispMonth - 1, dispDay, dispHours, dispMinutes, 0, 0);
  
  const offset = actuallyDisplayed - wantedTimestamp;
  
  // Adjust our test timestamp by the offset to get the correct UTC time
  return testTimestamp - offset;
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
 * Generate simplified timezone options for select dropdown
 * @returns Array of curated timezone options with user-friendly labels and GMT offsets
 */
export const generateTimezoneOptions = (): { value: string; label: string; offset: number }[] => {
  // Curated list of most commonly used timezones worldwide
  const curatedTimezones = [
    // UTC first
    'UTC',
    
    // North America - US
    'America/New_York',      // Eastern Time
    'America/Chicago',       // Central Time  
    'America/Denver',        // Mountain Time
    'America/Los_Angeles',   // Pacific Time
    'America/Anchorage',     // Alaska Time
    'Pacific/Honolulu',      // Hawaii Time
    
    // North America - Canada
    'America/Toronto',       // Eastern Canada
    'America/Vancouver',     // Pacific Canada
    
    // Central/South America
    'America/Mexico_City',   // Mexico
    'America/Sao_Paulo',     // Brazil
    'America/Argentina/Buenos_Aires', // Argentina
    
    // Europe
    'Europe/London',         // UK
    'Europe/Dublin',         // Ireland
    'Europe/Paris',          // France
    'Europe/Berlin',         // Germany
    'Europe/Rome',           // Italy
    'Europe/Madrid',         // Spain
    'Europe/Amsterdam',      // Netherlands
    'Europe/Stockholm',      // Sweden
    'Europe/Moscow',         // Russia
    
    // Asia
    'Asia/Dubai',           // UAE
    'Asia/Kolkata',         // India
    'Asia/Shanghai',        // China
    'Asia/Tokyo',           // Japan
    'Asia/Seoul',           // South Korea
    'Asia/Singapore',       // Singapore
    'Asia/Hong_Kong',       // Hong Kong
    
    // Oceania  
    'Australia/Sydney',     // Australia East
    'Australia/Perth',      // Australia West
    'Pacific/Auckland',     // New Zealand
  ];

  // Helper function to get timezone info
  const getTimezoneInfo = (tz: string) => {
    try {
      const date = new Date();
      
      // Get the timezone offset using proper Intl formatting
      const formatter = new Intl.DateTimeFormat('en', {
        timeZone: tz,
        timeZoneName: 'longOffset'
      });
      
      const parts = formatter.formatToParts(date);
      const offsetPart = parts.find(part => part.type === 'timeZoneName');
      let offsetString = 'GMT+00:00';
      let offsetMinutes = 0;
      
      if (offsetPart?.value && offsetPart.value !== 'GMT') {
        offsetString = offsetPart.value;
        // Parse offset like "GMT-04:00" to get minutes
        const match = offsetPart.value.match(/GMT([+-])(\d{2}):(\d{2})/);
        if (match) {
          const sign = match[1] === '+' ? -1 : 1; // Reversed because JS timezone offsets are inverted
          const hours = parseInt(match[2], 10);
          const mins = parseInt(match[3], 10);
          offsetMinutes = sign * (hours * 60 + mins);
        }
      }
      
      // Special cases for better display names
      const specialNames: Record<string, string> = {
        'UTC': 'UTC',
        'America/New_York': 'Eastern Time (US)',
        'America/Chicago': 'Central Time (US)',
        'America/Denver': 'Mountain Time (US)', 
        'America/Los_Angeles': 'Pacific Time (US)',
        'America/Anchorage': 'Alaska Time',
        'Pacific/Honolulu': 'Hawaii Time',
        'America/Toronto': 'Eastern Time (Canada)',
        'America/Vancouver': 'Pacific Time (Canada)',
        'America/Mexico_City': 'Mexico City',
        'America/Sao_Paulo': 'São Paulo',
        'America/Argentina/Buenos_Aires': 'Buenos Aires',
        'Europe/London': 'London',
        'Europe/Dublin': 'Dublin',
        'Europe/Paris': 'Paris',
        'Europe/Berlin': 'Berlin',
        'Europe/Rome': 'Rome',
        'Europe/Madrid': 'Madrid',
        'Europe/Amsterdam': 'Amsterdam',
        'Europe/Stockholm': 'Stockholm',
        'Europe/Moscow': 'Moscow',
        'Asia/Dubai': 'Dubai',
        'Asia/Kolkata': 'Mumbai/Delhi',
        'Asia/Shanghai': 'Beijing/Shanghai',
        'Asia/Tokyo': 'Tokyo',
        'Asia/Seoul': 'Seoul',
        'Asia/Singapore': 'Singapore',
        'Asia/Hong_Kong': 'Hong Kong',
        'Australia/Sydney': 'Sydney',
        'Australia/Perth': 'Perth',
        'Pacific/Auckland': 'Auckland'
      };
      
      const displayName = specialNames[tz] || tz.split('/').pop()?.replace(/_/g, ' ') || tz;
      
      return {
        value: tz,
        label: `${displayName} (${offsetString})`,
        offset: offsetMinutes
      };
    } catch (error) {
      return null;
    }
  };
  
  try {
    // Process curated timezones and sort by offset
    const timezoneOptions = curatedTimezones
      .map(tz => getTimezoneInfo(tz))
      .filter(Boolean)
      .sort((a, b) => {
        // Sort by offset first, then by name
        if (a!.offset !== b!.offset) return a!.offset - b!.offset;
        return a!.label.localeCompare(b!.label);
      });
    
    return timezoneOptions as { value: string; label: string; offset: number }[];
    
  } catch (error) {
    console.error('Failed to generate timezone options:', error);
    
    // Fallback to minimal hardcoded list
    return [
      { value: 'UTC', label: 'UTC (GMT+00:00)', offset: 0 },
      { value: 'America/New_York', label: 'Eastern Time (US) (GMT-05:00)', offset: 300 },
      { value: 'America/Chicago', label: 'Central Time (US) (GMT-06:00)', offset: 360 },
      { value: 'America/Denver', label: 'Mountain Time (US) (GMT-07:00)', offset: 420 },
      { value: 'America/Los_Angeles', label: 'Pacific Time (US) (GMT-08:00)', offset: 480 },
      { value: 'Europe/London', label: 'London (GMT+00:00)', offset: 0 },
      { value: 'Europe/Paris', label: 'Paris (GMT+01:00)', offset: -60 },
      { value: 'Asia/Tokyo', label: 'Tokyo (GMT+09:00)', offset: -540 },
      { value: 'Asia/Shanghai', label: 'Beijing/Shanghai (GMT+08:00)', offset: -480 },
      { value: 'Australia/Sydney', label: 'Sydney (GMT+11:00)', offset: -660 },
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
  
  // Add 1 hour to current time and convert to the selected timezone
  const defaultTime = new Date(now.getTime() + 60 * 60 * 1000);
  
  // Ensure it's at least 10 minutes from actual current time
  const minTime = new Date(now.getTime() + 10 * 60 * 1000);
  const finalTime = defaultTime > minTime ? defaultTime : new Date(now.getTime() + 60 * 60 * 1000);
  
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
  
  // Add 2 minutes buffer to current time
  const minDateTime = new Date(now.getTime() + 2 * 60 * 1000);
  
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
  
  // Get current time and add 1 hour
  const now = new Date();
  const defaultDateTime = new Date(now.getTime() + 60 * 60 * 1000);
  
  // Ensure the time is at least 2 minutes in the future from actual current time
  const minFutureTime = new Date(now.getTime() + 2 * 60 * 1000);
  const finalDateTime = defaultDateTime > minFutureTime ? defaultDateTime : new Date(now.getTime() + 60 * 60 * 1000);
  
  const { dateValue, timeValue } = convertUTCToLocal(finalDateTime.getTime(), tz);
  
  return {
    dateValue,
    timeValue
  };
};