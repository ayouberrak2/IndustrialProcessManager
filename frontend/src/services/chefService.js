// Point directly to backend server (backend écoute sur le port 8090)
const API_BASE_URL = 'http://localhost:8090/api/chef'
const ADMIN_API_BASE_URL = 'http://localhost:8090/api/admin'

async function request(path) {
  const response = await fetch(`${API_BASE_URL}${path}`)
  const data = await response.json().catch(() => ({}))

  if (!response.ok) {
    throw new Error(data.message || 'Operation impossible')
  }

  return data
}

async function adminRequest(path, options = {}) {
  const response = await fetch(`${ADMIN_API_BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
  })

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

export async function getTechniciensLabo(atelierId) {
  const users = await adminRequest('/users')
  const safeAtelierId = Number(atelierId || 0)

  return users.filter((user) => {
    const isTechnicienLabo = user.role === 'TECHNICIEN_LABO'
    const isSameAtelier = safeAtelierId === 0 || Number(user.atelierId) === safeAtelierId

    return isTechnicienLabo && isSameAtelier
  })
}

export async function getAtelierByChef(chefId) {
  const ateliers = await adminRequest('/ateliers')
  const safeChefId = Number(chefId || 0)

  return ateliers.find((atelier) => Number(atelier.chefAtelierId) === safeChefId) || null
}

export function createTechnicienLabo(technicien) {
  return adminRequest('/users', {
    method: 'POST',
    body: JSON.stringify({
      username: technicien.username,
      password: technicien.password,
      role: 'TECHNICIEN_LABO',
      email: technicien.email,
      atelierId: technicien.atelierId,
    }),
  })
}

export function updateTechnicienLabo(id, technicien) {
  return adminRequest(`/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify({
      username: technicien.username,
      password: technicien.password,
      role: 'TECHNICIEN_LABO',
      email: technicien.email,
      atelierId: technicien.atelierId,
    }),
  })
}

export function deleteTechnicienLabo(id) {
  return adminRequest(`/users/${id}`, {
    method: 'DELETE',
  })
}

export function getOperations(atelierId) {
  const id = atelierId || 0
  return request(`/operations?atelierId=${id}`)
}

export function getOperationDetails(id) {
  return request(`/operations/${id}/details`)
}

export async function createOperation(operation) {
  const response = await fetch(`${API_BASE_URL}/operations`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(operation),
  })

  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.message || 'Impossible de creer operation')
  return data
}

export async function updateOperation(id, operation) {
  const response = await fetch(`${API_BASE_URL}/operations/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(operation),
  })

  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.message || 'Impossible de modifier operation')
  return data
}

export async function deleteOperation(id) {
  const response = await fetch(`${API_BASE_URL}/operations/${id}`, {
    method: 'DELETE',
  })

  if (!response.ok) {
    const data = await response.json().catch(() => ({}))
    throw new Error(data.message || 'Impossible de supprimer operation')
  }

  return { success: true }
}

export function getLots(atelierId) {
  const id = atelierId || 0
  return request(`/lots?atelierId=${id}`)
}

export function getArticles() {
  return request('/articles')
}

export async function createArticle(article) {
  const response = await fetch(`${API_BASE_URL}/articles`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(article),
  })

  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.message || 'Impossible de creer article')
  return data
}

export async function updateArticle(id, article) {
  const response = await fetch(`${API_BASE_URL}/articles/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(article),
  })

  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.message || 'Impossible de modifier article')
  return data
}

export async function deleteArticle(id) {
  const response = await fetch(`${API_BASE_URL}/articles/${id}`, {
    method: 'DELETE',
  })

  if (!response.ok) {
    const data = await response.json().catch(() => ({}))
    throw new Error(data.message || 'Impossible de supprimer article')
  }

  return { success: true }
}
