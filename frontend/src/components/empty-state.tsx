import { ReactNode } from 'react'

interface EmptyStateProps {
    children: ReactNode
}

export function EmptyState({ children }: EmptyStateProps) {
    return (
        <div className="border border-white/10 rounded-lg p-4 text-sm text-zinc-400">
            {children}
        </div>
    )
}
