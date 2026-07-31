import { ApiRequestError } from './api'

export function getFriendlyErrorMessage(error: unknown, fallback: string) {
    if (!(error instanceof Error)) {
        return fallback
    }

    if (error.message === 'Failed to fetch') {
        return 'Backend unavailable. Check if the API is running.'
    }

    const knownErrors: Record<string, string> = {
        'Event is full': 'Event has no available seats.',
        'Attendee is already subscribed': 'This email is already registered for this event.',
        'This attendee is already checked in': 'Check-in has already been completed for this attendee.',
        'Invalid check-in token': 'Invalid check-in token.',
        'Expired check-in token': 'Expired check-in token.',
        'Request validation failed': 'Check the fields and try again.',
        'Malformed request body': 'Invalid request.',
    }

    const message = knownErrors[error.message] ?? error.message ?? fallback

    if (error instanceof ApiRequestError && error.fields?.length)
        return `${message}: ${error.fields.map((field) => translateFieldError(field.message)).join('; ')}`

    return message
}

function translateFieldError(message: string) {
    const knownFieldErrors: Record<string, string> = {
        'Name is required': 'name is required',
        'Name must have at most 255 characters': 'name must have at most 255 characters',
        'Email is required': 'email is required',
        'Email must be valid': 'email must be valid',
        'Email must have at most 255 characters': 'email must have at most 255 characters',
        'Password is required': 'password is required',
        'Password must have between 8 and 72 characters': 'password must have between 8 and 72 characters',
        'Title is required': 'title is required',
        'Title must have at most 255 characters': 'title must have at most 255 characters',
        'Details are required': 'details are required',
        'Details must have at most 255 characters': 'details must have at most 255 characters',
        'Maximum attendees is required': 'maximum attendees is required',
        'Maximum attendees must be greater than zero': 'maximum attendees must be greater than zero',
    }

    return knownFieldErrors[message] ?? message
}
