// backend on port 8090
const API_BASE_URL = 'http://localhost:8090/api/admin'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
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

export function getDashboardStats() {
  return request('/dashboard')
}

export function getUsers() {
  return request('/users')
}

export function createUser(user) {
  return request('/users', {
    method: 'POST',
    body: JSON.stringify(user),
  })
}

// export function viewUser(id){
//   return request(`/users/${id}`,{
//     method:'GET'
//   })
// }

export function updateUser(id, user) {
  return request(`/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(user),
  })
}

export function deleteUser(id) {
  return request(`/users/${id}`, {
    method: 'DELETE',
  })
}

export function getAteliers() {
  return request('/ateliers')
}

export function createAtelier(atelier) {
  return request('/ateliers', {
    method: 'POST',
    body: JSON.stringify(atelier),
  })
}

export function updateAtelier(id, atelier) {
  return request(`/ateliers/${id}`, {
    method: 'PUT',
    body: JSON.stringify(atelier),
  })
}

export function deleteAtelier(id) {
  return request(`/ateliers/${id}`, {
    method: 'DELETE',
  })
}
