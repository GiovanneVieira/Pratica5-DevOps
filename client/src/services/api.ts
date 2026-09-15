const API_URL = '/api';

export interface CreateAlunoRequest {
  name: string
  email: string
  password: string
}

export interface AlunoResponseDTO {
  id: string
  name: string
  ra: { valor: string } | null
  plano: 'BASICO' | 'PREMIUM' | null
  moedas?: number | null
}

async function parseError(response: Response, fallback: string) {
  try {
    const body = await response.json()
    return body.message || body.error || fallback
  } catch {
    return fallback
  }
}

export async function checkHealth(): Promise<{ message: string }> {
  const response = await fetch(`${API_URL}/health`)
  if (!response.ok) throw new Error(await parseError(response, 'API indisponível'))
  return response.json()
}

export async function createAluno(data: CreateAlunoRequest): Promise<AlunoResponseDTO> {
  const response = await fetch(`${API_URL}/alunos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })

  if (!response.ok) {
    throw new Error(await parseError(response, 'Não foi possível criar o aluno.'))
  }

  return response.json()
}

export async function getAlunoById(id: string): Promise<AlunoResponseDTO> {
  const response = await fetch(`${API_URL}/alunos/${encodeURIComponent(id)}`)
  if (!response.ok) throw new Error(await parseError(response, 'Aluno não encontrado.'))
  return response.json()
}

export async function getAlunoByRA(ra: string): Promise<AlunoResponseDTO> {
  const response = await fetch(`${API_URL}/alunos/${encodeURIComponent(ra)}`)
  if (!response.ok) throw new Error(await parseError(response, 'RA não encontrado.'))
  return response.json()
}
