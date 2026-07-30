import { Search, Ticket, ChevronsLeft, ChevronLeft, ChevronRight, ChevronsRight } from 'lucide-react'
import { IconButton } from './icon-button'
import { Table } from './table/table'
import { TableHeader } from './table/table-header'
import { TableCell } from './table/table-cell'
import { TableRow } from './table/table-row'
import { ChangeEvent, useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import { Attendee, getEventAttendees } from '../lib/api'
import { Alert } from './alert'
import { getFriendlyErrorMessage } from '../lib/errors'

dayjs.extend(relativeTime)

const PAGE_SIZE = 10

interface AttendeeListProps {
    eventId: string
    authenticatedRequest: <T>(request: (accessToken: string) => Promise<T>) => Promise<T>
}

export function AttendeeList({ eventId, authenticatedRequest }: AttendeeListProps) {
    const [searchParams, setSearchParams] = useSearchParams()
    const [page, setPage] = useState(() => {
        return Number(searchParams.get('page') ?? 0)
    })
    const [query, setQuery] = useState(() => {
        return searchParams.get('query') ?? ''
    })
    const [attendees, setAttendees] = useState<Attendee[]>([])
    const [totalElements, setTotalElements] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const currentPageSize = attendees.length

    useEffect(() => {
        authenticatedRequest((accessToken) => getEventAttendees(eventId, page, PAGE_SIZE, query, accessToken))
            .then((data) => {
                setAttendees(data.attendees)
                setTotalElements(data.totalElements)
                setTotalPages(data.totalPages)
                setError(null)
            })
            .catch((error: Error) => {
                setAttendees([])
                setTotalElements(0)
                setTotalPages(0)
                setError(getFriendlyErrorMessage(error, 'Nao foi possivel carregar os participantes.'))
            })
            .finally(() => setIsLoading(false))
    }, [authenticatedRequest, eventId, page, query])

    function setCurrentPage(page: number) {
        const nextParams = new URLSearchParams()
        nextParams.set('page', String(page))
        if (query)
            nextParams.set('query', query)
        setSearchParams(nextParams)
        setIsLoading(true)
        setPage(page)
    }

    function onSearchInputChanged(event: ChangeEvent<HTMLInputElement>) {
        const value = event.target.value
        setQuery(value)

        const nextParams = new URLSearchParams()
        nextParams.set('page', '0')
        if (value)
            nextParams.set('query', value)

        setSearchParams(nextParams)
        setIsLoading(true)
        setPage(0)
    }

    function goToNextPage() {
        setCurrentPage(page + 1)
    }

    function goToPreviousPage() {
        setCurrentPage(page - 1)
    }

    function goToFirstPage() {
        setCurrentPage(0)
    }

    function goToLastPage() {
        setCurrentPage(totalPages - 1)
    }

    return (
        <div className='flex flex-col gap-4'>
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                <h1 className="text-2xl font-bold">Participantes</h1>
                <div className="w-full px-3 py-1.5 border border-white/10 rounded-lg flex items-center gap-3 sm:w-72">
                    <Search className="size-4 text-emerald-300" />
                    <input
                        className="bg-transparent flex-1 outline-none h-auto border-0 p-0 text-sm focus:ring-0"
                        placeholder="Buscar participante..."
                        value={query}
                        onChange={onSearchInputChanged}
                    />
                </div>
            </div>

            {error && (
                <Alert>
                    {error}
                </Alert>
            )}

            <Table>
                <thead>
                    <tr className='border-b border-white/10'>
                        <TableHeader style={{width: 48}}>
                            <input className='size-4 bg-black/20 rounded border border-white/10 ' type="checkbox" name="" id="" />
                        </TableHeader>
                        <TableHeader>Código</TableHeader>
                        <TableHeader>Participante</TableHeader>
                        <TableHeader>Data de inscrição</TableHeader>
                        <TableHeader>Data do check-in</TableHeader>
                        <TableHeader style={{width: 64}}></TableHeader>
                    </tr>
                </thead>
                <tbody>
                    {isLoading && (
                        <TableRow>
                            <TableCell colSpan={6} className="text-zinc-400">Carregando participantes...</TableCell>
                        </TableRow>
                    )}

                    {!isLoading && !error && attendees.length === 0 && (
                        <TableRow>
                            <TableCell colSpan={6} className="text-zinc-400">Nenhum participante encontrado.</TableCell>
                        </TableRow>
                    )}

                    {attendees.map((attendee) => {
                        return (
                            <TableRow key={attendee.id}>
                                <TableCell>
                                    <input className='size-4 bg-black/20 rounded border border-white/10 ' type="checkbox" name="" id="" />
                                </TableCell>
                                <TableCell>{attendee.id}</TableCell>
                                <TableCell>
                                    <div className='flex flex-col gap-1'>
                                        <span className='font-semibold text-white'>{attendee.name}</span>
                                        <span>{attendee.email}</span>
                                    </div>
                                </TableCell>
                                <TableCell>{dayjs().to(attendee.createdAt)}</TableCell>
                                <TableCell>{attendee.checkInAt === null ? '' : dayjs().to(attendee.checkInAt)}</TableCell>
                                <TableCell>
                                    <Link
                                        to={`/attendees/${attendee.id}/badge`}
                                        className="inline-flex border border-white/10 rounded-md p-1.5 bg-black/20"
                                        title="Abrir badge"
                                    >
                                        <Ticket className="size-4" />
                                    </Link>
                                </TableCell>
                            </TableRow>
                        )
                    })}
                </tbody>
                <tfoot>
                    <TableCell colSpan={3}>
                        Mostrando {currentPageSize} de {totalElements}
                    </TableCell>
                    <TableCell className='text-right' colSpan={3}>
                        <div className='inline-flex items-center gap-8'>
                            <span className="whitespace-nowrap">Página {totalPages === 0 ? 0 : page + 1} de {totalPages}</span>
                            
                            <div className='flex gap-1.5'>
                                <IconButton onClick={goToFirstPage} disabled={page === 0}>
                                    <ChevronsLeft className="size-4" />
                                </IconButton>
                                <IconButton onClick={goToPreviousPage} disabled={page === 0}>
                                    <ChevronLeft className="size-4" />
                                </IconButton>
                                <IconButton onClick={goToNextPage} disabled={totalPages === 0 || page >= totalPages - 1}>
                                    <ChevronRight className="size-4" />
                                </IconButton>
                                <IconButton onClick={goToLastPage} disabled={totalPages === 0 || page >= totalPages - 1}>
                                    <ChevronsRight className="size-4" />
                                </IconButton>
                            </div>
                        </div>
                    </TableCell>
                </tfoot>
            </Table>
        </div>
    )
}
