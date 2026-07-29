import { ComponentProps } from 'react'
import { twMerge } from 'tailwind-merge'

type InputProps = ComponentProps<'input'>

export function Input(props: InputProps) {
    return (
        <input
            {...props}
            className={twMerge(
                'w-full rounded-lg border border-white/10 bg-black/20 px-3 py-2 text-sm outline-none focus:border-emerald-300',
                props.className,
            )}
        />
    )
}
