export interface Event {
    id: string
    title: string
    details: string
    slug: string
    maximumAttendees: number
    numberOfAttendees: number
}

export interface EventListResponse {
    events: Event[]
}

export interface Attendee {
    id: string
    name: string
    email: string
    createdAt: string
    checkInAt: string | null
}

export interface AttendeeListResponse {
    attendees: Attendee[]
    page: number
    size: number
    totalElements: number
    totalPages: number
}

export interface AttendeeBadge {
    name: string
    email: string
    checkInUrl: string
    eventId: string
}

export interface ApiError {
    message: string
    fields?: Array<{
        field: string
        message: string
    }>
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${path}`, init)

    if (!response.ok) {
        const error = await readError(response)
        throw new Error(error.message || `Request failed with status ${response.status}`)
    }

    if (response.status === 204) {
        return undefined as T
    }

    const contentType = response.headers.get('content-type') ?? ''
    if (contentType.includes('application/json')) {
        return response.json()
    }

    return response.text() as Promise<T>
}

async function readError(response: Response): Promise<ApiError> {
    const contentType = response.headers.get('content-type') ?? ''
    if (!contentType.includes('application/json')) {
        return { message: await response.text() }
    }

    return response.json()
}

export function getEvents() {
    return request<EventListResponse>('/events')
}

export function getEvent(eventId: string) {
    return request<{ event: Event }>(`/events/${eventId}`)
}

export function getEventAttendees(eventId: string, page: number, size: number, query: string) {
    const searchParams = new URLSearchParams({
        page: String(page),
        size: String(size),
    })

    if (query) {
        searchParams.set('query', query)
    }

    return request<AttendeeListResponse>(`/events/${eventId}/attendees?${searchParams}`)
}

export function registerAttendee(eventId: string, attendee: { name: string, email: string }) {
    return request<string>(`/events/${eventId}/attendees`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(attendee),
    })
}

export function getAttendeeBadge(attendeeId: string) {
    return request<AttendeeBadge>(`/attendees/${attendeeId}/badge`)
}

export function checkInAttendee(attendeeId: string) {
    return request<void>(`/attendees/${attendeeId}/check-in`, {
        method: 'POST',
    })
}
