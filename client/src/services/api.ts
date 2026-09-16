const API_URL = '/api'

export interface CreateAlunoRequest {
  name: string
  email: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

export type Plano = 'BASICO' | 'PREMIUM'

export type CursoStatus = 'INICIADO' | 'CONCLUIDO' | 'REPROVADO'

export interface AlunoResponseDTO {
  id: string
  name: string
  ra: string | null
  plano: Plano | null
  moedas: number | null
  cursosConcluidos: number | null
}

export interface MatriculaResponseDTO {
  id: string
  nomeCurso: string
  status: CursoStatus
  notaFinal: number | null
}

export interface VoucherResponseDTO {
  id: string
  nome: string
  valorEmReais: number
  descricao: string
  status: 'VALIDO' | 'UTILIZADO' | 'EXPIRADO'
  expiresAt: string | null
}

export interface ConcluirCursoResponseDTO {
  matriculaId: string
  nomeCurso: string
  status: CursoStatus
  plano: Plano
  moedas: number
  cursosLiberados: MatriculaResponseDTO[]
  voucher: VoucherResponseDTO | null
  upgradePremium: boolean
  moedasRecebidas: number
}

async function parseError(response: Response, fallback: string) {
  try {
    const body = await response.json()
    return body.message || body.error || fallback
  } catch {
    return fallback
  }
}

async function request<T>(path: string, init: RequestInit, fallbackError: string): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, init)
  if (!response.ok) throw new Error(await parseError(response, fallbackError))
  return response.json()
}

function jsonInit(method: string, body: unknown): RequestInit {
  return {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  }
}

export async function checkHealth(): Promise<{ message: string }> {
  return request<{ message: string }>('/health', {}, 'API indisponível')
}

export async function createAluno(data: CreateAlunoRequest): Promise<AlunoResponseDTO> {
  return request<AlunoResponseDTO>('/alunos', jsonInit('POST', data), 'Não foi possível criar o aluno.')
}

export async function login(data: LoginRequest): Promise<AlunoResponseDTO> {
  return request<AlunoResponseDTO>('/login', jsonInit('POST', data), 'Credenciais inválidas.')
}

export async function getAlunoById(id: string): Promise<AlunoResponseDTO> {
  return request<AlunoResponseDTO>(`/alunos/${encodeURIComponent(id)}`, {}, 'Aluno não encontrado.')
}

export async function getAlunoByRA(ra: string): Promise<AlunoResponseDTO> {
  return request<AlunoResponseDTO>(`/alunos/ra/${encodeURIComponent(ra)}`, {}, 'RA não encontrado.')
}

export async function listarMatriculas(alunoId: string): Promise<MatriculaResponseDTO[]> {
  return request<MatriculaResponseDTO[]>(`/alunos/${encodeURIComponent(alunoId)}/matriculas`, {}, 'Não foi possível carregar as matrículas.')
}

export async function getVouchers(alunoId: string): Promise<VoucherResponseDTO[]> {
  return request<VoucherResponseDTO[]>(`/alunos/${encodeURIComponent(alunoId)}/vouchers`, {}, 'Não foi possível carregar os vouchers.')
}

export async function matricular(alunoId: string, nomeCurso: string): Promise<MatriculaResponseDTO> {
  return request<MatriculaResponseDTO>(
    `/alunos/${encodeURIComponent(alunoId)}/matriculas`,
    jsonInit('POST', { nomeCurso }),
    'Não foi possível criar a matrícula.',
  )
}

export async function concluirCurso(alunoId: string, matriculaId: string, notaFinal: number): Promise<ConcluirCursoResponseDTO> {
  return request<ConcluirCursoResponseDTO>(
    `/alunos/${encodeURIComponent(alunoId)}/matriculas/${encodeURIComponent(matriculaId)}/conclusao`,
    jsonInit('PATCH', { notaFinal }),
    'Não foi possível concluir o curso.',
  )
}

export async function desistirMatricula(alunoId: string, matriculaId: string): Promise<void> {
  const response = await fetch(`${API_URL}/alunos/${encodeURIComponent(alunoId)}/matriculas/${encodeURIComponent(matriculaId)}`, {
    method: 'DELETE',
  })
  if (!response.ok) throw new Error(await parseError(response, 'Não foi possível remover a matrícula.'))
}
