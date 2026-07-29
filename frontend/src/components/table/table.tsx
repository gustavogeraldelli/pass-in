import { ComponentProps } from "react";

type TableProps = ComponentProps<'table'>

export function Table(props: TableProps) {
    return (
        <div className="border border-white/10 rounded-lg overflow-x-auto">
                <table className="w-full min-w-[760px]" {...props} />
        </div>
    )
}
