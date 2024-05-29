import { Search, MoreHorizontal, ChevronsLeft, ChevronLeft, ChevronRight, ChevronsRight } from 'lucide-react'
import { IconButton } from './icon-button'
import { Table } from './table/table'
import { TableHeader } from './table/table-header'
import { TableCell } from './table/table-cell'
import { TableRow } from './table/table-row'
import { useState } from 'react'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

export function AttendeeList() {
    let attendees = Array.from({length: 50}).map(() => {
        return {
            id: 123123,
            name: 'Foo Bar',
            email: 'email@test.com',
            createdAt: Date.parse("2024-05-20"),
            checkedInAt: new Date()
        }
    })

    const [page, setPage] = useState(1)

    const totalPages = Math.ceil(attendees.length / 10);

    function goToNextPage() {
        setPage(page + 1)
    }

    function goToPreviousPage() {
        setPage(page - 1)
    }

    function goToFirstPage() {
        setPage(1)
    }

    function goToLastPage() {
        setPage(totalPages)
    }

    return (
        <div className='flex flex-col gap-4'>
            <div className="flex gap-3 items-center">
                <h1 className="text-2xl font-bold">Participantes</h1>
                <div className="w-72 px-3 py-1.5 border border-white/10 rounded-lg flex items-center gap-3">
                    <Search className="size-4 text-emerald-300" />
                    <input className="bg-transparent flex-1 outline-none h-auto border-0 p-0 text-sm" placeholder="Buscar participante..." />
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
                                <TableCell>{dayjs().to(attendee.checkedInAt)}</TableCell>
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
                        Mostrando 10 de {attendees.length}
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