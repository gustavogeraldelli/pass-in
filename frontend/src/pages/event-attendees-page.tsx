import { ArrowLeft, Check, CheckCircle2, Copy, Download, ExternalLink, Ticket, UserCheck, Users } from 'lucide-react'
import { ReactNode, useEffect, useState } from 'react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { AttendeeList } from '../components/attendee-list'
import { Alert } from '../components/alert'
import { Button } from '../components/button'
import { EmptyState } from '../components/empty-state'
import { Event, exportEventAttendees, getEvent } from '../lib/api'
import { getFriendlyErrorMessage } from '../lib/errors'
import { useAuth } from '../lib/use-auth'

export function EventAttendeesPage() {
    const { eventId } = useParams()
    const { authenticatedRequest, isAuthenticated } = useAuth()
    const [event, setEvent] = useState<Event | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [isPublicLinkCopied, setIsPublicLinkCopied] = useState(false)
    const [isExporting, setIsExporting] = useState(false)

    useEffect(() => {
        if (!eventId)
            return

        getEvent(eventId)
            .then((data) => {
                setEvent(data.event)
                setError(null)
            })
            .catch((error: Error) => setError(getFriendlyErrorMessage(error, 'Could not load the event.')))
            .finally(() => setIsLoading(false))
    }, [eventId])

    if (!eventId)
        return <Navigate to="/events" replace />

    if (!isAuthenticated)
        return <Navigate to="/login" replace />

    const remainingSeats = event ? Math.max(event.maximumAttendees - event.numberOfAttendees, 0) : 0
    const occupancyRate = event ? Math.min(Math.round((event.numberOfAttendees / event.maximumAttendees) * 100), 100) : 0
    const checkInRate = event && event.numberOfAttendees > 0
        ? Math.min(Math.round((event.numberOfCheckIns / event.numberOfAttendees) * 100), 100)
        : 0
    const isEventFull = event ? remainingSeats === 0 : false
    const publicEventUrl = event ? `${window.location.origin}/events/${event.id}` : ''

    function handleCopyPublicLink() {
        navigator.clipboard.writeText(publicEventUrl).then(() => {
            setIsPublicLinkCopied(true)
            window.setTimeout(() => setIsPublicLinkCopied(false), 2000)
        })
    }

    function handleExportAttendees() {
        if (!event)
            return

        setIsExporting(true)
        authenticatedRequest((accessToken) => exportEventAttendees(event.id, accessToken))
            .then((blob) => {
                const url = URL.createObjectURL(blob)
                const link = document.createElement('a')
                link.href = url
                link.download = `${event.slug}-attendees.csv`
                link.click()
                URL.revokeObjectURL(url)
            })
            .catch((error: Error) => setError(getFriendlyErrorMessage(error, 'Could not export attendees.')))
            .finally(() => setIsExporting(false))
    }

    return (
        <main className="flex flex-col gap-5">
            <Link to="/events" className="inline-flex items-center gap-2 text-sm text-zinc-400 hover:text-zinc-200">
                <ArrowLeft className="size-4" />
                Events
            </Link>

            {isLoading && (
                <EmptyState>
                    Loading event...
                </EmptyState>
            )}

            {error && (
                <Alert>
                    {error}
                </Alert>
            )}

            {event && (
                <div className="grid gap-5">
                    <section className="flex flex-col gap-4">
                        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                            <div className="flex flex-col gap-2">
                                <div className="flex items-center gap-3">
                                    <h1 className="text-2xl font-bold">{event.title}</h1>
                                    <div className="inline-flex items-center gap-2 text-sm text-zinc-400">
                                        <Users className="size-4 text-emerald-300" />
                                        <span>{event.numberOfAttendees}/{event.maximumAttendees}</span>
                                    </div>
                                </div>
                                <p className="text-sm text-zinc-400">{event.details}</p>
                            </div>

                            <div className="flex flex-col gap-2 sm:flex-row">
                                <Button type="button" variant="secondary" onClick={handleExportAttendees} disabled={isExporting}>
                                    <Download className="size-4" />
                                    {isExporting ? 'Exporting' : 'Export CSV'}
                                </Button>

                                <Button type="button" variant="secondary" onClick={handleCopyPublicLink}>
                                    {isPublicLinkCopied ? <Check className="size-4" /> : <Copy className="size-4" />}
                                    {isPublicLinkCopied ? 'Copied' : 'Copy link'}
                                </Button>

                                <Link
                                    to={`/events/${event.id}`}
                                    className="inline-flex items-center justify-center gap-2 rounded-lg border border-white/10 bg-white/5 px-4 py-2 text-sm font-semibold text-zinc-200 hover:bg-white/10"
                                >
                                    <ExternalLink className="size-4" />
                                    Public page
                                </Link>
                            </div>
                        </div>

                        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                            <MetricCard
                                icon={<Ticket className="size-5" />}
                                label="Total seats"
                                value={event.maximumAttendees}
                            />
                            <MetricCard
                                icon={<Users className="size-5" />}
                                label="Registered"
                                value={event.numberOfAttendees}
                                detail={`${occupancyRate}% occupied`}
                            />
                            <MetricCard
                                icon={<UserCheck className="size-5" />}
                                label="Check-ins"
                                value={event.numberOfCheckIns}
                                detail={`${checkInRate}% of attendees`}
                            />
                            <MetricCard
                                icon={<CheckCircle2 className="size-5" />}
                                label="Remaining seats"
                                value={remainingSeats}
                                detail={isEventFull ? 'Event full' : 'Available'}
                                tone={isEventFull ? 'warning' : 'default'}
                            />
                        </div>

                        <div className="grid gap-4 border border-white/10 rounded-lg p-4 bg-white/[0.02] md:grid-cols-2">
                            <ProgressMetric label="Occupancy" value={occupancyRate} />
                            <ProgressMetric label="Check-in" value={checkInRate} />
                        </div>
                    </section>
                </div>
            )}

            <AttendeeList eventId={eventId} authenticatedRequest={authenticatedRequest} />
        </main>
    )
}

type MetricCardProps = {
    icon: ReactNode
    label: string
    value: number
    detail?: string
    tone?: 'default' | 'warning'
}

function MetricCard({ icon, label, value, detail, tone = 'default' }: MetricCardProps) {
    const iconColor = tone === 'warning' ? 'text-amber-300' : 'text-emerald-300'

    return (
        <div className="border border-white/10 rounded-lg p-4 bg-white/[0.02]">
            <div className={`mb-3 ${iconColor}`}>{icon}</div>
            <span className="text-xs uppercase text-zinc-500">{label}</span>
            <p className="text-2xl font-semibold text-white">{value}</p>
            {detail && <span className="text-xs text-zinc-400">{detail}</span>}
        </div>
    )
}

type ProgressMetricProps = {
    label: string
    value: number
}

function ProgressMetric({ label, value }: ProgressMetricProps) {
    return (
        <div className="flex flex-col gap-2">
            <div className="flex items-center justify-between text-sm">
                <span className="font-medium text-zinc-200">{label}</span>
                <span className="text-zinc-400">{value}%</span>
            </div>
            <div className="h-2 overflow-hidden rounded-full bg-white/10">
                <div className="h-full rounded-full bg-emerald-400" style={{ width: `${value}%` }} />
            </div>
        </div>
    )
}
