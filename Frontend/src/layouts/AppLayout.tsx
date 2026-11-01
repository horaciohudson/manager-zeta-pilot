import { Outlet } from "react-router-dom"

export function AppLayout() {
	return (
		<div className="app-shell">
			<header className="app-header">
				<a className="brand" href="/" aria-label="ZetaPilotAI Manager home">
					<span className="brand-mark" aria-hidden="true">Z</span>
					<span>ZetaPilotAI <strong>Manager</strong></span>
				</a>
				<span className="environment-badge">Account portal</span>
			</header>
			<main className="app-main"><Outlet /></main>
		</div>
	)
}