import { useEffect, useState } from "react"
import { Navigate, Route, Routes } from "react-router-dom"
import { AppLayout } from "./layouts/AppLayout"
import { Home } from "./pages/Home"
import { SignIn } from "./pages/SignIn"
import { AuthenticatedUser, loadCurrentUser, signOut } from "./services/auth"

export default function App() {
	const [user, setUser] = useState<AuthenticatedUser | null>(null)
	const [isLoading, setIsLoading] = useState(true)
	const [sessionError, setSessionError] = useState("")

	useEffect(() => {
		let active = true
		loadCurrentUser()
			.then((currentUser) => { if (active) setUser(currentUser) })
			.catch(() => { if (active) setSessionError("Could not verify your session. Refresh the page to try again.") })
			.finally(() => { if (active) setIsLoading(false) })
		return () => { active = false }
	}, [])

	async function handleSignOut() {
		try {
			await signOut()
			setUser(null)
		} catch (cause) {
			setSessionError(cause instanceof Error ? cause.message : "Could not sign out. Please try again.")
		}
	}

	return (
		<Routes>
			<Route element={<AppLayout />}>
				<Route index element={isLoading
					? <p className="welcome-copy" role="status">Checking your session…</p>
					: user
						? <Home user={user} onSignOut={handleSignOut} sessionError={sessionError} />
						: <SignIn onSignedIn={(authenticatedUser) => { setSessionError(""); setUser(authenticatedUser) }} />}
				/>
				<Route path="*" element={<Navigate to="/" replace />} />
			</Route>
		</Routes>
	)
}