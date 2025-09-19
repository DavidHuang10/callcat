import { useMemo } from 'react'
import { PAGINATION } from '@/constants/ui'

interface UsePaginationProps<T> {
  items: T[]
  currentPage: number
  itemsPerPage?: number
}

interface UsePaginationReturn<T> {
  paginatedItems: T[]
  totalPages: number
  hasNextPage: boolean
  hasPrevPage: boolean
  startIndex: number
  endIndex: number
  showPagination: boolean
}

export function usePagination<T>({
  items,
  currentPage,
  itemsPerPage = PAGINATION.CALLS_PER_PAGE,
}: UsePaginationProps<T>): UsePaginationReturn<T> {
  return useMemo(() => {
    const startIndex = currentPage * itemsPerPage
    const endIndex = startIndex + itemsPerPage
    const paginatedItems = items.slice(startIndex, endIndex)
    const totalPages = Math.ceil(items.length / itemsPerPage)

    return {
      paginatedItems,
      totalPages,
      hasNextPage: endIndex < items.length,
      hasPrevPage: currentPage > 0,
      startIndex,
      endIndex,
      showPagination: items.length > itemsPerPage,
    }
  }, [items, currentPage, itemsPerPage])
}