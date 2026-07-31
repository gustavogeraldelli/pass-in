import { Loader2, UserPlus } from 'lucide-react'
import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { Alert } from '../components/alert'
import { Button } from '../components/button'
import { Input } from '../components/input'
import { registerOrganizer } from '../lib/api'
import { getFriendlyErrorMessage } from '../lib/errors'
import { useAuth } from '../lib/use-auth'

export function RegisterPage() {
    const navigate = useNavigate()
    const { saveTokens } = useAuth()
    const [name, setName] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState<string | null>(null)
    const [isSubmitting, setIsSubmitting] = useState(false)

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setError(null)
        setIsSubmitting(true)

        registerOrganizer({ name, email, password })
            .then((tokens) => {
                saveTokens(tokens)
                navigate('/events', { replace: true })
            })
            .catch((error: Error) => setError(getFriendlyErrorMessage(error, 'Could not create your account.')))
            .finally(() => setIsSubmitting(false))
    }

    return (
        <main className="mx-auto flex w-full max-w-sm flex-col gap-4">
            <div className="flex flex-col gap-1">
                <h1 className="text-2xl font-bold">Create account</h1>
                <span className="text-sm text-zinc-400">Create your organizer account.</span>
            </div>

            <form onSubmit={handleSubmit} className="flex flex-col gap-3 rounded-lg border border-white/10 bg-white/[0.02] p-4">
                <Input
                    placeholder="Name"
                    value={name}
                    onChange={(event) => setName(event.target.value)}
                    required
                />

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
                    {isSubmitting ? <Loader2 className="size-4 animate-spin" /> : <UserPlus className="size-4" />}
                    Create account
                </Button>
            </form>

            <Link to="/login" className="text-sm text-zinc-400 hover:text-zinc-200">
                I already have an account
            </Link>
        </main>
    )
}
