import { ArrowLeft, Check, Loader2, QrCode, Ticket, TriangleAlert } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { QRCodeSVG } from 'qrcode.react'
import { AttendeeBadge, checkInAttendee, getAttendeeBadge } from '../lib/api'

type CheckInStatus = 'idle' | 'loading' | 'success' | 'error'

export function AttendeeBadgePage() {
    const { attendeeId } = useParams()
    const [badge, setBadge] = useState<AttendeeBadge | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [checkInStatus, setCheckInStatus] = useState<CheckInStatus>('idle')
    const [checkInMessage, setCheckInMessage] = useState<string | null>(null)

    useEffect(() => {
        if (!attendeeId) {
            return
        }

        getAttendeeBadge(attendeeId)
            .then((data) => {
                setBadge(data)
                setError(null)
            })
            .catch((error: Error) => setError(error.message || 'Nao foi possivel carregar o badge.'))
            .finally(() => setIsLoading(false))
    }, [attendeeId])

    if (!attendeeId) {
        return <Navigate to="/events" replace />
    }

    function handleCheckIn() {
        setCheckInStatus('loading')
        setCheckInMessage(null)

        checkInAttendee(attendeeId!)
            .then(() => {
                setCheckInStatus('success')
                setCheckInMessage('Check-in realizado.')
            })
            .catch((error: Error) => {
                setCheckInStatus('error')
                setCheckInMessage(error.message || 'Nao foi possivel realizar o check-in.')
            })
    }

    return (
        <main className="flex flex-col gap-5">
            <Link to="/events" className="inline-flex items-center gap-2 text-sm text-zinc-400 hover:text-zinc-200">
                <ArrowLeft className="size-4" />
                Eventos
            </Link>

            {isLoading && (
                <div className="border border-white/10 rounded-lg p-4 text-sm text-zinc-400">
                    Carregando badge...
                </div>
            )}

            {error && (
                <div className="border border-red-400/30 bg-red-400/10 rounded-lg p-4 text-sm text-red-200">
                    {error}
                </div>
            )}

            {badge && (
                <div className="grid gap-5 lg:grid-cols-[1fr_320px]">
                    <section className="border border-white/10 rounded-lg p-5 flex flex-col gap-5 bg-white/[0.02]">
                        <div className="flex items-center gap-3">
                            <Ticket className="size-5 text-emerald-300" />
                            <h1 className="text-2xl font-bold">Badge do participante</h1>
                        </div>

                        <div className="grid gap-4 sm:grid-cols-2">
                            <div>
                                <span className="text-xs uppercase text-zinc-500">Nome</span>
                                <p className="font-medium text-white">{badge.name}</p>
                            </div>
                            <div>
                                <span className="text-xs uppercase text-zinc-500">Email</span>
                                <p className="font-medium text-white">{badge.email}</p>
                            </div>
                            <div>
                                <span className="text-xs uppercase text-zinc-500">Evento</span>
                                <p className="font-mono text-sm text-zinc-300">{badge.eventId}</p>
                            </div>
                        </div>

                        <div className="flex flex-wrap items-center gap-3">
                            <button
                                type="button"
                                onClick={handleCheckIn}
                                disabled={checkInStatus === 'loading' || checkInStatus === 'success'}
                                className="inline-flex items-center gap-2 rounded-lg bg-emerald-500 px-4 py-2 text-sm font-semibold text-black disabled:opacity-60"
                            >
                                {checkInStatus === 'loading' ? <Loader2 className="size-4 animate-spin" /> : <Check className="size-4" />}
                                Fazer check-in
                            </button>

                            {checkInMessage && (
                                <span className={checkInStatus === 'error' ? 'text-sm text-red-200' : 'text-sm text-emerald-300'}>
                                    {checkInStatus === 'error' && <TriangleAlert className="mr-1 inline size-4" />}
                                    {checkInMessage}
                                </span>
                            )}
                        </div>
                    </section>

                    <aside className="border border-white/10 rounded-lg p-5 flex flex-col items-center gap-4 bg-white/[0.02]">
                        <div className="flex items-center gap-2 text-sm text-zinc-400">
                            <QrCode className="size-4 text-emerald-300" />
                            QR Code de check-in
                        </div>
                        <div className="rounded-lg bg-white p-4">
                            <QRCodeSVG value={badge.checkInUrl} size={224} />
                        </div>
                    </aside>
                </div>
            )}
        </main>
    )
}
