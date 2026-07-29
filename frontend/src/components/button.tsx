import { ComponentProps } from 'react'
import { twMerge } from 'tailwind-merge'

interface ButtonProps extends ComponentProps<'button'> {
    variant?: 'primary' | 'secondary'
}

export function Button({ variant = 'primary', ...props }: ButtonProps) {
    return (
        <button
            {...props}
            className={twMerge(
                'inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2 text-sm font-semibold disabled:opacity-60',
                variant === 'primary'
                    ? 'bg-emerald-500 text-black'
                    : 'border border-white/10 bg-white/5 text-zinc-200 hover:bg-white/10',
                props.className,
            )}
        />
    )
}
