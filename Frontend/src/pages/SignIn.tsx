import { FormEvent, useState } from "react"
import { signIn, AuthenticatedUser } from "../services/auth"

type SignInProps = {
	onSignedIn: (user: AuthenticatedUser) => void
}

export function SignIn({ onSignedIn }: SignInProps) {
	const [email, setEmail] = useState("")
	const [password, setPassword] = useState("")
	const [error, setError] = useState("")
	const [isSubmitting, setIsSubmitting] = useState(false)

	async function handleSubmit(event: FormEvent<HTMLFormElement>) {
		event.preventDefault()
		setError("")
		setIsSubmitting(true)
		try {
			onSignedIn(await signIn(email, password))
		} catch (cause) {
			setError(cause instanceof Error ? cause.message : "Sign-in failed. Please try again.")
		} finally {
			setIsSubmitting(false)
		}
	}

	return (
		<section className="auth-card" aria-labelledby="sign-in-title">
			<p className="eyebrow">Account access</p>
			<h1 id="sign-in-title">Welcome back.</h1>
			<p className="welcome-copy">Sign in to manage your ZetaPilotAI account and services.</p>
			<form className="auth-form" onSubmit={handleSubmit}>
				<label htmlFor="email">Email address</label>
				<input
					autoComplete="username"
					autoFocus
					id="email"
					name="email"
					onChange={(event) => setEmail(event.target.value)}
					required
					type="email"
					value={email}
				/>
				<label htmlFor="password">Password</label>
				<input
					autoComplete="current-password"
					id="password"
					name="password"
					onChange={(event) => setPassword(event.target.value)}
					required
					type="password"
					value={password}
				/>
				{error && <p className="form-error" role="alert">{error}</p>}
				<button className="primary-button" disabled={isSubmitting} type="submit">
					{isSubmitting ? "Signing in…" : "Sign in"}
				</button>
			</form>
			<p className="auth-footnote">New accounts are currently provisioned by an administrator.</p>
		</section>
	)
}