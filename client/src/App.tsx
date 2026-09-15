import { FormEvent, useEffect, useState } from 'react'
import { createAluno, getAlunoById, type AlunoResponseDTO } from './services/api'
import './App.css'

type Page = 'home' | 'register' | 'dashboard'

type MockCourse = {
  id: number
  name: string
  category: string
  progress: number
  status: 'Em andamento' | 'Concluído' | 'Bloqueado'
}

const mockCourses: MockCourse[] = [
  { id: 1, name: 'Fundamentos de DevOps', category: 'DevOps', progress: 78, status: 'Em andamento' },
  { id: 2, name: 'Integração Contínua', category: 'CI/CD', progress: 42, status: 'Em andamento' },
  { id: 3, name: 'Testes Automatizados', category: 'Qualidade', progress: 100, status: 'Concluído' },
  { id: 4, name: 'Observabilidade', category: 'Infraestrutura', progress: 0, status: 'Bloqueado' },
]

const mockVouchers = [
  { title: '10% OFF — Curso de Cloud', code: 'CLOUD10', expires: '30/10/2026' },
  { title: '15% OFF — Workshop DevOps', code: 'DEVOPS15', expires: '15/11/2026' },
]

function Icon({ name }: { name: 'home' | 'book' | 'ticket' | 'user' | 'coin' | 'arrow' | 'check' | 'lock' | 'logout' | 'spark' }) {
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
  }
  return (
    <svg className="icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d={paths[name]} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function App() {
  const [page, setPage] = useState<Page>(() => localStorage.getItem('gamificacao-page') as Page || 'home')
  const [aluno, setAluno] = useState<AlunoResponseDTO | null>(null)
  const [loadingAluno, setLoadingAluno] = useState(false)
  const [apiError, setApiError] = useState('')

  useEffect(() => {
    const savedId = localStorage.getItem('gamificacao-aluno-id')
    if (!savedId) return

    setLoadingAluno(true)
    getAlunoById(savedId)
      .then(setAluno)
      .catch(() => {
        localStorage.removeItem('gamificacao-aluno-id')
      })
      .finally(() => setLoadingAluno(false))
  }, [])

  useEffect(() => {
    localStorage.setItem('gamificacao-page', page)
  }, [page])

  const openRegister = () => {
    setApiError('')
    setPage('register')
  }

  const openDashboard = () => {
    const id = localStorage.getItem('gamificacao-aluno-id')
    if (!id) {
      setPage('register')
      return
    }
    setPage('dashboard')
  }

  const handleRegister = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setApiError('')

    const form = new FormData(event.currentTarget)
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
      localStorage.setItem('gamificacao-aluno-id', created.id)
      setPage('dashboard')
      event.currentTarget.reset()
    } catch (error) {
      setApiError(error instanceof Error ? error.message : 'Não foi possível criar sua conta.')
    } finally {
      setLoadingAluno(false)
    }
  }

  const logout = () => {
    localStorage.removeItem('gamificacao-aluno-id')
    localStorage.removeItem('gamificacao-page')
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
          <button className="header-register" onClick={openRegister}>Criar conta</button>
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
            <button type="button" className="back-link" onClick={() => setPage('home')}>← Voltar para o início</button>
          </form>
        </main>
      )}

      {page === 'dashboard' && (
        <Dashboard aluno={aluno} loading={loadingAluno} onLogout={logout} onRegister={openRegister} />
      )}

      <footer><span>LEVELUP © 2026</span><span>Aprenda. Conquiste. Evolua.</span></footer>
    </div>
  )
}

function Feature({ icon, number, title, text }: { icon: 'book' | 'spark' | 'ticket', number: string, title: string, text: string }) {
  return <article className="feature-card"><div className="feature-icon"><Icon name={icon} /></div><span>{number}</span><h3>{title}</h3><p>{text}</p></article>
}

function Dashboard({ aluno, loading, onLogout, onRegister }: { aluno: AlunoResponseDTO | null, loading: boolean, onLogout: () => void, onRegister: () => void }) {
  if (!aluno && !loading) {
    return <main className="empty-dashboard"><div className="empty-icon"><Icon name="user" /></div><h1>Nenhum perfil encontrado.</h1><p>Crie sua conta para acessar seu dashboard.</p><button className="primary-button" onClick={onRegister}>Criar conta <Icon name="arrow" /></button></main>
  }

  const firstName = aluno?.name?.split(' ')[0] || 'aluno'
  const plan = aluno?.plano || 'BASICO'
  const ra = aluno?.ra?.valor || 'Não informado'
  const progress = mockCourses.filter(c => c.status === 'Concluído').length

  return <main className="dashboard-page">
    <section className="dashboard-head">
      <div><span className="eyebrow">SEU ESPAÇO</span><h1>Olá, {firstName}.</h1><p>Continue sua jornada. Cada passo conta.</p></div>
      <button className="logout-button" onClick={onLogout}><Icon name="logout" /> Sair</button>
    </section>

    <section className="stats-grid">
      <div className="stat-card accent"><span className="stat-icon"><Icon name="spark" /></span><div><small>NÍVEL</small><strong>07</strong><span>+350 XP esta semana</span></div></div>
      <div className="stat-card"><span className="stat-icon"><Icon name="coin" /></span><div><small>MOEDAS</small><strong>{(aluno?.moedas ?? 1280).toLocaleString('pt-BR')}</strong><span>Saldo disponível</span></div></div>
      <div className="stat-card"><span className="stat-icon"><Icon name="book" /></span><div><small>CURSOS CONCLUÍDOS</small><strong>{progress}</strong><span>de {mockCourses.length} iniciados</span></div></div>
      <div className="stat-card"><span className="stat-icon"><Icon name="ticket" /></span><div><small>PLANO</small><strong>{plan}</strong><span>{plan === 'PREMIUM' ? 'Acesso Premium' : 'Plano atual'}</span></div></div>
    </section>

    <section className="dashboard-columns">
      <div className="panel courses-panel">
        <div className="panel-head"><div><span>APRENDIZADO</span><h2>Meus cursos</h2></div><span className="mock-label">DEMONSTRAÇÃO</span></div>
        <div className="course-list">{mockCourses.map(course => <div className="course-row" key={course.id}><div className="course-icon"><Icon name={course.status === 'Bloqueado' ? 'lock' : course.status === 'Concluído' ? 'check' : 'book'} /></div><div className="course-info"><b>{course.name}</b><small>{course.category}</small><div className="progress"><i style={{ width: `${course.progress}%` }} /></div></div><div className={`course-status ${course.status.toLowerCase().replace(' ', '-')}`}>{course.status}</div></div>)}</div>
      </div>

      <div className="panel profile-panel">
        <div className="panel-head"><div><span>PERFIL</span><h2>Seus dados</h2></div><span className="plan-badge">{plan}</span></div>
        <div className="profile-main"><div className="big-avatar">{aluno?.name?.charAt(0).toUpperCase() || 'A'}</div><div><h3>{aluno?.name || 'Carregando...'}</h3><p>Aluno LevelUp</p></div></div>
        <div className="data-row"><span>RA</span><b>{ra}</b></div>
        <div className="data-row"><span>Plano</span><b>{plan}</b></div>
        <div className="data-row"><span>ID</span><b className="id-value">{aluno?.id || '—'}</b></div>
      </div>
    </section>

    <section className="panel voucher-panel">
      <div className="panel-head"><div><span>RECOMPENSAS</span><h2>Seus vouchers</h2></div><span className="mock-label">DEMONSTRAÇÃO</span></div>
      <div className="voucher-grid">{mockVouchers.map(v => <div className="voucher" key={v.code}><span className="voucher-icon"><Icon name="ticket" /></span><div><b>{v.title}</b><small>Código: <strong>{v.code}</strong> · válido até {v.expires}</small></div><button title="Voucher demonstrativo">Ver</button></div>)}</div>
      <p className="mock-notice"><Icon name="spark" /> Cursos, vouchers, progresso e moedas exibidos nesta área são dados de demonstração enquanto esses endpoints não estão disponíveis na API.</p>
    </section>
  </main>
}

export default App
