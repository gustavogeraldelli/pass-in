import { Loader2, LogIn } from 'lucide-react'
import { FormEvent, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { Alert } from '../components/alert'
import { Button } from '../components/button'
import { Input } from '../components/input'
import { loginOrganizer } from '../lib/api'
import { getFriendlyErrorMessage } from '../lib/errors'
import { useAuth } from '../lib/use-auth'

type LocationState = {
    from?: string
}

export function LoginPage() {
    const navigate = useNavigate()
    const location = useLocation()
    const { saveTokens } = useAuth()
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState<string | null>(null)
    const [isSubmitting, setIsSubmitting] = useState(false)

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setError(null)
        setIsSubmitting(true)

        loginOrganizer({ email, password })
            .then((tokens) => {
                saveTokens(tokens)
                navigate((location.state as LocationState | null)?.from ?? '/events', { replace: true })
            })
            .catch((error: Error) => setError(getFriendlyErrorMessage(error, 'Could not sign in.')))
            .finally(() => setIsSubmitting(false))
    }

    return (
        <main className="mx-auto flex w-full max-w-sm flex-col gap-4">
            <div className="flex flex-col gap-1">
                <h1 className="text-2xl font-bold">Sign in</h1>
                <span className="text-sm text-zinc-400">Access your organizer area.</span>
            </div>

            <form onSubmit={handleSubmit} className="flex flex-col gap-3 rounded-lg border border-white/10 bg-white/[0.02] p-4">
                <Input
                    placeholder="Email"
                    type="email"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                    required
                />

                <Input
                    placeholder="Password"
                    type="password"
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    required
                />

                {error && (
                    <Alert>
                        {error}
                    </Alert>
                )}

                <Button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? <Loader2 className="size-4 animate-spin" /> : <LogIn className="size-4" />}
                    Sign in
                </Button>
            </form>

            <Link to="/register" className="text-sm text-zinc-400 hover:text-zinc-200">
                Create an account
            </Link>
        </main>
    )
}
