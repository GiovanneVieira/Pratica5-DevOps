import { type FormEvent, type ReactNode, useEffect, useState } from 'react'
import {
  createAluno, login, getAlunoById, listarMatriculas, getVouchers, matricular, concluirCurso, desistirMatricula,
  type AlunoResponseDTO, type MatriculaResponseDTO, type ConcluirCursoResponseDTO, type VoucherResponseDTO,
} from './services/api'
import './App.css'

type Page = 'home' | 'register' | 'login' | 'dashboard'

const ALUNO_ID_KEY = 'gamificacao-aluno-id'
const PAGE_KEY = 'gamificacao-page'

function Icon({ name }: { name: 'home' | 'book' | 'ticket' | 'user' | 'coin' | 'arrow' | 'check' | 'lock' | 'logout' | 'spark' | 'close' | 'alert' }) {
  const paths: Record<string, string> = {
    home: 'M3 10.5 12 3l9 7.5M5.5 9v10h13V9M9 19v-5h6v5',
    book: 'M4 5.5A2.5 2.5 0 0 1 6.5 3H20v16H6.5A2.5 2.5 0 0 0 4 21.5v-16ZM4 17.5A2.5 2.5 0 0 1 6.5 15H20',
    ticket: 'm20.5 12-2-2a2 2 0 0 0-2.8-2.8l-2-2a2 2 0 0 0-2.8 0l-2 2A2 2 0 0 0 6.1 10l-2 2a2 2 0 0 0 0 2.8l2 2A2 2 0 0 0 8.9 19l2 2a2 2 0 0 0 2.8 0l2-2a2 2 0 0 0 2.8-2.8l2-2a2 2 0 0 0 .2-2.2Z',
    user: 'M20 21a8 8 0 0 0-16 0M12 13a5 5 0 1 0 0-10 5 5 0 0 0 0 10Z',
    coin: 'M12 22c5 0 9-2.2 9-5V7c0 2.8-4 5-9 5S3 9.8 3 7v10c0 2.8 4 5 9 5ZM21 7c0 2.8-4 5-9 5S3 9.8 3 7s4-5 9-5 9 2.2 9 5Z',
    arrow: 'M5 12h14M13 6l6 6-6 6',
    check: 'm5 12 4 4L19 6',
    lock: 'M6 10V7a6 6 0 0 1 12 0v3M5 10h14v11H5z',
    logout: 'M10 17l5-5-5-5M15 12H3M21 19V5a2 2 0 0 0-2-2h-4',
    spark: 'm12 3 1.6 5.4L19 10l-5.4 1.6L12 17l-1.6-5.4L5 10l5.4-1.6L12 3Z',
    close: 'M6 6l12 12M18 6 6 18',
    alert: 'M12 4 2.8 20h18.4L12 4Zm0 5.5v4m0 3.1v.01',
  }
  return (
    <svg className="icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d={paths[name]} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function App() {
  const [page, setPage] = useState<Page>(() => localStorage.getItem(PAGE_KEY) as Page || 'home')
  const [aluno, setAluno] = useState<AlunoResponseDTO | null>(null)
  const [loadingAluno, setLoadingAluno] = useState(true)
  const [apiError, setApiError] = useState('')

  const reloadAluno = (id: string) =>
    getAlunoById(id)
      .then(setAluno)
      .catch(() => localStorage.removeItem(ALUNO_ID_KEY))

  useEffect(() => {
    const savedId = localStorage.getItem(ALUNO_ID_KEY)
    Promise.resolve(savedId ? reloadAluno(savedId) : null)
      .finally(() => setLoadingAluno(false))
  }, [])

  useEffect(() => {
    localStorage.setItem(PAGE_KEY, page)
  }, [page])

  const openRegister = () => {
    setApiError('')
    setPage('register')
  }

  const openLogin = () => {
    setApiError('')
    setPage('login')
  }

  const openDashboard = () => {
    const id = localStorage.getItem(ALUNO_ID_KEY)
    if (!id) {
      setPage('login')
      return
    }
    setPage('dashboard')
  }

  const handleRegister = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setApiError('')

    // Referencia capturada antes do await: event.currentTarget e anulado pelo
    // React apos o trecho sincrono do handler (causava TypeError no reset).
    const formEl = event.currentTarget
    const form = new FormData(formEl)
    const name = String(form.get('name') || '').trim()
    const email = String(form.get('email') || '').trim()
    const password = String(form.get('password') || '')

    if (password.length < 6) {
      setApiError('A senha precisa ter pelo menos 6 caracteres.')
      return
    }

    try {
      setLoadingAluno(true)
      const created = await createAluno({ name, email, password })
      setAluno(created)
      localStorage.setItem(ALUNO_ID_KEY, created.id)
      setPage('dashboard')
      formEl.reset()
    } catch (error) {
      setApiError(error instanceof Error ? error.message : 'Não foi possível criar sua conta.')
    } finally {
      setLoadingAluno(false)
    }
  }

  const handleLogin = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setApiError('')

    const formEl = event.currentTarget
    const form = new FormData(formEl)
    const email = String(form.get('email') || '').trim()
    const password = String(form.get('password') || '')

    try {
      setLoadingAluno(true)
      const logado = await login({ email, password })
      setAluno(logado)
      localStorage.setItem(ALUNO_ID_KEY, logado.id)
      setPage('dashboard')
      formEl.reset()
    } catch (error) {
      setApiError(error instanceof Error ? error.message : 'Não foi possível entrar.')
    } finally {
      setLoadingAluno(false)
    }
  }

  const logout = () => {
    localStorage.removeItem(ALUNO_ID_KEY)
    localStorage.removeItem(PAGE_KEY)
    setAluno(null)
    setPage('home')
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <button className="brand" onClick={() => setPage('home')} aria-label="Ir para o início">
          <span className="brand-mark"><Icon name="spark" /></span>
          <span>Level<span>Up</span></span>
        </button>

        <nav className="topnav" aria-label="Navegação principal">
          <button className={page === 'home' ? 'active' : ''} onClick={() => setPage('home')}>Início</button>
          {aluno && <button className={page === 'dashboard' ? 'active' : ''} onClick={openDashboard}>Dashboard</button>}
        </nav>

        {aluno ? (
          <button className="avatar-button" onClick={openDashboard} title="Abrir dashboard">
            <span>{aluno.name.charAt(0).toUpperCase()}</span>
          </button>
        ) : (
          <div className="header-auth">
            <button className="header-login" onClick={openLogin}>Entrar</button>
            <button className="header-register" onClick={openRegister}>Criar conta</button>
          </div>
        )}
      </header>

      {page === 'home' && (
        <main className="home-page">
          <section className="hero-section">
            <div className="hero-copy">
              <div className="eyebrow"><span className="pulse-dot" /> Aprenda. Conquiste. Evolua.</div>
              <h1>Transforme seus<br /><em>estudos</em> em conquistas.</h1>
              <p>Uma experiência de aprendizagem gamificada para acompanhar seu progresso, desbloquear novos desafios e tornar cada curso uma nova conquista.</p>
              <div className="hero-actions">
                <button className="primary-button" onClick={openRegister}>Começar agora <Icon name="arrow" /></button>
                {aluno && <button className="secondary-button" onClick={openDashboard}>Meu dashboard</button>}
              </div>
              <div className="trust-row">
                <div><strong>100%</strong><span>foco no aprendizado</span></div>
                <div><strong>XP</strong><span>por cada conquista</span></div>
                <div><strong>∞</strong><span>possibilidades</span></div>
              </div>
            </div>

            <div className="hero-visual" aria-hidden="true">
              <div className="glow" />
              <div className="floating-card card-top"><span className="mini-icon"><Icon name="check" /></span><div><b>Curso concluído!</b><small>+250 XP conquistados</small></div></div>
              <div className="profile-orb"><div className="orb-ring" /><span>LU</span></div>
              <div className="level-card"><span>NÍVEL ATUAL</span><strong>07</strong><div className="xp-bar"><i /></div><small>2.450 / 3.000 XP</small></div>
              <div className="floating-card card-bottom"><span className="coin-mini"><Icon name="coin" /></span><div><b>1.280 moedas</b><small>Saldo disponível</small></div></div>
            </div>
          </section>

          <section className="features-section">
            <div className="section-heading"><span>COMO FUNCIONA</span><h2>Seu progresso, do seu jeito.</h2></div>
            <div className="feature-grid">
              <Feature icon="book" number="01" title="Aprenda" text="Avance pelos seus cursos e acompanhe seu desenvolvimento." />
              <Feature icon="spark" number="02" title="Conquiste" text="Complete desafios e transforme dedicação em recompensas." />
              <Feature icon="ticket" number="03" title="Desbloqueie" text="Use suas conquistas para acessar novas possibilidades." />
            </div>
          </section>

          <section className="cta-section">
            <div><span className="eyebrow">PRONTO PARA COMEÇAR?</span><h2>Seu próximo nível<br />começa aqui.</h2></div>
            <button className="primary-button" onClick={openRegister}>Criar minha conta <Icon name="arrow" /></button>
          </section>
        </main>
      )}

      {page === 'register' && (
        <main className="register-page">
          <div className="register-intro">
            <span className="eyebrow"><Icon name="spark" /> PRIMEIRO PASSO</span>
            <h1>Comece sua<br /><em>jornada.</em></h1>
            <p>Crie sua conta e entre em uma experiência de estudos onde cada avanço conta.</p>
            <div className="benefit-list">
              <div><span><Icon name="check" /></span><p><b>Acompanhe seu progresso</b><small>Veja sua evolução em um só lugar.</small></p></div>
              <div><span><Icon name="check" /></span><p><b>Conquiste recompensas</b><small>Transforme seus estudos em conquistas.</small></p></div>
              <div><span><Icon name="check" /></span><p><b>Desbloqueie novos desafios</b><small>Continue evoluindo a cada etapa.</small></p></div>
            </div>
          </div>

          <form className="register-card" onSubmit={handleRegister}>
            <div className="card-heading"><span>CRIAR CONTA</span><h2>Vamos começar.</h2><p>Preencha seus dados para criar seu perfil.</p></div>
            <label>Nome completo<input name="name" type="text" placeholder="Como podemos te chamar?" required /></label>
            <label>E-mail<input name="email" type="email" placeholder="voce@email.com" required /></label>
            <label>Senha<input name="password" type="password" placeholder="Mínimo de 6 caracteres" minLength={6} required /></label>
            {apiError && <div className="error-box">{apiError}</div>}
            <button className="primary-button full" disabled={loadingAluno}>{loadingAluno ? 'Criando conta...' : 'Criar minha conta'} {!loadingAluno && <Icon name="arrow" />}</button>
            <p className="form-note"><Icon name="lock" /> Seus dados são enviados diretamente para a API.</p>
            <button type="button" className="back-link" onClick={openLogin}>Já tem conta? Entrar</button>
          </form>
        </main>
      )}

      {page === 'login' && (
        <main className="register-page">
          <div className="register-intro">
            <span className="eyebrow"><Icon name="lock" /> BEM-VINDO DE VOLTA</span>
            <h1>Entre na sua<br /><em>conta.</em></h1>
            <p>Acesse seu dashboard, acompanhe suas matrículas e continue de onde parou.</p>
            <div className="benefit-list">
              <div><span><Icon name="check" /></span><p><b>Seu progresso salvo</b><small>Cursos, recompensas e moedas em um só lugar.</small></p></div>
              <div><span><Icon name="check" /></span><p><b>Segurança com BCrypt</b><small>Sua senha é validada contra o hash armazenado.</small></p></div>
            </div>
          </div>

          <form className="register-card" onSubmit={handleLogin}>
            <div className="card-heading"><span>LOGIN</span><h2>Entrar.</h2><p>Use o e-mail e a senha do seu cadastro.</p></div>
            <label>E-mail<input name="email" type="email" placeholder="voce@email.com" required /></label>
            <label>Senha<input name="password" type="password" placeholder="Sua senha" required /></label>
            {apiError && <div className="error-box">{apiError}</div>}
            <button className="primary-button full" disabled={loadingAluno}>{loadingAluno ? 'Entrando...' : 'Entrar'} {!loadingAluno && <Icon name="arrow" />}</button>
            <p className="form-note"><Icon name="lock" /> As credenciais são validadas na API (BCrypt).</p>
            <button type="button" className="back-link" onClick={openRegister}>Não tem conta? Criar agora</button>
          </form>
        </main>
      )}

      {page === 'dashboard' && (
        <Dashboard
          aluno={aluno}
          loading={loadingAluno}
          onLogout={logout}
          onRegister={openRegister}
          onAlunoAtualizado={setAluno}
        />
      )}

      <footer><span>LEVELUP © 2026</span><span>Aprenda. Conquiste. Evolua.</span></footer>
    </div>
  )
}

function Feature({ icon, number, title, text }: { icon: 'book' | 'spark' | 'ticket', number: string, title: string, text: string }) {
  return <article className="feature-card"><div className="feature-icon"><Icon name={icon} /></div><span>{number}</span><h3>{title}</h3><p>{text}</p></article>
}

function Dashboard({ aluno, loading, onLogout, onRegister, onAlunoAtualizado }: {
  aluno: AlunoResponseDTO | null
  loading: boolean
  onLogout: () => void
  onRegister: () => void
  onAlunoAtualizado: (aluno: AlunoResponseDTO) => void
}) {
  const [matriculas, setMatriculas] = useState<MatriculaResponseDTO[]>([])
  const [carregandoMatriculas, setCarregandoMatriculas] = useState(true)
  const [matriculaError, setMatriculaError] = useState('')
  const [resultado, setResultado] = useState<ConcluirCursoResponseDTO | null>(null)
  const [recompensa, setRecompensa] = useState<ConcluirCursoResponseDTO | null>(null)
  const [confirmacao, setConfirmacao] = useState<MatriculaResponseDTO | null>(null)
  const [vouchers, setVouchers] = useState<VoucherResponseDTO[]>([])
  const [enviando, setEnviando] = useState(false)

  const alunoId = aluno?.id || localStorage.getItem(ALUNO_ID_KEY)

  const carregarDados = () => {
    if (!alunoId) return Promise.resolve()
    setCarregandoMatriculas(true)
    return Promise.all([listarMatriculas(alunoId), getAlunoById(alunoId), getVouchers(alunoId)])
      .then(([lista, alunoAtualizado, vouchersAtualizados]) => {
        setMatriculas(lista)
        onAlunoAtualizado(alunoAtualizado)
        setVouchers(vouchersAtualizados)
      })
      .catch((error) => setMatriculaError(error instanceof Error ? error.message : 'Erro ao carregar matrículas.'))
      .finally(() => setCarregandoMatriculas(false))
  }

  useEffect(() => {
    if (!alunoId) return
    let cancelado = false
    Promise.all([listarMatriculas(alunoId), getAlunoById(alunoId), getVouchers(alunoId)])
      .then(([lista, alunoAtualizado, vouchersAtualizados]) => {
        if (cancelado) return
        setMatriculas(lista)
        onAlunoAtualizado(alunoAtualizado)
        setVouchers(vouchersAtualizados)
      })
      .catch((error) => {
        if (!cancelado) setMatriculaError(error instanceof Error ? error.message : 'Erro ao carregar matrículas.')
      })
      .finally(() => {
        if (!cancelado) setCarregandoMatriculas(false)
      })
    return () => { cancelado = true }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [alunoId])

  if (!aluno && !loading) {
    return <main className="empty-dashboard"><div className="empty-icon"><Icon name="user" /></div><h1>Nenhum perfil encontrado.</h1><p>Faça login ou crie sua conta para acessar seu dashboard.</p><button className="primary-button" onClick={onRegister}>Criar conta <Icon name="arrow" /></button></main>
  }

  const handleMatricular = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setMatriculaError('')
    const formEl = event.currentTarget
    const form = new FormData(formEl)
    const nomeCurso = String(form.get('nomeCurso') || '').trim()
    if (!nomeCurso || !alunoId) return

    try {
      setEnviando(true)
      await matricular(alunoId, nomeCurso)
      formEl.reset()
      carregarDados()
    } catch (error) {
      setMatriculaError(error instanceof Error ? error.message : 'Não foi possível criar a matrícula.')
    } finally {
      setEnviando(false)
    }
  }

  const handleConcluir = async (matriculaId: string, notaTexto: string) => {
    setMatriculaError('')
    setResultado(null)
    setRecompensa(null)
    const notaFinal = Number(notaTexto)
    if (!alunoId || Number.isNaN(notaFinal) || notaFinal < 0 || notaFinal > 10) {
      setMatriculaError('Informe uma nota entre 0 e 10.')
      return
    }

    try {
      setEnviando(true)
      const resposta = await concluirCurso(alunoId, matriculaId, notaFinal)
      if (resposta.status === 'CONCLUIDO') {
        setRecompensa(resposta)
      } else {
        setResultado(resposta)
      }
      carregarDados()
    } catch (error) {
      setMatriculaError(error instanceof Error ? error.message : 'Não foi possível concluir o curso.')
    } finally {
      setEnviando(false)
    }
  }

  const handleDesistir = (matricula: MatriculaResponseDTO) => {
    setMatriculaError('')
    setConfirmacao(matricula)
  }

  const confirmarDesistir = async () => {
    if (!alunoId || !confirmacao) return
    const emAndamento = confirmacao.status === 'INICIADO'

    try {
      setEnviando(true)
      await desistirMatricula(alunoId, confirmacao.id)
      setConfirmacao(null)
      setResultado(null)
      carregarDados()
    } catch (error) {
      setMatriculaError(error instanceof Error
        ? error.message
        : `Não foi possível ${emAndamento ? 'desistir' : 'apagar'} o curso.`)
    } finally {
      setEnviando(false)
    }
  }

  const firstName = aluno?.name?.split(' ')[0] || 'aluno'
  const plan = aluno?.plano || 'BASICO'
  const ra = aluno?.ra || 'Não informado'
  // progresso oficial persistido no aluno: apagar curso do histórico não reduz
  const concluidos = aluno?.cursosConcluidos ?? 0
  const iniciados = matriculas.filter(m => m.status === 'INICIADO').length

  return <main className="dashboard-page">
    <section className="dashboard-head">
      <div><span className="eyebrow">SEU ESPAÇO</span><h1>Olá, {firstName}.</h1><p>Continue sua jornada. Cada passo conta.</p></div>
      <button className="logout-button" onClick={onLogout}><Icon name="logout" /> Sair</button>
    </section>

    <section className="stats-grid">
      <div className="stat-card accent"><span className="stat-icon"><Icon name="book" /></span><div><small>MATRÍCULAS</small><strong>{matriculas.length}</strong><span>{iniciados} em andamento</span></div></div>
      <div className="stat-card"><span className="stat-icon"><Icon name="coin" /></span><div><small>MOEDAS</small><strong>{(aluno?.moedas ?? 0).toLocaleString('pt-BR')}</strong><span>Saldo disponível</span></div></div>
      <div className="stat-card"><span className="stat-icon"><Icon name="check" /></span><div><small>CURSOS CONCLUÍDOS</small><strong>{concluidos}</strong><span>{12 - concluidos > 0 ? `${12 - concluidos} até o Premium` : 'Meta atingida'}</span></div></div>
      <div className="stat-card"><span className="stat-icon"><Icon name="ticket" /></span><div><small>PLANO</small><strong>{plan}</strong><span>{plan === 'PREMIUM' ? 'Acesso Premium' : 'Plano atual'}</span></div></div>
    </section>

    {resultado && (
      <section className="reward-banner failure">
        <span className="reward-icon"><Icon name="close" /></span>
        <div>
          <b>Curso "{resultado.nomeCurso}" reprovado (nota ≤ 7,0).</b>
          <small>Nenhum curso liberado e o progresso para o Premium não foi incrementado.</small>
        </div>
      </section>
    )}

    <section className="dashboard-columns">
      <div className="panel courses-panel">
        <div className="panel-head"><div><span>APRENDIZADO</span><h2>Minhas matrículas</h2></div><span className="live-label">DADOS DA API</span></div>

        <form className="matricula-form" onSubmit={handleMatricular}>
          <input name="nomeCurso" type="text" placeholder="Nome do curso (ex.: DevOps Essentials)" required />
          <button className="primary-button" disabled={enviando}>{enviando ? '...' : 'Matricular'}</button>
        </form>

        {matriculaError && <div className="error-box">{matriculaError}</div>}
        {carregandoMatriculas && <p className="loading-note">Carregando matrículas...</p>}

        <div className="course-list">
          {!carregandoMatriculas && matriculas.length === 0 && (
            <p className="loading-note">Nenhuma matrícula ainda. Cadastre seu primeiro curso acima.</p>
          )}
          {matriculas.map(matricula => (
            <div className="course-row" key={matricula.id}>
              <div className="course-icon"><Icon name={matricula.status === 'CONCLUIDO' ? 'check' : matricula.status === 'REPROVADO' ? 'close' : 'book'} /></div>
              <div className="course-info">
                <b>{matricula.nomeCurso}</b>
                <small>{matricula.status === 'INICIADO' ? 'Curso em andamento' : `Nota final: ${matricula.notaFinal?.toFixed(1) ?? '—'}`}</small>
                <div className="progress"><i style={{ width: matricula.status === 'CONCLUIDO' ? '100%' : matricula.status === 'REPROVADO' ? '35%' : '55%' }} /></div>
              </div>
              <div className="course-actions">
                {matricula.status === 'INICIADO' && (
                  <form className="concluir-form" onSubmit={(e) => { e.preventDefault(); const nota = String(new FormData(e.currentTarget).get('nota') || ''); handleConcluir(matricula.id, nota) }}>
                    <input name="nota" type="number" min={0} max={10} step={0.1} placeholder="Nota" required />
                    <button type="submit" disabled={enviando}>Concluir</button>
                  </form>
                )}
                <button
                  type="button"
                  className="desistir-button"
                  disabled={enviando}
                  onClick={() => handleDesistir(matricula)}
                >
                  {matricula.status === 'INICIADO' ? 'Desistir' : 'Apagar'}
                </button>
                <span className={`status-chip ${matricula.status.toLowerCase()}`}>{matricula.status}</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="panel profile-panel">
        <div className="panel-head"><div><span>PERFIL</span><h2>Seus dados</h2></div><span className="plan-badge">{plan}</span></div>
        <div className="profile-main"><div className="big-avatar">{aluno?.name?.charAt(0).toUpperCase() || 'A'}</div><div><h3>{aluno?.name || 'Carregando...'}</h3><p>Aluno LevelUp</p></div></div>
        <div className="data-row"><span>RA</span><b>{ra}</b></div>
        <div className="data-row"><span>Plano</span><b>{plan}</b></div>
        <div className="data-row"><span>Moedas</span><b>{aluno?.moedas ?? 0}</b></div>
        <div className="data-row"><span>ID</span><b className="id-value">{aluno?.id || '—'}</b></div>
      </div>
    </section>

    <section className="panel voucher-panel">
      <div className="panel-head"><div><span>RECOMPENSAS</span><h2>Seus vouchers</h2></div><span className="live-label">DADOS DA API</span></div>
      {vouchers.length === 0 ? (
        <p className="loading-note"><Icon name="ticket" /> Nenhum voucher ainda. Conclua o 12º curso com nota superior a 7,0 para virar Premium e receber vouchers para projetos reais.</p>
      ) : (
        <div className="voucher-grid">{vouchers.map(v => (
          <div className="voucher" key={v.id}>
            <span className="voucher-icon"><Icon name="ticket" /></span>
            <div><b>{v.nome}</b><small>R$ {v.valorEmReais.toFixed(2)} · {v.status}{v.expiresAt ? ` · expira em ${new Date(v.expiresAt).toLocaleDateString('pt-BR')}` : ''}</small></div>
          </div>
        ))}</div>
      )}
    </section>

    <ConfirmModal
      aberto={confirmacao !== null}
      matricula={confirmacao}
      enviando={enviando}
      onCancelar={() => setConfirmacao(null)}
      onConfirmar={confirmarDesistir}
    />

    <RewardModal resultado={recompensa} onFechar={() => setRecompensa(null)} />
  </main>
}

function Modal({ aberto, onFechar, children }: { aberto: boolean, onFechar: () => void, children: ReactNode }) {
  useEffect(() => {
    if (!aberto) return
    const aoPressionarTecla = (evento: KeyboardEvent) => {
      if (evento.key === 'Escape') onFechar()
    }
    window.addEventListener('keydown', aoPressionarTecla)
    document.body.style.overflow = 'hidden'
    return () => {
      window.removeEventListener('keydown', aoPressionarTecla)
      document.body.style.overflow = ''
    }
  }, [aberto, onFechar])

  if (!aberto) return null

  return (
    <div className="modal-overlay" onClick={onFechar}>
      <div className="modal-card" role="dialog" aria-modal="true" onClick={(evento) => evento.stopPropagation()}>
        {children}
      </div>
    </div>
  )
}

function ConfirmModal({ aberto, matricula, enviando, onCancelar, onConfirmar }: {
  aberto: boolean
  matricula: MatriculaResponseDTO | null
  enviando: boolean
  onCancelar: () => void
  onConfirmar: () => void
}) {
  if (!matricula) return null
  const emAndamento = matricula.status === 'INICIADO'

  return (
    <Modal aberto={aberto} onFechar={onCancelar}>
      <span className="modal-icon danger"><Icon name={emAndamento ? 'alert' : 'book'} /></span>
      <h3>{emAndamento ? 'Desistir do curso?' : 'Apagar do histórico?'}</h3>
      <p>
        {emAndamento
          ? <>Você vai desistir de <strong>"{matricula.nomeCurso}"</strong>. A matrícula será removida do seu histórico e todo o progresso nela será perdido.</>
          : <>Você vai apagar <strong>"{matricula.nomeCurso}"</strong> do seu histórico. Esta ação não pode ser desfeita.</>}
      </p>
      <div className="modal-actions">
        <button className="secondary-button" onClick={onCancelar} disabled={enviando}>Cancelar</button>
        <button className="danger-button" onClick={onConfirmar} disabled={enviando}>
          {enviando ? 'Removendo...' : emAndamento ? 'Sim, desistir' : 'Sim, apagar'}
        </button>
      </div>
    </Modal>
  )
}

function RewardModal({ resultado, onFechar }: { resultado: ConcluirCursoResponseDTO | null, onFechar: () => void }) {
  if (!resultado) return null
  const virouPremium = resultado.upgradePremium
  // TDD5: a cada 12 cursos concluídos com nota > 7,0 o aluno Premium recebe 3 moedas + voucher de novo
  const recompensaRecorrente = !virouPremium && resultado.voucher !== null
  const temRecompensas = virouPremium || recompensaRecorrente || resultado.cursosLiberados.length > 0

  return (
    <Modal aberto onFechar={onFechar}>
      <div className="reward-hero">
        <span className="reward-hero-icon"><Icon name={virouPremium || recompensaRecorrente ? 'spark' : 'check'} /></span>
        {(virouPremium || recompensaRecorrente) && (
          <span className="reward-eyebrow">{virouPremium ? 'UPGRADE DESBLOQUEADO' : 'A CADA 12 CURSOS'}</span>
        )}
        <h3>
          {virouPremium ? 'Você virou PREMIUM!' : recompensaRecorrente ? 'Recompensa desbloqueada!' : 'Curso concluído!'}
        </h3>
        <p>
          {virouPremium
            ? 'Parabéns! Ao concluir o 12º curso com nota superior a 7,0, sua assinatura foi alterada para o plano Premium.'
            : recompensaRecorrente
              ? 'Mais um ciclo de 12 cursos concluídos com nota superior a 7,0: você recebe novamente moedas e um voucher para projetos reais.'
              : `Nota superior a 7,0 em "${resultado.nomeCurso}". Continue assim para evoluir seu plano.`}
        </p>
      </div>

      {temRecompensas && (
      <div className="reward-rows">
        {resultado.moedasRecebidas > 0 && (
          <div className="reward-row">
            <span className="reward-row-icon coin"><Icon name="coin" /></span>
            <div><b>{resultado.moedasRecebidas} moeda{resultado.moedasRecebidas === 1 ? '' : 's'} recebida{resultado.moedasRecebidas === 1 ? '' : 's'}</b><small>Saldo atual: {(resultado.moedas ?? 0).toLocaleString('pt-BR')}</small></div>
          </div>
        )}
        {resultado.voucher && (
          <div className="reward-row">
            <span className="reward-row-icon ticket"><Icon name="ticket" /></span>
            <div><b>{resultado.voucher.nome}</b><small>R$ {resultado.voucher.valorEmReais.toFixed(2)} · válido até {resultado.voucher.expiresAt ? new Date(resultado.voucher.expiresAt).toLocaleDateString('pt-BR') : '—'}</small></div>
          </div>
        )}
        {resultado.cursosLiberados.length > 0 && (
          <div className="reward-row">
            <span className="reward-row-icon book"><Icon name="book" /></span>
            <div>
              <b>{resultado.cursosLiberados.length} novos cursos liberados</b>
              <small>{resultado.cursosLiberados.map(curso => curso.nomeCurso).join(' · ')}</small>
            </div>
          </div>
        )}
      </div>
      )}

      <div className="modal-actions">
        <button className="primary-button full" onClick={onFechar}>Continuar <Icon name="arrow" /></button>
      </div>
    </Modal>
  )
}

export default App
