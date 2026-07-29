import { ArrowLeft, Loader2, UserPlus, Users } from 'lucide-react'
import { FormEvent, useEffect, useState } from 'react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import { AttendeeList } from '../components/attendee-list'
import { Event, getEvent, registerAttendee } from '../lib/api'

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
            .catch(() => setError('Nao foi possivel carregar o evento.'))
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
            .catch((error: Error) => setRegistrationError(error.message || 'Nao foi possivel realizar a inscricao.'))
            .finally(() => setIsRegistering(false))
    }

    return (
        <main className="flex flex-col gap-5">
            <Link to="/events" className="inline-flex items-center gap-2 text-sm text-zinc-400 hover:text-zinc-200">
                <ArrowLeft className="size-4" />
                Eventos
            </Link>

            {isLoading && (
                <div className="border border-white/10 rounded-lg p-4 text-sm text-zinc-400">
                    Carregando evento...
                </div>
            )}

            {error && (
                <div className="border border-red-400/30 bg-red-400/10 rounded-lg p-4 text-sm text-red-200">
                    {error}
                </div>
            )}

            {event && (
                <div className="grid gap-5 lg:grid-cols-[1fr_360px]">
                    <section className="flex flex-col gap-2">
                        <div className="flex items-center gap-3">
                            <h1 className="text-2xl font-bold">{event.title}</h1>
                            <div className="inline-flex items-center gap-2 text-sm text-zinc-400">
                                <Users className="size-4 text-emerald-300" />
                                <span>{event.numberOfAttendees}/{event.maximumAttendees}</span>
                            </div>
                        </div>
                        <p className="text-sm text-zinc-400">{event.details}</p>
                    </section>

                    <form onSubmit={handleSubmit} className="border border-white/10 rounded-lg p-4 flex flex-col gap-3 bg-white/[0.02]">
                        <div className="flex items-center gap-2">
                            <UserPlus className="size-4 text-emerald-300" />
                            <h2 className="font-semibold text-white">Nova inscricao</h2>
                        </div>

                        <input
                            className="w-full rounded-lg border border-white/10 bg-black/20 px-3 py-2 text-sm outline-none focus:border-emerald-300"
                            placeholder="Nome"
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            required
                        />

                        <input
                            className="w-full rounded-lg border border-white/10 bg-black/20 px-3 py-2 text-sm outline-none focus:border-emerald-300"
                            placeholder="Email"
                            type="email"
                            value={email}
                            onChange={(event) => setEmail(event.target.value)}
                            required
                        />

                        {registrationError && (
                            <span className="text-sm text-red-200">{registrationError}</span>
                        )}

                        <button
                            type="submit"
                            disabled={isRegistering}
                            className="inline-flex items-center justify-center gap-2 rounded-lg bg-emerald-500 px-4 py-2 text-sm font-semibold text-black disabled:opacity-60"
                        >
                            {isRegistering ? <Loader2 className="size-4 animate-spin" /> : <UserPlus className="size-4" />}
                            Inscrever
                        </button>
                    </form>
                </div>
            )}

            <AttendeeList eventId={eventId} />
        </main>
    )
}
