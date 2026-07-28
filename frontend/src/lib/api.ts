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

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${path}`, init)

    if (!response.ok) {
        throw new Error(`Request failed with status ${response.status}`)
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
