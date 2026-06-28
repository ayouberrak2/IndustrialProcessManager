const LABO_API_BASE_URL = 'http://localhost:8090/api/labo'

async function request(path, options = {}) {
  const response = await fetch(`${LABO_API_BASE_URL}${path}`, {
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

export function getLaboOperations(atelierId) {
  const safeAtelierId = atelierId || 0
  return request(`/operations?atelierId=${safeAtelierId}`)
}

export function getLaboOperationDetails(operationId) {
  return request(`/operations/${operationId}/details`)
}

export function getLaboAnalyses(atelierId) {
  const safeAtelierId = atelierId || 0
  return request(`/analyses?atelierId=${safeAtelierId}`)
}

export function saveLaboAnalyse(analyse) {
  return request('/analyses', {
    method: 'POST',
    body: JSON.stringify(analyse),
  })
}
