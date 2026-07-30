import { LogOut } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import icon from '../assets/icon.svg'
import { useAuth } from '../lib/use-auth'
import { Button } from './button'
import { NavLink } from './nav-link'

export function Header() {
    const navigate = useNavigate()
    const { isAuthenticated, logout } = useAuth()

    function handleLogout() {
        logout()
        navigate('/login', { replace: true })
    }

    return (
        <div className="flex items-center justify-between gap-5 py-2">
            <div className="flex items-center gap-5">
                <Link to={isAuthenticated ? '/events' : '/login'} aria-label="Pass.in">
                    <img src={icon} alt="logo" />
                </Link>
                <nav className="flex items-center gap-5">
                    {isAuthenticated && <NavLink to="/events">Eventos</NavLink>}
                    {!isAuthenticated && <NavLink to="/login">Entrar</NavLink>}
                    {!isAuthenticated && <NavLink to="/register">Cadastro</NavLink>}
                </nav>
            </div>

            <nav className="flex items-center gap-3">
                {isAuthenticated && (
                    <Button type="button" variant="secondary" onClick={handleLogout}>
                        <LogOut className="size-4" />
                        Sair
                    </Button>
                )}
            </nav>
        </div>
    )
}
