export interface Event {
    id: string
    title: string
    details: string
    slug: string
    maximumAttendees: number
    numberOfAttendees: number
    numberOfCheckIns: number
}

export interface EventListResponse {
    events: Event[]
}

export interface EventRequest {
    title: string
    details: string
    maximumAttendees: number
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
    checkInToken: string
    eventId: string
}

export interface ApiError {
    message: string
    fields?: Array<{
        field: string
        message: string
    }>
}

export interface AuthTokens {
    accessToken: string
    refreshToken: string
    tokenType: string
    expiresInSeconds: number
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

async function request<T>(path: string, init?: RequestInit, accessToken?: string): Promise<T> {
    const headers = new Headers(init?.headers)
    if (accessToken)
        headers.set('Authorization', `Bearer ${accessToken}`)

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...init,
        headers,
    })

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

export function getOrganizerEvents(accessToken: string) {
    return request<EventListResponse>('/events', undefined, accessToken)
}

export function getEvent(eventId: string) {
    return request<{ event: Event }>(`/events/${eventId}`)
}

export function createEvent(event: EventRequest, accessToken: string) {
    return request<string>('/events', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(event),
    }, accessToken)
}

export function getEventAttendees(
    eventId: string,
    page: number,
    size: number,
    query: string,
    accessToken: string
) {
    const searchParams = new URLSearchParams({
        page: String(page),
        size: String(size),
    })

    if (query)
        searchParams.set('query', query)

    return request<AttendeeListResponse>(`/events/${eventId}/attendees?${searchParams}`, undefined, accessToken)
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

export function checkInAttendee(checkInToken: string) {
    return request<void>(`/check-ins/${checkInToken}`, {
        method: 'POST',
    })
}

export function registerOrganizer(organizer: { name: string, email: string, password: string }) {
    return request<AuthTokens>('/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(organizer),
    })
}

export function loginOrganizer(credentials: { email: string, password: string }) {
    return request<AuthTokens>('/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
    })
}
