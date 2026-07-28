import { ArrowLeft, Users } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { AttendeeList } from '../components/attendee-list'
import { Event, getEvent } from '../lib/api'

export function EventAttendeesPage() {
    const { eventId } = useParams()
    const [event, setEvent] = useState<Event | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

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
            )}

            <AttendeeList eventId={eventId} />
        </main>
    )
}
