// Point directly to backend server (backend écoute sur le port 8090)
const API_BASE_URL = 'http://localhost:8090/api/chef'

async function request(path) {
  const response = await fetch(`${API_BASE_URL}${path}`)
  const data = await response.json().catch(() => ({}))

  if (!response.ok) {
    throw new Error(data.message || 'Operation impossible')
  }

  return data
}

export function getChefDashboard(atelierId) {
  const safeAtelierId = atelierId || 0
  return request(`/dashboard?atelierId=${safeAtelierId}`)
}

export function getEquipements(atelierId) {
  const id = atelierId || 0
  return request(`/equipements?atelierId=${id}`)
}

export async function createEquipement(payload) {
  const response = await fetch(`${API_BASE_URL}/equipements`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })

  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.message || 'Impossible de creer equipement')
  return data
}

export async function updateEquipement(id, payload) {
  const response = await fetch(`${API_BASE_URL}/equipements/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })

  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.message || 'Impossible de modifier equipement')
  return data
}

export async function deleteEquipement(id) {
  const response = await fetch(`${API_BASE_URL}/equipements/${id}`, {
    method: 'DELETE',
  })

  if (!response.ok) {
    const data = await response.json().catch(() => ({}))
    throw new Error(data.message || 'Impossible de supprimer equipement')
  }

  return { success: true }
}
