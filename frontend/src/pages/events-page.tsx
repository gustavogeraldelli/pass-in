import { CalendarDays, CheckCircle2, ChevronRight, Loader2, Plus, Ticket, Users } from 'lucide-react'
import { FormEvent, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { Alert } from '../components/alert'
import { Button } from '../components/button'
import { EmptyState } from '../components/empty-state'
import { Input } from '../components/input'
import { createEvent, Event, getOrganizerEvents } from '../lib/api'
import { getFriendlyErrorMessage } from '../lib/errors'
import { useAuth } from '../lib/use-auth'

export function EventsPage() {
    const navigate = useNavigate()
    const { authenticatedRequest } = useAuth()
    const [events, setEvents] = useState<Event[]>([])
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [title, setTitle] = useState('')
    const [details, setDetails] = useState('')
    const [maximumAttendees, setMaximumAttendees] = useState('')
    const [creationError, setCreationError] = useState<string | null>(null)
    const [isCreating, setIsCreating] = useState(false)

    useEffect(() => {
        authenticatedRequest(getOrganizerEvents)
            .then((data) => {
                setEvents(data.events)
                setError(null)
            })
            .catch((error: Error) => setError(getFriendlyErrorMessage(error, 'Could not load events.')))
            .finally(() => setIsLoading(false))
    }, [authenticatedRequest])

    function handleCreateEvent(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setIsCreating(true)
        setCreationError(null)

        authenticatedRequest((accessToken) => createEvent({
            title,
            details,
            maximumAttendees: Number(maximumAttendees),
        }, accessToken))
            .then((eventId) => navigate(`/events/${eventId}/attendees`))
            .catch((error: Error) => setCreationError(getFriendlyErrorMessage(error, 'Could not create the event.')))
            .finally(() => setIsCreating(false))
    }

    return (
        <main className="flex flex-col gap-4">
            <div className="flex items-center justify-between gap-4">
                <h1 className="text-2xl font-bold">Events</h1>
            </div>

            <form onSubmit={handleCreateEvent} className="border border-white/10 rounded-lg p-4 grid gap-3 bg-white/[0.02] lg:grid-cols-[1fr_1fr_180px_auto]">
                <Input
                    placeholder="Event title"
                    value={title}
                    onChange={(event) => setTitle(event.target.value)}
                    required
                />

                <Input
                    placeholder="Description"
                    value={details}
                    onChange={(event) => setDetails(event.target.value)}
                    required
                />

                <Input
                    placeholder="Seats"
                    type="number"
                    min={1}
                    value={maximumAttendees}
                    onChange={(event) => setMaximumAttendees(event.target.value)}
                    required
                />

                <Button type="submit" disabled={isCreating}>
                    {isCreating ? <Loader2 className="size-4 animate-spin" /> : <Plus className="size-4" />}
                    Create
                </Button>

                {creationError && (
                    <div className="lg:col-span-4">
                        <Alert>{creationError}</Alert>
                    </div>
                )}
            </form>

            {isLoading && (
                <EmptyState>
                    Loading events...
                </EmptyState>
            )}

            {error && (
                <Alert>
                    {error}
                </Alert>
            )}

            {!isLoading && !error && events.length === 0 && (
                <EmptyState>
                    No events registered. Create the first event using the form above.
                </EmptyState>
            )}

            <div className="grid gap-3">
                {events.map((event) => {
                    const remainingSeats = Math.max(event.maximumAttendees - event.numberOfAttendees, 0)
                    const occupancyRate = Math.min(Math.round((event.numberOfAttendees / event.maximumAttendees) * 100), 100)
                    const checkInRate = event.numberOfAttendees > 0
                        ? Math.min(Math.round((event.numberOfCheckIns / event.numberOfAttendees) * 100), 100)
                        : 0
                    const isEventFull = remainingSeats === 0

                    return (
                        <Link
                            key={event.id}
                            to={`/events/${event.id}/attendees`}
                            className="border border-white/10 rounded-lg p-4 grid gap-4 bg-white/[0.02] hover:bg-white/[0.04] lg:grid-cols-[1fr_auto]"
                        >
                            <div className="min-w-0 flex flex-col gap-2">
                                <div className="flex items-center gap-2 text-sm text-emerald-300">
                                    <CalendarDays className="size-4" />
                                    <span>{event.slug}</span>
                                    {isEventFull && (
                                        <span className="rounded-md border border-amber-400/20 bg-amber-400/10 px-2 py-0.5 text-xs text-amber-100">
                                            Full
                                        </span>
                                    )}
                                </div>
                                <div>
                                    <h2 className="font-semibold text-white">{event.title}</h2>
                                    <p className="text-sm text-zinc-400 truncate">{event.details}</p>
                                </div>
                            </div>

                            <div className="flex items-center justify-between gap-4 text-sm text-zinc-300 lg:justify-end">
                                <div className="flex items-center gap-5">
                                    <div className="flex items-center gap-2">
                                        <Users className="size-4 text-emerald-300" />
                                        <span>{event.numberOfAttendees}/{event.maximumAttendees}</span>
                                    </div>
                                    <div className="hidden sm:flex items-center gap-2">
                                        <CheckCircle2 className="size-4 text-emerald-300" />
                                        <span>{checkInRate}% check-in</span>
                                    </div>
                                </div>

                                <div className="hidden md:flex min-w-40 flex-col gap-1">
                                    <div className="flex items-center justify-between text-xs text-zinc-500">
                                        <span>{occupancyRate}% occupied</span>
                                        <span>{remainingSeats} free</span>
                                    </div>
                                    <div className="h-1.5 overflow-hidden rounded-full bg-white/10">
                                        <div className="h-full rounded-full bg-emerald-400" style={{ width: `${occupancyRate}%` }} />
                                    </div>
                                </div>

                                <div className="hidden sm:flex items-center gap-2">
                                    <Ticket className="size-4 text-emerald-300" />
                                    <span>{remainingSeats}</span>
                                </div>
                                <ChevronRight className="size-5 text-zinc-500" />
                            </div>
                        </Link>
                    )
                })}
            </div>
        </main>
    )
}
