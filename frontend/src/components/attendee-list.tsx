import { Search, MoreHorizontal, ChevronsLeft, ChevronLeft, ChevronRight, ChevronsRight } from 'lucide-react'
import { IconButton } from './icon-button'
import { Table } from './table/table'
import { TableHeader } from './table/table-header'
import { TableCell } from './table/table-cell'
import { TableRow } from './table/table-row'
import { ChangeEvent, useEffect, useState } from 'react'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

interface Attendee {
    id: string,
    name: string,
    email: string,
    createdAt: string,  
    checkInAt: string | null
}

interface AttendeeListResponse {
    attendees: Attendee[],
    page: number,
    size: number,
    totalElements: number,
    totalPages: number
}

const EVENT_ID = 'ae0246f4-77a3-4d64-a949-3b95b7ed1e32'
const PAGE_SIZE = 10

export function AttendeeList() {
    const [page, setPage] = useState(() => {
        const url = new URL(window.location.toString())
        if (url.searchParams.has('page'))
            return Number(url.searchParams.get('page'))
        return 0
    })
    const [query, setQuery] = useState(() => {
        const url = new URL(window.location.toString())
        return url.searchParams.get('query') ?? ''
    })
    const [attendees, setAttendees] = useState<Attendee[]>([])
    const [totalElements, setTotalElements] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const currentPageSize = attendees.length

    useEffect(() => {
        const url = new URL(`http://localhost:8080/events/${EVENT_ID}/attendees`)

        url.searchParams.set('page', String(page))
        url.searchParams.set('size', String(PAGE_SIZE))

        if (query) {
            url.searchParams.set('query', query)
        }

        fetch(url)
        .then(response => response.json())
        .then((data: AttendeeListResponse) => {
            setAttendees(data.attendees)
            setTotalElements(data.totalElements)
            setTotalPages(data.totalPages)
        })
    }, [page, query])

    function setCurrentPage(page: number) {
        const url = new URL(window.location.toString())
        url.searchParams.set('page', String(page))
        if (query) {
            url.searchParams.set('query', query)
        } else {
            url.searchParams.delete('query')
        }
        window.history.pushState({}, '', url)
        setPage(page)
    }

    function onSearchInputChanged(event: ChangeEvent<HTMLInputElement>) {
        const value = event.target.value
        setQuery(value)

        const url = new URL(window.location.toString())
        url.searchParams.set('page', '0')
        if (value) {
            url.searchParams.set('query', value)
        } else {
            url.searchParams.delete('query')
        }

        window.history.pushState({}, '', url)
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
            <div className="flex gap-3 items-center">
                <h1 className="text-2xl font-bold">Participantes</h1>
                <div className="w-72 px-3 py-1.5 border border-white/10 rounded-lg flex items-center gap-3">
                    <Search className="size-4 text-emerald-300" />
                    <input
                        className="bg-transparent flex-1 outline-none h-auto border-0 p-0 text-sm focus:ring-0"
                        placeholder="Buscar participante..."
                        value={query}
                        onChange={onSearchInputChanged}
                    />
                </div>
            </div>

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
                                    <IconButton transparent>
                                        <MoreHorizontal className="size-4" />
                                    </IconButton>
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
                            <span>Página {totalPages === 0 ? 0 : page + 1} de {totalPages}</span>
                            
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
