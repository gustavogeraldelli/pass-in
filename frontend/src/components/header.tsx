import icon from '../assets/icon.svg'
import { NavLink } from './nav-link'

export function Header() {
    return (
        <div className="flex items-center gap-5 py-2">
            <img src={icon} alt="logo" />
            <nav className="flex items-center gap-5">
                <NavLink to='/events'>Eventos</NavLink>
            </nav>
        </div>
    )
}
