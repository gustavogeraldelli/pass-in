import { TriangleAlert } from 'lucide-react'
import { ReactNode } from 'react'

interface AlertProps {
    children: ReactNode
}

export function Alert({ children }: AlertProps) {
    return (
        <div className="flex items-center gap-2 border border-red-400/30 bg-red-400/10 rounded-lg p-4 text-sm text-red-200">
            <TriangleAlert className="size-4 shrink-0" />
            <span>{children}</span>
        </div>
    )
}
