export function getFriendlyErrorMessage(error: unknown, fallback: string) {
    if (!(error instanceof Error)) {
        return fallback
    }

    if (error.message === 'Failed to fetch') {
        return 'Backend indisponivel. Verifique se a API esta rodando.'
    }

    const knownErrors: Record<string, string> = {
        'Event is full': 'Evento sem vagas disponiveis.',
        'Attendee is already subscribed': 'Este email ja esta inscrito neste evento.',
        'This attendee is already checked in': 'Check-in ja realizado para este participante.',
        'Invalid check-in token': 'Token de check-in invalido.',
        'Expired check-in token': 'Token de check-in expirado.',
        'Request validation failed': 'Verifique os campos informados.',
        'Malformed request body': 'Requisicao invalida.',
    }

    return knownErrors[error.message] ?? error.message ?? fallback
}
