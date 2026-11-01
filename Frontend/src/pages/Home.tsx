import { AuthenticatedUser } from "../services/auth"

type HomeProps = {
	user: AuthenticatedUser
	onSignOut: () => void
	sessionError: string
}

export function Home({ user, onSignOut, sessionError }: HomeProps) {
	return (
		<div className="dashboard">
			<div className="dashboard-heading">
				<div>
					<p className="eyebrow">Management platform</p>
					<h1 id="welcome-title">Welcome, {user.fullName || user.email}.</h1>
				</div>
				<button className="secondary-button" onClick={onSignOut} type="button">Sign out</button>
			</div>
			<p className="welcome-copy">Your ZetaPilotAI Manager account is ready.</p>
			{sessionError && <p className="form-error" role="alert">{sessionError}</p>}
			<div className="account-card">
				<p className="card-label">Signed in as</p>
				<p className="account-email">{user.email}</p>
				<div className="role-list" aria-label="Account roles">
					{user.roles.map((role) => <span className="role-badge" key={role}>{role.replace("ROLE_", "")}</span>)}
				</div>
			</div>
		</div>
	)
}