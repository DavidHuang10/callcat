import { useState, useEffect, useCallback } from 'react';
import { useCallList } from './useCallList';
import { CallResponse, PaginatedCallsResponse } from '@/types';

interface UseCallsForDashboardReturn {
  scheduledCalls: CallResponse[];
  completedCalls: CallResponse[];
  scheduledLoading: boolean;
  completedLoading: boolean;
  scheduledError: string | null;
  completedError: string | null;
  scheduledPage: number;
  completedPage: number;
  hasMoreScheduled: boolean;
  hasMoreCompleted: boolean;
  scheduledTotal: number;
  completedTotal: number;
  refreshAll: () => Promise<void>;
  setScheduledPage: (page: number) => void;
  setCompletedPage: (page: number) => void;
}

export function useCallsForDashboard(): UseCallsForDashboardReturn {
  // Hook for scheduled calls
  const {
    calls: scheduledCalls,
    loading: scheduledLoading,
    error: scheduledError,
    page: scheduledPage,
    hasMore: hasMoreScheduled,
    total: scheduledTotal,
    refresh: refreshScheduled,
    setPage: setScheduledPage,
  } = useCallList({
    status: 'SCHEDULED',
    limit: 6,
    autoRefresh: true,
    refreshInterval: 30000, // Refresh every 30 seconds
  });

  // Hook for completed calls
  const {
    calls: completedCalls,
    loading: completedLoading,
    error: completedError,
    page: completedPage,
    hasMore: hasMoreCompleted,
    total: completedTotal,
    refresh: refreshCompleted,
    setPage: setCompletedPage,
  } = useCallList({
    status: 'COMPLETED',
    limit: 6,
    autoRefresh: false, // Don't auto-refresh completed calls as frequently
  });

  const refreshAll = useCallback(async () => {
    await Promise.all([refreshScheduled(), refreshCompleted()]);
  }, [refreshScheduled, refreshCompleted]);

  return {
    scheduledCalls,
    completedCalls,
    scheduledLoading,
    completedLoading,
    scheduledError,
    completedError,
    scheduledPage,
    completedPage,
    hasMoreScheduled,
    hasMoreCompleted,
    scheduledTotal,
    completedTotal,
    refreshAll,
    setScheduledPage,
    setCompletedPage,
  };
}