"use client"

import { AlertTriangle, Phone, User, Calendar } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { CallResponse } from "@/types"
import { formatPhoneForDisplay } from "@/utils/phone"

interface DeleteConfirmationDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  call: CallResponse | null
  onConfirm: () => void
  isDeleting?: boolean
}


const formatTimestamp = (timestamp: number) => {
  const date = new Date(timestamp)
  return date.toLocaleDateString("en-US", { 
    year: "numeric", 
    month: "long", 
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  })
}

export default function DeleteConfirmationDialog({
  open,
  onOpenChange,
  call,
  onConfirm,
  isDeleting = false,
}: DeleteConfirmationDialogProps) {
  if (!call) return null

  const isScheduled = call.status === "SCHEDULED"
  const displayDate = call.scheduledFor ? formatTimestamp(call.scheduledFor) : formatTimestamp(call.createdAt)

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader className="text-left">
          <DialogTitle className="flex items-center gap-2 text-red-700">
            <AlertTriangle className="w-5 h-5" />
            Delete Call
          </DialogTitle>
          <DialogDescription className="text-gray-600 mt-2">
            Are you sure you want to delete this call? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>

        {/* Call Details */}
        <div className="bg-gray-50 rounded-lg p-4 my-4 space-y-3">
          <div className="flex items-center gap-2">
            <User className="w-4 h-4 text-gray-500" />
            <span className="font-medium text-gray-800">{call.calleeName}</span>
          </div>
          
          <div className="flex items-center gap-2">
            <Phone className="w-4 h-4 text-gray-500" />
            <span className="text-sm text-gray-700">{formatPhoneForDisplay(call.phoneNumber)}</span>
          </div>

          <div className="flex items-center gap-2">
            <Calendar className="w-4 h-4 text-gray-500" />
            <span className="text-sm text-gray-700">
              {isScheduled ? `Scheduled for ${displayDate}` : `Created ${displayDate}`}
            </span>
          </div>

          {call.subject && (
            <div className="pt-2 border-t border-gray-200">
              <p className="text-sm font-medium text-gray-800 mb-1">Subject:</p>
              <p className="text-sm text-gray-600 line-clamp-2">{call.subject}</p>
            </div>
          )}
        </div>

        <DialogFooter className="gap-2">
          <Button 
            variant="outline" 
            onClick={() => onOpenChange(false)}
            disabled={isDeleting}
            className="flex-1"
          >
            Cancel
          </Button>
          <Button 
            variant="destructive" 
            onClick={onConfirm}
            disabled={isDeleting}
            className="flex-1 bg-red-600 hover:bg-red-700"
          >
            {isDeleting ? (
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                Deleting...
              </div>
            ) : (
              "Delete Call"
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}