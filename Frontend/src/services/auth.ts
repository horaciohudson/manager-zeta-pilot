export type AuthenticatedUser = {
	id: string
	email: string
	fullName: string | null
	roles: string[]
}

type ProblemResponse = {
	detail?: string
}

class ApiError extends Error {
	constructor(message: string, readonly status: number) {
		super(message)
	}
}

async function getCsrfToken(forceRefresh = false): Promise<string> {
	const response = await fetch("/api/auth/csrf", { credentials: "include" })
	if (!response.ok) throw new Error("Could not start a secure session. Please try again.")
	const body = (await response.json()) as { token: string }
	if (!forceRefresh) return body.token

	const cookieToken = document.cookie
		.split(";")
		.map((cookie) => cookie.trim())
		.find((cookie) => cookie.startsWith("XSRF-TOKEN="))
		?.slice("XSRF-TOKEN=".length)
	if (!cookieToken) return body.token
	try {
		return decodeURIComponent(cookieToken)
	} catch {
		return cookieToken
	}
}

async function requestUser(path: string, init: RequestInit = {}): Promise<AuthenticatedUser> {
	const response = await fetch(path, { ...init, credentials: "include" })
	if (!response.ok) {
		const problem = (await response.json().catch(() => ({}))) as ProblemResponse
		throw new ApiError(problem.detail ?? "The request could not be completed.", response.status)
	}
	return (await response.json()) as AuthenticatedUser
}

export async function signIn(email: string, password: string): Promise<AuthenticatedUser> {
	const submit = (csrfToken: string) => requestUser("/api/auth/login", {
		method: "POST",
		headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": csrfToken },
		body: JSON.stringify({ email, password }),
	})
	const csrfToken = await getCsrfToken()
	try {
		return await submit(csrfToken)
	} catch (error) {
		if (!(error instanceof ApiError) || error.status !== 403) throw error
		return submit(await getCsrfToken(true))
	}
}

export async function loadCurrentUser(): Promise<AuthenticatedUser | null> {
	const response = await fetch("/api/auth/me", { credentials: "include" })
	if (response.ok) return (await response.json()) as AuthenticatedUser
	if (response.status !== 401) throw new Error("Could not load your account. Please try again.")

	const csrfToken = await getCsrfToken()
	const refreshed = await fetch("/api/auth/refresh", {
		method: "POST",
		credentials: "include",
		headers: { "X-XSRF-TOKEN": csrfToken },
	})
	if (!refreshed.ok) return null
	return (await refreshed.json()) as AuthenticatedUser
}

export async function signOut(): Promise<void> {
	const csrfToken = await getCsrfToken()
	const response = await fetch("/api/auth/logout", {
		method: "POST",
		credentials: "include",
		headers: { "X-XSRF-TOKEN": csrfToken },
	})
	if (!response.ok) throw new Error("Could not sign out. Please try again.")
}