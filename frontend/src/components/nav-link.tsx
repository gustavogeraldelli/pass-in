import { NavLink as RouterNavLink, NavLinkProps } from "react-router"

interface AppNavLinkProps extends NavLinkProps {
    children: string
}

export function NavLink(props: AppNavLinkProps) {
    return (
        <RouterNavLink
            {...props}
            className={({ isActive }) => [
                'font-medium text-sm',
                isActive ? 'text-white' : 'text-zinc-400 hover:text-zinc-200',
            ].join(' ')}
        >
            {props.children}
        </RouterNavLink>
    )
}
