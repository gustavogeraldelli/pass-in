import { CheckCircle2, Gauge, Loader2, Ticket, UserCheck, UserPlus, Users } from 'lucide-react'
import { FormEvent, ReactNode, useEffect, useState } from 'react'
import { Navigate, useNavigate, useParams } from 'react-router-dom'
import { Alert } from '../components/alert'
import { Button } from '../components/button'
import { EmptyState } from '../components/empty-state'
import { Input } from '../components/input'
import { Event, getEvent, registerAttendee } from '../lib/api'
import { getFriendlyErrorMessage } from '../lib/errors'

export function EventPage() {
    const { eventId } = useParams()
    const navigate = useNavigate()
    const [event, setEvent] = useState<Event | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [name, setName] = useState('')
    const [email, setEmail] = useState('')
    const [registrationError, setRegistrationError] = useState<string | null>(null)
    const [isRegistering, setIsRegistering] = useState(false)

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

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setIsRegistering(true)
        setRegistrationError(null)

        registerAttendee(eventId!, { name, email })
            .then((attendeeId) => navigate(`/attendees/${attendeeId}/badge`))
            .catch((error: Error) => setRegistrationError(getFriendlyErrorMessage(error, 'Nao foi possivel realizar a inscricao.')))
            .finally(() => setIsRegistering(false))
    }

    const remainingSeats = event ? Math.max(event.maximumAttendees - event.numberOfAttendees, 0) : 0
    const occupancyRate = event ? Math.min(Math.round((event.numberOfAttendees / event.maximumAttendees) * 100), 100) : 0
    const checkInRate = event && event.numberOfAttendees > 0
        ? Math.min(Math.round((event.numberOfCheckIns / event.numberOfAttendees) * 100), 100)
        : 0
    const isEventFull = event ? remainingSeats === 0 : false

    return (
        <main className="flex flex-col gap-5">
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
                <div className="grid gap-5 lg:grid-cols-[1fr_360px]">
                    <section className="flex flex-col gap-4">
                        <div className="flex items-center gap-3">
                            <h1 className="text-2xl font-bold">{event.title}</h1>
                            <div className="inline-flex items-center gap-2 text-sm text-zinc-400">
                                <Users className="size-4 text-emerald-300" />
                                <span>{event.numberOfAttendees}/{event.maximumAttendees}</span>
                            </div>
                        </div>
                        <p className="text-sm text-zinc-400">{event.details}</p>

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
                    </section>

                    <form onSubmit={handleSubmit} className="border border-white/10 rounded-lg p-4 flex flex-col gap-3 bg-white/[0.02]">
                        <div className="flex items-center gap-2">
                            <UserPlus className="size-4 text-emerald-300" />
                            <h2 className="font-semibold text-white">Nova inscricao</h2>
                        </div>

                        {isEventFull && (
                            <span className="inline-flex items-center gap-2 rounded-md border border-amber-400/20 bg-amber-400/10 px-3 py-2 text-sm text-amber-100">
                                <Gauge className="size-4" />
                                Evento lotado
                            </span>
                        )}

                        <Input
                            placeholder="Nome"
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            disabled={isEventFull}
                            required
                        />

                        <Input
                            placeholder="Email"
                            type="email"
                            value={email}
                            onChange={(event) => setEmail(event.target.value)}
                            disabled={isEventFull}
                            required
                        />

                        {registrationError && (
                            <span className="text-sm text-red-200">{registrationError}</span>
                        )}

                        <Button
                            type="submit"
                            disabled={isRegistering || isEventFull}
                        >
                            {isRegistering ? <Loader2 className="size-4 animate-spin" /> : <UserPlus className="size-4" />}
                            Inscrever
                        </Button>
                    </form>
                </div>
            )}
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
