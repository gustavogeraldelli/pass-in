import { ArrowLeft, CheckCircle2, Loader2, Ticket, UserCheck, UserPlus, Users } from 'lucide-react'
import { FormEvent, useEffect, useState } from 'react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import { AttendeeList } from '../components/attendee-list'
import { Alert } from '../components/alert'
import { Button } from '../components/button'
import { EmptyState } from '../components/empty-state'
import { Input } from '../components/input'
import { Event, getEvent, registerAttendee } from '../lib/api'
import { getFriendlyErrorMessage } from '../lib/errors'

export function EventAttendeesPage() {
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
        if (!eventId) {
            return
        }

        getEvent(eventId)
            .then((data) => {
                setEvent(data.event)
                setError(null)
            })
            .catch((error: Error) => setError(getFriendlyErrorMessage(error, 'Nao foi possivel carregar o evento.')))
            .finally(() => setIsLoading(false))
    }, [eventId])

    if (!eventId) {
        return <Navigate to="/events" replace />
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setIsRegistering(true)
        setRegistrationError(null)

        registerAttendee(eventId!, { name, email })
            .then((attendeeId) => navigate(`/attendees/${attendeeId}/badge`))
            .catch((error: Error) => setRegistrationError(getFriendlyErrorMessage(error, 'Nao foi possivel realizar a inscricao.')))
            .finally(() => setIsRegistering(false))
    }

    const remainingSeats = event ? event.maximumAttendees - event.numberOfAttendees : 0

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
                            <div className="border border-white/10 rounded-lg p-4 bg-white/[0.02]">
                                <Ticket className="mb-3 size-5 text-emerald-300" />
                                <span className="text-xs uppercase text-zinc-500">Total de vagas</span>
                                <p className="text-2xl font-semibold text-white">{event.maximumAttendees}</p>
                            </div>
                            <div className="border border-white/10 rounded-lg p-4 bg-white/[0.02]">
                                <Users className="mb-3 size-5 text-emerald-300" />
                                <span className="text-xs uppercase text-zinc-500">Inscritos</span>
                                <p className="text-2xl font-semibold text-white">{event.numberOfAttendees}</p>
                            </div>
                            <div className="border border-white/10 rounded-lg p-4 bg-white/[0.02]">
                                <UserCheck className="mb-3 size-5 text-emerald-300" />
                                <span className="text-xs uppercase text-zinc-500">Check-ins</span>
                                <p className="text-2xl font-semibold text-white">{event.numberOfCheckIns}</p>
                            </div>
                            <div className="border border-white/10 rounded-lg p-4 bg-white/[0.02]">
                                <CheckCircle2 className="mb-3 size-5 text-emerald-300" />
                                <span className="text-xs uppercase text-zinc-500">Vagas restantes</span>
                                <p className="text-2xl font-semibold text-white">{remainingSeats}</p>
                            </div>
                        </div>
                    </section>

                    <form onSubmit={handleSubmit} className="border border-white/10 rounded-lg p-4 flex flex-col gap-3 bg-white/[0.02]">
                        <div className="flex items-center gap-2">
                            <UserPlus className="size-4 text-emerald-300" />
                            <h2 className="font-semibold text-white">Nova inscricao</h2>
                        </div>

                        <Input
                            placeholder="Nome"
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            required
                        />

                        <Input
                            placeholder="Email"
                            type="email"
                            value={email}
                            onChange={(event) => setEmail(event.target.value)}
                            required
                        />

                        {registrationError && (
                            <span className="text-sm text-red-200">{registrationError}</span>
                        )}

                        <Button
                            type="submit"
                            disabled={isRegistering}
                        >
                            {isRegistering ? <Loader2 className="size-4 animate-spin" /> : <UserPlus className="size-4" />}
                            Inscrever
                        </Button>
                    </form>
                </div>
            )}

            <AttendeeList eventId={eventId} />
        </main>
    )
}
