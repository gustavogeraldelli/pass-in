import { CalendarDays, ChevronRight, Users } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Event, getEvents } from '../lib/api'

export function EventsPage() {
    const [events, setEvents] = useState<Event[]>([])
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        getEvents()
            .then((data) => {
                setEvents(data.events)
                setError(null)
            })
            .catch(() => setError('Nao foi possivel carregar os eventos.'))
            .finally(() => setIsLoading(false))
    }, [])

    return (
        <main className="flex flex-col gap-4">
            <div className="flex items-center justify-between gap-4">
                <h1 className="text-2xl font-bold">Eventos</h1>
            </div>

            {isLoading && (
                <div className="border border-white/10 rounded-lg p-4 text-sm text-zinc-400">
                    Carregando eventos...
                </div>
            )}

            {error && (
                <div className="border border-red-400/30 bg-red-400/10 rounded-lg p-4 text-sm text-red-200">
                    {error}
                </div>
            )}

            {!isLoading && !error && events.length === 0 && (
                <div className="border border-white/10 rounded-lg p-4 text-sm text-zinc-400">
                    Nenhum evento cadastrado.
                </div>
            )}

            <div className="grid gap-3">
                {events.map((event) => {
                    const remainingSeats = event.maximumAttendees - event.numberOfAttendees

                    return (
                        <Link
                            key={event.id}
                            to={`/events/${event.id}`}
                            className="border border-white/10 rounded-lg p-4 flex items-center justify-between gap-4 bg-white/[0.02] hover:bg-white/[0.04]"
                        >
                            <div className="min-w-0 flex flex-col gap-2">
                                <div className="flex items-center gap-2 text-sm text-emerald-300">
                                    <CalendarDays className="size-4" />
                                    <span>{event.slug}</span>
                                </div>
                                <div>
                                    <h2 className="font-semibold text-white">{event.title}</h2>
                                    <p className="text-sm text-zinc-400 truncate">{event.details}</p>
                                </div>
                            </div>

                            <div className="flex items-center gap-6 text-sm text-zinc-300">
                                <div className="hidden sm:flex items-center gap-2">
                                    <Users className="size-4 text-emerald-300" />
                                    <span>{event.numberOfAttendees}/{event.maximumAttendees}</span>
                                </div>
                                <span className="hidden md:inline text-zinc-500">{remainingSeats} vagas restantes</span>
                                <ChevronRight className="size-5 text-zinc-500" />
                            </div>
                        </Link>
                    )
                })}
            </div>
        </main>
    )
}
