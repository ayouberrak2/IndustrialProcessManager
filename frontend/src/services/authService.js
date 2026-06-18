// backend on port 8090
const API_BASE_URL = 'http://localhost:8090/api'

export async function login(username, password) {
  let response

  try {
    response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    })
  } catch {
    throw new Error("Impossible de joindre l'API. Vérifiez que le backend est démarré.")
  }

  const data = await response.json().catch(() => ({}))

  if (!response.ok) {
    throw new Error(data.message || 'La connexion a échoué')
  }

  return data
}
