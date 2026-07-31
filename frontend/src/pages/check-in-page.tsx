import { CheckCircle2, Loader2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { Alert } from '../components/alert'
import { EmptyState } from '../components/empty-state'
import { checkInAttendee } from '../lib/api'
import { getFriendlyErrorMessage } from '../lib/errors'

type CheckInStatus = 'loading' | 'success' | 'error'

export function CheckInPage() {
    const { token } = useParams()
    const [status, setStatus] = useState<CheckInStatus>('loading')
    const [message, setMessage] = useState('Checking in...')

    useEffect(() => {
        if (!token) {
            return
        }

        checkInAttendee(token)
            .then(() => {
                setStatus('success')
                setMessage('Check-in completed.')
            })
            .catch((error: Error) => {
                setStatus('error')
                setMessage(getFriendlyErrorMessage(error, 'Could not complete check-in.'))
            })
    }, [token])

    if (!token) {
        return <Navigate to="/events" replace />
    }

    return (
        <main className="mx-auto flex w-full max-w-lg flex-col gap-5 py-12">
            {status === 'loading' && (
                <EmptyState>
                    <span className="inline-flex items-center gap-2">
                        <Loader2 className="size-4 animate-spin" />
                        {message}
                    </span>
                </EmptyState>
            )}

            {status === 'success' && (
                <section className="border border-white/10 rounded-lg p-5 flex flex-col gap-4 bg-white/[0.02]">
                    <div className="flex items-center gap-3">
                        <CheckCircle2 className="size-5 text-emerald-300" />
                        <h1 className="text-2xl font-bold">Check-in completed</h1>
                    </div>
                    <p className="text-sm text-zinc-400">{message}</p>
                    <Link
                        to="/events"
                        className="inline-flex self-start rounded-lg border border-white/10 bg-white/5 px-4 py-2 text-sm font-semibold text-zinc-200 hover:bg-white/10"
                    >
                        Back to events
                    </Link>
                </section>
            )}

            {status === 'error' && (
                <>
                    <Alert>
                        {message}
                    </Alert>
                    <Link to="/events" className="text-sm text-zinc-400 hover:text-zinc-200">
                        Back to events
                    </Link>
                </>
            )}
        </main>
    )
}
