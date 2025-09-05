import { useState, useEffect, useCallback } from 'react';
import { apiService } from '@/lib/api';
import { CallResponse, CallListResponse } from '@/types';

interface UseCallListOptions {
  status?: 'SCHEDULED' | 'COMPLETED';
  limit?: number;
  autoRefresh?: boolean;
  refreshInterval?: number;
}

interface UseCallListReturn {
  calls: CallResponse[];
  loading: boolean;
  error: string | null;
  page: number;
  hasMore: boolean;
  total: number;
  refresh: () => Promise<void>;
  loadMore: () => Promise<void>;
  setPage: (page: number) => void;
}

export function useCallList({
  status,
  limit = 6,
  autoRefresh = false,
  refreshInterval = 30000,
}: UseCallListOptions = {}): UseCallListReturn {
  const [calls, setCalls] = useState<CallResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPageState] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [total, setTotal] = useState(0);

  const fetchCalls = useCallback(async (pageNum: number = 0, append: boolean = false) => {
    try {
      setLoading(true);
      setError(null);

      const offset = pageNum * limit;
      const response: CallListResponse = await apiService.getCalls(status, limit, offset);

      if (append) {
        setCalls(prev => [...prev, ...response.calls]);
      } else {
        setCalls(response.calls);
      }

      setTotal(response.total || response.calls.length);
      setHasMore(response.calls.length === limit);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to fetch calls';
      setError(errorMessage);
      console.error('Error fetching calls:', err);
    } finally {
      setLoading(false);
    }
  }, [status, limit]);

  const refresh = useCallback(async () => {
    await fetchCalls(0, false);
    setPageState(0);
  }, [fetchCalls]);

  const loadMore = useCallback(async () => {
    if (!hasMore || loading) return;
    const nextPage = page + 1;
    await fetchCalls(nextPage, true);
    setPageState(nextPage);
  }, [hasMore, loading, page, fetchCalls]);

  const setPage = useCallback((newPage: number) => {
    setPageState(newPage);
    fetchCalls(newPage, false);
  }, [fetchCalls]);

  // Initial fetch
  useEffect(() => {
    fetchCalls(0, false);
  }, [fetchCalls]);

  // Auto-refresh functionality (timer-based)
  useEffect(() => {
    if (!autoRefresh) return;

    const interval = setInterval(refresh, refreshInterval);
    return () => clearInterval(interval);
  }, [autoRefresh, refreshInterval, refresh]);

  // Smart refresh on page visibility change (when user returns to tab/page)
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        // User returned to the tab - refresh to check for any status changes
        refresh();
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
  }, [refresh]);

  return {
    calls,
    loading,
    error,
    page,
    hasMore,
    total,
    refresh,
    loadMore,
    setPage,
  };
}