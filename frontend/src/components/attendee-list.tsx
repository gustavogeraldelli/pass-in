import { Search, MoreHorizontal, ChevronsLeft, ChevronLeft, ChevronRight, ChevronsRight } from 'lucide-react'
import { IconButton } from './icon-button'
import { Table } from './table/table'
import { TableHeader } from './table/table-header'
import { TableCell } from './table/table-cell'
import { TableRow } from './table/table-row'
import { useEffect, useState } from 'react'
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

export function AttendeeList() {
    const [page, setPage] = useState(() => {
        const url = new URL(window.location.toString())
        if (url.searchParams.has('page'))
            return Number(url.searchParams.get('page'))
        return 1
    })
    const [attendees, setAttendees] = useState<Attendee[]>([])
    const totalPages = Math.ceil(attendees.length / 10);

    useEffect(() => {
        const url = new URL('http://localhost:8080/events/ae0246f4-77a3-4d64-a949-3b95b7ed1e32/attendees')

        // url.searchParams.set('query', '')

        fetch(url)
        .then(response => response.json())
        .then(data => {
            setAttendees(data.attendees)
        })
    }, [page])

    function setCurrentPage(page: number) {
        const url = new URL(window.location.toString())
        url.searchParams.set('page', String(page))
        window.history.pushState({}, '', url)
        setPage(page)
    }

    function goToNextPage() {
        setCurrentPage(page + 1)
    }

    function goToPreviousPage() {
        setCurrentPage(page - 1)
    }

    function goToFirstPage() {
        setCurrentPage(1)
    }

    function goToLastPage() {
        setCurrentPage(totalPages)
    }

    return (
        <div className='flex flex-col gap-4'>
            <div className="flex gap-3 items-center">
                <h1 className="text-2xl font-bold">Participantes</h1>
                <div className="w-72 px-3 py-1.5 border border-white/10 rounded-lg flex items-center gap-3">
                    <Search className="size-4 text-emerald-300" />
                    <input className="bg-transparent flex-1 outline-none h-auto border-0 p-0 text-sm focus:ring-0" placeholder="Buscar participante..." />
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
                    {attendees.slice((page - 1) * 10, page * 10).map((attendee) => {
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
                        Mostrando {page * 10 < attendees.length ? 10 : page*10 - attendees.length} de {attendees.length}
                    </TableCell>
                    <TableCell className='text-right' colSpan={3}>
                        <div className='inline-flex items-center gap-8'>
                            <span>Página {page} de {totalPages}</span>
                            
                            <div className='flex gap-1.5'>
                                <IconButton onClick={goToFirstPage} disabled={page === 1}>
                                    <ChevronsLeft className="size-4" />
                                </IconButton>
                                <IconButton onClick={goToPreviousPage} disabled={page === 1}>
                                    <ChevronLeft className="size-4" />
                                </IconButton>
                                <IconButton onClick={goToNextPage} disabled={page === totalPages}>
                                    <ChevronRight className="size-4" />
                                </IconButton>
                                <IconButton onClick={goToLastPage} disabled={page === totalPages}>
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