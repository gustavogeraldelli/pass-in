import { ArrowLeft, CheckCircle2, Ticket, UserCheck, Users } from 'lucide-react'
import { ReactNode, useEffect, useState } from 'react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { AttendeeList } from '../components/attendee-list'
import { Alert } from '../components/alert'
import { EmptyState } from '../components/empty-state'
import { Event, getEvent } from '../lib/api'
import { getFriendlyErrorMessage } from '../lib/errors'
import { useAuth } from '../lib/use-auth'

export function EventAttendeesPage() {
    const { eventId } = useParams()
    const { authenticatedRequest, isAuthenticated } = useAuth()
    const [event, setEvent] = useState<Event | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        if (!eventId)
            return

        getEvent(eventId)
            .then((data) => {
                setEvent(data.event)
                setError(null)
            })
            .catch((error: Error) => setError(getFriendlyErrorMessage(error, 'Nao foi possivel carregar o evento.')))
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

    return (
        <main className="flex flex-col gap-5">
            <Link to="/events" className="inline-flex items-center gap-2 text-sm text-zinc-400 hover:text-zinc-200">
                <ArrowLeft className="size-4" />
                Eventos
            </Link>

            {isLoading && (
                <EmptyState>
                    Carregando evento...
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

                            <Link
                                to={`/events/${event.id}`}
                                className="inline-flex items-center justify-center rounded-lg border border-white/10 bg-white/5 px-4 py-2 text-sm font-semibold text-zinc-200 hover:bg-white/10"
                            >
                                Pagina publica
                            </Link>
                        </div>

                        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                            <MetricCard
                                icon={<Ticket className="size-5" />}
                                label="Total de vagas"
                                value={event.maximumAttendees}
                            />
                            <MetricCard
                                icon={<Users className="size-5" />}
                                label="Inscritos"
                                value={event.numberOfAttendees}
                                detail={`${occupancyRate}% ocupado`}
                            />
                            <MetricCard
                                icon={<UserCheck className="size-5" />}
                                label="Check-ins"
                                value={event.numberOfCheckIns}
                                detail={`${checkInRate}% dos inscritos`}
                            />
                            <MetricCard
                                icon={<CheckCircle2 className="size-5" />}
                                label="Vagas restantes"
                                value={remainingSeats}
                                detail={isEventFull ? 'Evento lotado' : 'Disponivel'}
                                tone={isEventFull ? 'warning' : 'default'}
                            />
                        </div>

                        <div className="grid gap-4 border border-white/10 rounded-lg p-4 bg-white/[0.02] md:grid-cols-2">
                            <ProgressMetric label="Ocupacao" value={occupancyRate} />
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
