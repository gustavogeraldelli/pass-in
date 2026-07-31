import { ArrowLeft, Check, Loader2, QrCode, Ticket, TriangleAlert } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, Navigate, useParams } from 'react-router'
import { QRCodeSVG } from 'qrcode.react'
import { AttendeeBadge, checkInAttendee, getAttendeeBadge } from '../lib/api'
import { Alert } from '../components/alert'
import { Button } from '../components/button'
import { EmptyState } from '../components/empty-state'
import { getFriendlyErrorMessage } from '../lib/errors'

type CheckInStatus = 'idle' | 'loading' | 'success' | 'error'

export function AttendeeBadgePage() {
    const { attendeeId } = useParams()
    const [badge, setBadge] = useState<AttendeeBadge | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [checkInStatus, setCheckInStatus] = useState<CheckInStatus>('idle')
    const [checkInMessage, setCheckInMessage] = useState<string | null>(null)

    useEffect(() => {
        if (!attendeeId) {
            return
        }

        getAttendeeBadge(attendeeId)
            .then((data) => {
                setBadge(data)
                setError(null)
            })
            .catch((error: Error) => setError(getFriendlyErrorMessage(error, 'Could not load the badge.')))
            .finally(() => setIsLoading(false))
    }, [attendeeId])

    if (!attendeeId) {
        return <Navigate to="/events" replace />
    }

    function handleCheckIn() {
        setCheckInStatus('loading')
        setCheckInMessage(null)

        if (!badge) {
            return
        }

        checkInAttendee(badge.checkInToken)
            .then(() => {
                setCheckInStatus('success')
                setCheckInMessage('Check-in completed.')
            })
            .catch((error: Error) => {
                setCheckInStatus('error')
                setCheckInMessage(getFriendlyErrorMessage(error, 'Could not complete check-in.'))
            })
    }

    const qrCodeUrl = badge ? `${window.location.origin}/check-ins/${badge.checkInToken}` : ''

    return (
        <main className="flex flex-col gap-5">
            <Link to="/events" className="inline-flex items-center gap-2 text-sm text-zinc-400 hover:text-zinc-200">
                <ArrowLeft className="size-4" />
                Events
            </Link>

            {isLoading && (
                <EmptyState>
                    Loading badge...
                </EmptyState>
            )}

            {error && (
                <Alert>
                    {error}
                </Alert>
            )}

            {badge && (
                <div className="grid gap-5 lg:grid-cols-[1fr_320px]">
                    <section className="border border-white/10 rounded-lg p-5 flex flex-col gap-5 bg-white/[0.02]">
                        <div className="flex items-center gap-3">
                            <Ticket className="size-5 text-emerald-300" />
                            <h1 className="text-2xl font-bold">Attendee badge</h1>
                        </div>

                        <div className="grid gap-4 sm:grid-cols-2">
                            <div>
                                <span className="text-xs uppercase text-zinc-500">Name</span>
                                <p className="font-medium text-white">{badge.name}</p>
                            </div>
                            <div>
                                <span className="text-xs uppercase text-zinc-500">Email</span>
                                <p className="font-medium text-white">{badge.email}</p>
                            </div>
                            <div>
                                <span className="text-xs uppercase text-zinc-500">Event</span>
                                <p className="font-mono text-sm text-zinc-300">{badge.eventId}</p>
                            </div>
                        </div>

                        <div className="flex flex-wrap items-center gap-3">
                            <Button
                                type="button"
                                onClick={handleCheckIn}
                                disabled={checkInStatus === 'loading' || checkInStatus === 'success'}
                            >
                                {checkInStatus === 'loading' ? <Loader2 className="size-4 animate-spin" /> : <Check className="size-4" />}
                                Check in
                            </Button>

                            {checkInMessage && (
                                <span className={checkInStatus === 'error' ? 'text-sm text-red-200' : 'text-sm text-emerald-300'}>
                                    {checkInStatus === 'error' && <TriangleAlert className="mr-1 inline size-4" />}
                                    {checkInMessage}
                                </span>
                            )}
                        </div>
                    </section>

                    <aside className="border border-white/10 rounded-lg p-5 flex flex-col items-center gap-4 bg-white/[0.02]">
                        <div className="flex items-center gap-2 text-sm text-zinc-400">
                            <QrCode className="size-4 text-emerald-300" />
                            Check-in QR Code
                        </div>
                        <div className="rounded-lg bg-white p-4">
                            <QRCodeSVG value={qrCodeUrl} size={224} />
                        </div>
                    </aside>
                </div>
            )}
        </main>
    )
}
