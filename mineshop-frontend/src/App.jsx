import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { NavLink, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import {
  Activity, ArrowDownRight, ArrowUpRight, Boxes, Check, ChevronLeft, ChevronRight,
  CircleAlert, ClipboardList, Clock3, Command, DoorOpen, Eye, EyeOff, LayoutDashboard,
  LoaderCircle, LogOut, Menu, Package, Plus, RefreshCw, Search, ShieldCheck, ShoppingCart,
  Trash2, Users, X
} from 'lucide-react'
import { api, errorMessage } from './api'

const money = value => `${Number(value ?? 0).toLocaleString('ru-RU', { maximumFractionDigits: 2 })} ₽`
const date = value => value ? new Date(value).toLocaleString('ru-RU') : '—'
const statusLabels = {
  PENDING: 'Ожидает оплаты', CREATED: 'Создан', PAID: 'Оплачен',
  COMPLETED: 'Завершён', CANCELLED: 'Отменён', FAILED: 'Ошибка',
  REFUNDED: 'Возвращён'
}
const statusClass = status => String(status || '').toLowerCase()
const navItems = [
  { to: '/', label: 'Обзор', icon: LayoutDashboard, end: true },
  { to: '/orders', label: 'Заказы', icon: ShoppingCart },
  { to: '/products', label: 'Товары', icon: Package },
  { to: '/users', label: 'Пользователи', icon: Users },
  { to: '/admins', label: 'Администраторы', icon: ShieldCheck },
]

function Button({ children, variant = 'primary', className = '', ...props }) {
  return <button className={`btn btn-${variant} ${className}`} {...props}>{children}</button>
}
function Field({ label, children, hint }) {
  return <label className="field"><span>{label}</span>{children}{hint && <small>{hint}</small>}</label>
}
function Modal({ title, onClose, children, wide = false }) {
  useEffect(() => {
    const close = e => e.key === 'Escape' && onClose()
    window.addEventListener('keydown', close)
    return () => window.removeEventListener('keydown', close)
  }, [onClose])
  return <div className="modal-backdrop" onMouseDown={e => e.target === e.currentTarget && onClose()}>
    <section className={`modal ${wide ? 'modal-wide' : ''}`} role="dialog" aria-modal="true">
      <header className="modal-head"><h2>{title}</h2><button className="icon-btn" onClick={onClose} aria-label="Закрыть"><X size={18}/></button></header>
      {children}
    </section>
  </div>
}
function ErrorBanner({ children, onClose }) {
  if (!children) return null
  return <div className="error-banner"><CircleAlert size={17}/><span>{children}</span>{onClose && <button onClick={onClose}><X size={16}/></button>}</div>
}
function Notice({ children }) { return children ? <div className="notice">{children}</div> : null }
function Empty({ children = 'Нет данных для отображения.' }) { return <div className="empty">{children}</div> }
function Loading() { return <div className="loading"><LoaderCircle className="spin" size={22}/> Загрузка…</div> }
function PageHeading({ eyebrow, title, description, actions }) {
  return <div className="page-heading"><div><div className="eyebrow">{eyebrow}</div><h1>{title}</h1>{description && <p>{description}</p>}</div>{actions && <div className="heading-actions">{actions}</div>}</div>
}
function SearchBox({ value, onChange, placeholder = 'Поиск…' }) {
  return <label className="search-box"><Search size={17}/><input value={value} onChange={e => onChange(e.target.value)} placeholder={placeholder}/>{value && <button onClick={() => onChange('')} type="button"><X size={14}/></button>}</label>
}
function TableFrame({ children }) { return <div className="table-wrap"><table>{children}</table></div> }
function Pagination({ page, setPage, hasNext }) {
  return <div className="pagination"><span>Страница {page + 1}</span><div><Button variant="secondary" disabled={page <= 0} onClick={() => setPage(p => Math.max(0, p - 1))}><ChevronLeft size={16}/> Назад</Button><Button variant="secondary" disabled={!hasNext} onClick={() => setPage(p => p + 1)}>Далее <ChevronRight size={16}/></Button></div></div>
}
function useToast() {
  const [toast, setToast] = useState('')
  useEffect(() => {
    if (!toast) return
    const timer = setTimeout(() => setToast(''), 3500)
    return () => clearTimeout(timer)
  }, [toast])
  return [toast, setToast]
}
function useApiData(loader, deps = []) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [refreshKey, setRefreshKey] = useState(0)
  const refresh = () => setRefreshKey(k => k + 1)
  useEffect(() => {
    let alive = true
    setLoading(true); setError('')
    loader().then(result => { if (alive) setData(result) })
      .catch(err => { if (alive) setError(errorMessage(err)) })
      .finally(() => { if (alive) setLoading(false) })
    return () => { alive = false }
  // loader identity is intentionally controlled by deps and refreshKey
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, refreshKey])
  return { data, setData, loading, error, refresh, setError }
}

function Login({ onLogin }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  async function submit(e) {
    e.preventDefault(); setError(''); setLoading(true)
    try {
      const { data } = await api.post('/api/admin/auth/login', { username: username.trim(), password })
      localStorage.setItem('mineshop_token', data.token)
      localStorage.setItem('mineshop_username', data.username)
      onLogin(data.username)
    } catch (err) { setError(errorMessage(err)) }
    finally { setLoading(false) }
  }
  return <div className="login-screen"><div className="login-art"><div className="brand"><div className="brand-mark"><Command size={23}/></div><span>Mineshop<span className="brand-muted"> / ADMIN</span></span></div><div className="login-art-copy"><div className="eyebrow">ПАНЕЛЬ УПРАВЛЕНИЯ</div><h1>Магазин под<br/>полным контролем.</h1><p>Управляйте товарами, заказами и пользователями из одного рабочего пространства.</p><div className="art-grid"><div/><div/><div/><div/></div></div><div className="login-art-foot">MINESHOP CONTROL CENTER <span>v1.0</span></div></div>
    <div className="login-panel"><form className="login-form" onSubmit={submit}><div className="mobile-brand"><div className="brand-mark"><Command size={22}/></div><strong>Mineshop Admin</strong></div><div className="eyebrow">С РАДОСТЬЮ СНОВА ВИДИМ</div><h2>Вход в систему</h2><p className="muted">Введите данные администратора для продолжения.</p><ErrorBanner>{error}</ErrorBanner><Field label="Имя пользователя"><input autoComplete="username" autoFocus required value={username} onChange={e => setUsername(e.target.value)} placeholder="admin"/></Field><Field label="Пароль"><div className="password-input"><input autoComplete="current-password" required type={showPassword ? 'text' : 'password'} value={password} onChange={e => setPassword(e.target.value)} placeholder="Введите пароль"/><button type="button" onClick={() => setShowPassword(s => !s)}>{showPassword ? <EyeOff size={17}/> : <Eye size={17}/>}</button></div></Field><Button disabled={loading} className="login-submit">{loading ? <LoaderCircle className="spin" size={17}/> : null} Войти в панель <ChevronRight size={17}/></Button><div className="login-security"><ShieldCheck size={16}/> Доступ только для авторизованных администраторов</div></form></div></div>
}

function Dashboard() {
  const orders = useApiData(() => api.get('/api/order/list/0').then(r => r.data))
  const activeOrders = useApiData(() => api.get('/api/order/active/list/0').then(r => r.data))
  const products = useApiData(() => api.get('/api/product/list/0').then(r => r.data))
  const users = useApiData(() => api.get('/api/user/list/0').then(r => r.data))
  const [recent, setRecent] = useState([])
  useEffect(() => {
    if (Array.isArray(orders.data)) setRecent([...orders.data].sort((a,b) => new Date(b.createdAt)-new Date(a.createdAt)).slice(0,5))
  }, [orders.data])
  const sum = (orders.data || []).filter(o => ['PAID','COMPLETED'].includes(o.status)).reduce((a,o) => a + Number(o.cost || 0), 0)
  const cards = [
    { label: 'Заказы на странице', value: orders.data?.length ?? '—', note: 'Последняя загруженная страница', icon: ShoppingCart, tone: 'violet' },
    { label: 'Активные заказы', value: activeOrders.data?.length ?? '—', note: 'Согласно API active/list', icon: Activity, tone: 'green' },
    { label: 'Товары на странице', value: products.data?.length ?? '—', note: 'Последняя загруженная страница', icon: Package, tone: 'blue' },
    { label: 'Пользователи на странице', value: users.data?.length ?? '—', note: 'Последняя загруженная страница', icon: Users, tone: 'orange' },
  ]
  return <><PageHeading eyebrow="РАБОЧЕЕ ПРОСТРАНСТВО" title="Обзор" description="Сводка по данным магазина и последние заказы." actions={<Button variant="secondary" onClick={() => [orders.refresh(),activeOrders.refresh(),products.refresh(),users.refresh()]}><RefreshCw size={16}/> Обновить</Button>}/><div className="stats-grid">{cards.map(c => <div className="stat-card" key={c.label}><div className="stat-top"><span>{c.label}</span><span className={`stat-icon ${c.tone}`}><c.icon size={19}/></span></div><strong>{c.value}</strong><small>{c.note}</small></div>)}</div><div className="dashboard-grid"><section className="panel"><div className="panel-heading"><div><h2>Последние заказы</h2><p>Недавние записи из первой страницы API</p></div><NavLink className="text-link" to="/orders">Все заказы <ChevronRight size={15}/></NavLink></div>{orders.loading ? <Loading/> : orders.error ? <ErrorBanner>{orders.error}</ErrorBanner> : recent.length ? <TableFrame><thead><tr><th>Заказ</th><th>Пользователь</th><th>Сумма</th><th>Статус</th><th>Дата</th></tr></thead><tbody>{recent.map(o => <tr key={o.paymentId}><td><span className="mono">#{String(o.paymentId).slice(0,8)}</span></td><td><strong>{o.mcName}</strong><small className="cell-sub">ID {o.userId}</small></td><td className="numeric">{money(o.cost)}</td><td><span className={`status status-${statusClass(o.status)}`}>{statusLabels[o.status] || o.status}</span></td><td>{date(o.createdAt)}</td></tr>)}</tbody></TableFrame> : <Empty/>}</section><section className="panel quick-panel"><div className="panel-heading"><div><h2>Быстрые действия</h2><p>Часто используемые разделы</p></div></div><NavLink to="/products" className="quick-link"><span className="quick-icon blue"><Package size={18}/></span><span><strong>Управление товарами</strong><small>Создать, удалить или изменить активность</small></span><ChevronRight size={17}/></NavLink><NavLink to="/admins" className="quick-link"><span className="quick-icon violet"><ShieldCheck size={18}/></span><span><strong>Администраторы</strong><small>Добавить или удалить доступ</small></span><ChevronRight size={17}/></NavLink><NavLink to="/users" className="quick-link"><span className="quick-icon orange"><Users size={18}/></span><span><strong>Найти пользователя</strong><small>Профиль и история покупок</small></span><ChevronRight size={17}/></NavLink><div className="revenue-note"><div className="revenue-label">Оборот на загруженной странице</div><strong>{money(sum)}</strong><small>Только заказы со статусом PAID или COMPLETED. Не является общим оборотом магазина.</small></div></section></div></>
}

function OrdersPage() {
  const [page, setPage] = useState(0)
  const [activeOnly, setActiveOnly] = useState(false)
  const { data, loading, error, refresh, setError } = useApiData(() => api.get(`/api/order/${activeOnly ? 'active/list' : 'list'}/${page}`).then(r => r.data), [page, activeOnly])
  const [search, setSearch] = useState('')
  const [selected, setSelected] = useState(null)
  const [detailOrder, setDetailOrder] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')
  const [status, setStatus] = useState('PENDING')
  const [busy, setBusy] = useState(false)
  const [toast, setToast] = useToast()
  const filtered = (data || []).filter(o => [o.paymentId,o.mcName,o.userId,o.productId,o.status].some(v => String(v ?? '').toLowerCase().includes(search.toLowerCase())))
  async function openOrderDetail(order) {
    setDetailOrder(null); setDetailError(''); setDetailLoading(true)
    try { const r = await api.get(`/api/order/${order.paymentId}`); setDetailOrder(r.data) }
    catch (err) { setDetailError(errorMessage(err)) }
    finally { setDetailLoading(false) }
  }
  async function saveStatus(e) {
    e.preventDefault(); setBusy(true); setError('')
    try {
      await api.post('/api/order/status', { orderId: selected.paymentId, status })
      setToast('Статус заказа обновлён.'); setSelected(null); refresh()
    } catch (err) { setError(errorMessage(err)) }
    finally { setBusy(false) }
  }
  return <><PageHeading eyebrow="ТОРГОВЫЕ ОПЕРАЦИИ" title="Заказы" description="Просмотр заказов и управление их статусами." actions={<Button variant="secondary" onClick={refresh}><RefreshCw size={16}/> Обновить</Button>}/><ErrorBanner>{error}</ErrorBanner><Notice>В текущем backend есть просмотр заказов и изменение статуса через <code>POST /api/order/status</code>. Endpoint для создания/редактирования заказа отсутствует; деактивация доступна только если соответствующий статус поддерживается enum <code>OrderStatus</code>.</Notice><div className="toolbar"><SearchBox value={search} onChange={setSearch} placeholder="Поиск по ID, нику, товару…"/><label className="toggle"><input type="checkbox" checked={activeOnly} onChange={e => {setActiveOnly(e.target.checked);setPage(0)}}/><span className="toggle-ui"/><span>Только активные</span></label></div><section className="panel table-panel">{loading ? <Loading/> : filtered.length ? <TableFrame><thead><tr><th>Заказ / платёж</th><th>Покупатель</th><th>Товар</th><th>Кол-во</th><th>Сумма</th><th>Статус</th><th>Создан</th><th></th></tr></thead><tbody>{filtered.map(o => <tr key={o.paymentId}><td><span className="mono">#{String(o.paymentId).slice(0,8)}</span><small className="cell-sub">{String(o.paymentId)}</small></td><td><strong>{o.mcName}</strong><small className="cell-sub">ID {o.userId}</small></td><td>{o.productId}</td><td>{o.amount}</td><td className="numeric">{money(o.cost)}</td><td><span className={`status status-${statusClass(o.status)}`}>{statusLabels[o.status] || o.status}</span></td><td>{date(o.createdAt)}</td><td><div className="row-actions"><Button variant="ghost" onClick={() => openOrderDetail(o)}>Подробнее</Button><Button variant="ghost" onClick={() => {setSelected(o);setStatus(o.status)}}>Статус</Button></div></td></tr>)}</tbody></TableFrame> : <Empty>{error ? 'Не удалось загрузить заказы.' : 'Заказы не найдены на этой странице.'}</Empty>}</section><Pagination page={page} setPage={setPage} hasNext={!loading && (data?.length ?? 0) > 0}/>{toast && <div className="toast"><Check size={17}/>{toast}</div>}{(detailLoading || detailOrder || detailError) && <Modal title="Детали заказа" onClose={() => {setDetailOrder(null);setDetailError('');setDetailLoading(false)}}>{detailLoading ? <Loading/> : detailError ? <div className="modal-body"><ErrorBanner>{detailError}</ErrorBanner></div> : detailOrder && <div className="modal-body"><div className="detail-highlight"><span className="eyebrow">PAYMENT ID</span><strong className="mono">{detailOrder.paymentId}</strong><span>{detailOrder.mcName} · {money(detailOrder.cost)}</span></div><div className="detail-list">{[['Пользователь ID', detailOrder.userId],['Товар ID', detailOrder.productId],['Количество', detailOrder.amount],['Статус', statusLabels[detailOrder.status] || detailOrder.status],['Создан', date(detailOrder.createdAt)],['Оплачен', date(detailOrder.paidAt)],['Завершён', date(detailOrder.completeAt)]].map(([k,v]) => <div className="detail-row" key={k}><span>{k}</span><strong>{v ?? '—'}</strong></div>)}</div></div>}</Modal>}{selected && <Modal title="Изменить статус заказа" onClose={() => setSelected(null)}><form className="modal-body" onSubmit={saveStatus}><div className="detail-highlight"><span className="eyebrow">ЗАКАЗ</span><strong className="mono">{selected.paymentId}</strong><span>{selected.mcName} · {money(selected.cost)}</span></div><Field label="Новый статус"><select value={status} onChange={e => setStatus(e.target.value)}>{['PENDING','CREATED','PAID','COMPLETED','CANCELLED','FAILED','REFUNDED'].map(s => <option key={s} value={s}>{statusLabels[s] || s} ({s})</option>)}</select></Field><p className="form-hint">Список статусов в интерфейсе — распространённый набор. Если ваш enum OrderStatus отличается, оставьте только реально существующие значения.</p><div className="modal-actions"><Button variant="secondary" type="button" onClick={() => setSelected(null)}>Отмена</Button><Button disabled={busy}>{busy ? 'Сохранение…' : 'Сохранить статус'}</Button></div></form></Modal>}</>
}


function ProductCover({ id, alt = '', className = '', version = 0 }) {
  const [src, setSrc] = useState('')
  const [failed, setFailed] = useState(false)
  useEffect(() => {
    let active = true
    let objectUrl = ''
    setSrc('')
    setFailed(false)
    api.get(`/api/product/cover/${id}`, { responseType: 'blob' })
      .then(({ data }) => {
        if (!active) return
        objectUrl = URL.createObjectURL(data)
        setSrc(objectUrl)
      })
      .catch(() => { if (active) setFailed(true) })
    return () => {
      active = false
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [id, version])
  if (!src || failed) return <div className={`product-placeholder ${className}`}><Package size={18}/></div>
  return <img className={className} src={src} alt={alt} onError={() => setFailed(true)} />
}

function ProductsPage() {
  const [page, setPage] = useState(0)
  const { data, loading, error, refresh, setError } = useApiData(() => api.get(`/api/product/list/${page}`).then(r => r.data), [page])
  const [search, setSearch] = useState('')
  const [editing, setEditing] = useState(null)
  const [detailProduct, setDetailProduct] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')
  const [busy, setBusy] = useState(false)
  const [coverVersion, setCoverVersion] = useState(0)
  const [toast, setToast] = useToast()
  const filtered = (data || []).filter(p => [p.id,p.title,p.description,p.giveCommand].some(v => String(v ?? '').toLowerCase().includes(search.toLowerCase())))

  function openCreate() {
    setEditing({ id: '', title: '', description: '', price: '0', giveCommand: '', isActive: true, coverFile: null, isNew: true })
  }

  async function uploadCover(id, file) {
    if (!file) return
    const formData = new FormData()
    formData.append('file', file)
    await api.post(`/api/product/cover/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }

  async function saveProduct(e) {
    e.preventDefault();
    setBusy(true);
    setError('');

    const payload = {
      title: editing.title.trim(),
      description: editing.description.trim(),
      price: Number(editing.price),
      giveCommand: editing.giveCommand.trim(),
      isActive: Boolean(editing.isActive),
    };

    try {
      let productId = editing.id;

      if (editing.isNew) {
        const response = await api.put('/api/product/create', payload);
        productId = response.data.id;
      } else {
        await api.post(`/api/product/edit/${editing.id}`, payload);
      }

      if (editing.coverFile) {
        const formData = new FormData();
        formData.append('file', editing.coverFile);

        await api.post(
            `/api/product/cover/${productId}`,
            formData,
            {
              headers: {
                'Content-Type': 'multipart/form-data',
              },
            }
        );
      }

      setToast(
          editing.isNew
              ? 'Товар создан.'
              : 'Товар обновлён.'
      );
      setEditing(null);
      refresh();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  async function openProductDetail(p) {
    setDetailProduct(null)
    setDetailError('')
    setDetailLoading(true)
    try {
      const r = await api.get(`/api/product/${p.id}`)
      setDetailProduct(r.data)
    } catch (err) {
      setDetailError(errorMessage(err))
    } finally {
      setDetailLoading(false)
    }
  }

  async function toggle(p) {
    setError('')
    try {
      await api.post('/api/product/active', { id: p.id, isActive: !p.isActive })
      setToast(`Товар ${!p.isActive ? 'активирован' : 'деактивирован'}.`)
      refresh()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  async function remove(p) {
    if (!window.confirm(`Удалить товар «${p.title}» (ID ${p.id})?`)) return
    setError('')
    try {
      await api.delete(`/api/product/${p.id}`)
      setToast('Товар удалён.')
      refresh()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  return <>
    <PageHeading eyebrow="КАТАЛОГ" title="Товары" description="Каталог магазина, цены, обложки, команды выдачи и доступность." actions={<Button onClick={openCreate}><Plus size={17}/> Создать товар</Button>}/>
    <ErrorBanner>{error}</ErrorBanner>
    <div className="toolbar"><SearchBox value={search} onChange={setSearch} placeholder="Поиск товара…"/><Button variant="secondary" onClick={refresh}><RefreshCw size={16}/> Обновить</Button></div>
    <section className="panel table-panel">
      {loading ? <Loading/> : filtered.length ? <TableFrame>
        <thead><tr><th>Товар</th><th>ID</th><th>Цена</th><th>Команда выдачи</th><th>Статус</th><th>Действия</th></tr></thead>
        <tbody>{filtered.map(p => <tr key={p.id}>
          <td><div className="product-cell"><ProductCover id={p.id} alt={p.title} version={coverVersion}/><div><strong>{p.title}</strong><small className="cell-sub description-cell">{p.description}</small></div></div></td>
          <td className="mono">{p.id}</td>
          <td className="numeric">{money(p.price)}</td>
          <td><code className="command-cell">{p.giveCommand}</code></td>
          <td><span className={`status ${p.isActive ? 'status-active' : 'status-inactive'}`}>{p.isActive ? 'Активен' : 'Отключён'}</span></td>
          <td><div className="row-actions">
            <Button variant="ghost" onClick={() => openProductDetail(p)}>Подробнее</Button>
            <Button variant="ghost" onClick={() => setEditing({ ...p, price: String(p.price), coverFile: null, isNew: false })}>Изменить</Button>
            <Button variant="ghost" onClick={() => toggle(p)}>{p.isActive ? 'Отключить' : 'Включить'}</Button>
            <button className="icon-btn danger" onClick={() => remove(p)} title="Удалить"><Trash2 size={16}/></button>
          </div></td>
        </tr>)}</tbody>
      </TableFrame> : <Empty>{error ? 'Не удалось загрузить товары.' : 'Товары не найдены.'}</Empty>}
    </section>
    <Pagination page={page} setPage={setPage} hasNext={!loading && (data?.length ?? 0) > 0}/>
    {toast && <div className="toast"><Check size={17}/>{toast}</div>}

    {(detailLoading || detailProduct || detailError) && <Modal title="Детали товара" onClose={() => {setDetailProduct(null);setDetailError('');setDetailLoading(false)}} wide>
      {detailLoading ? <Loading/> : detailError ? <div className="modal-body"><ErrorBanner>{detailError}</ErrorBanner></div> : detailProduct && <div className="modal-body">
        <ProductCover id={detailProduct.id} alt={detailProduct.title} className="product-detail-cover" version={coverVersion}/>
        <div className="detail-highlight"><span className="eyebrow">ТОВАР #{detailProduct.id}</span><strong>{detailProduct.title}</strong><span>{money(detailProduct.price)}</span></div>
        <p className="detail-description">{detailProduct.description}</p>
        <div className="detail-list">{[['Команда выдачи', detailProduct.giveCommand],['Статус', detailProduct.isActive ? 'Активен' : 'Отключён']].map(([k,v]) => <div className="detail-row" key={k}><span>{k}</span><strong>{v ?? '—'}</strong></div>)}</div>
      </div>}
    </Modal>}

    {editing && <Modal title={editing.isNew ? 'Создать товар' : 'Редактирование товара'} onClose={() => setEditing(null)} wide>
      <form className="modal-body" onSubmit={saveProduct}>
        <div className="form-grid">
          <Field label="Название"><input required value={editing.title} onChange={e => setEditing({...editing,title:e.target.value})}/></Field>
          <Field label="Цена (₽)"><input type="number" min="0" step="0.01" required value={editing.price} onChange={e => setEditing({...editing,price:e.target.value})}/></Field>
          <Field label="Команда выдачи"><input required value={editing.giveCommand} onChange={e => setEditing({...editing,giveCommand:e.target.value})} placeholder="give %player% diamond 1"/></Field>
          <Field label="Статус"><select value={String(editing.isActive)} onChange={e => setEditing({...editing,isActive:e.target.value==='true'})}><option value="true">Активен</option><option value="false">Отключён</option></select></Field>
          <Field label="Обложка товара"><input type="file" accept="image/*" onChange={e => setEditing({...editing,coverFile:e.target.files?.[0] || null})}/><small>{editing.coverFile ? `Выбран файл: ${editing.coverFile.name}` : 'Выберите изображение; загрузка выполняется через multipart/form-data.'}</small></Field>
          <div className="span-2"><Field label="Описание"><textarea rows="3" value={editing.description} onChange={e => setEditing({...editing,description:e.target.value})}/></Field></div>
        </div>
        <Notice>При создании используется <code>PUT /api/product/create</code> — ID генерирует backend и возвращает в ответе. При редактировании используется <code>POST /api/product/edit</code>. Выбранная обложка загружается на <code>POST /api/product/cover/{'{id}'}</code>.</Notice>
        <div className="modal-actions"><Button variant="secondary" type="button" onClick={() => setEditing(null)}>Отмена</Button><Button disabled={busy}>{busy ? 'Сохранение…' : 'Сохранить'}</Button></div>
      </form>
    </Modal>}
  </>
}

function UsersPage() {
  const [page, setPage] = useState(0)
  const { data, loading, error, refresh } = useApiData(() => api.get(`/api/user/list/${page}`).then(r => r.data), [page])
  const [search, setSearch] = useState('')
  const [selected, setSelected] = useState(null)
  const [profile, setProfile] = useState(null)
  const [profileError, setProfileError] = useState('')
  const [historyPage, setHistoryPage] = useState(0)
  const [history, setHistory] = useState([])
  const [historyLoading, setHistoryLoading] = useState(false)
  const [historyError, setHistoryError] = useState('')
  const filtered = (data || []).filter(u => [u.id,u.username,u.mcNickname].some(v => String(v ?? '').toLowerCase().includes(search.toLowerCase())))
  async function openUser(u) {
    setSelected(u); setProfile(null); setProfileError(''); setHistory([]); setHistoryError(''); setHistoryPage(0)
    try { const r = await api.get(`/api/user/${u.id}`); setProfile(r.data) } catch(e) { setProfileError(errorMessage(e)) }
    loadHistory(u.id, 0)
  }
  async function loadHistory(id, p) {
    setHistoryLoading(true); setHistoryError('')
    try { const r = await api.get(`/api/user/history/${id}/${p}`); setHistory(r.data) }
    catch(e) { setHistoryError(errorMessage(e)); setHistory([]) }
    finally { setHistoryLoading(false) }
  }
  function changeHistoryPage(p) { setHistoryPage(p); loadHistory(selected.id, p) }
  return <><PageHeading eyebrow="КЛИЕНТСКАЯ БАЗА" title="Пользователи" description="Профили покупателей и история заказов." actions={<Button variant="secondary" onClick={refresh}><RefreshCw size={16}/> Обновить</Button>}/><ErrorBanner>{error}</ErrorBanner><div className="toolbar"><SearchBox value={search} onChange={setSearch} placeholder="Ник, логин или ID…"/></div><section className="panel table-panel">{loading ? <Loading/> : filtered.length ? <TableFrame><thead><tr><th>Пользователь</th><th>ID</th><th>Minecraft ник</th><th></th></tr></thead><tbody>{filtered.map(u => <tr key={u.id}><td><div className="user-cell"><span className="avatar">{String(u.username || '?').slice(0,1).toUpperCase()}</span><div><strong>{u.username}</strong><small className="cell-sub">Клиент магазина</small></div></div></td><td className="mono">{u.id}</td><td>{u.mcNickname}</td><td><Button variant="ghost" onClick={() => openUser(u)}>Профиль и история <ChevronRight size={15}/></Button></td></tr>)}</tbody></TableFrame> : <Empty/>}</section><Pagination page={page} setPage={setPage} hasNext={!loading && (data?.length ?? 0) > 0}/>{selected && <Modal title="Профиль пользователя" onClose={() => setSelected(null)} wide><div className="modal-body">{profileError && <ErrorBanner>{profileError}</ErrorBanner>}{!profile && !profileError ? <Loading/> : profile && <div className="profile-card"><span className="avatar avatar-large">{String(profile.username || '?').slice(0,1).toUpperCase()}</span><div><h3>{profile.username}</h3><p className="muted">Minecraft: {profile.mcNickname}</p><small className="muted">ID пользователя: {profile.id}</small></div></div>}<div className="panel-heading history-heading"><div><h3>История заказов</h3><p>Страница {historyPage + 1}</p></div><div className="history-nav"><Button variant="secondary" disabled={historyPage===0 || historyLoading} onClick={() => changeHistoryPage(historyPage-1)}><ChevronLeft size={15}/></Button><Button variant="secondary" disabled={historyLoading || !history.length} onClick={() => changeHistoryPage(historyPage+1)}><ChevronRight size={15}/></Button></div></div>{historyError && <ErrorBanner>{historyError}</ErrorBanner>}{historyLoading ? <Loading/> : history.length ? <TableFrame><thead><tr><th>Заказ</th><th>Товар</th><th>Сумма</th><th>Статус</th><th>Дата</th></tr></thead><tbody>{history.map(o => <tr key={o.paymentId}><td className="mono">{String(o.paymentId).slice(0,8)}</td><td>{o.productId}</td><td>{money(o.cost)}</td><td><span className={`status status-${statusClass(o.status)}`}>{statusLabels[o.status] || o.status}</span></td><td>{date(o.createdAt)}</td></tr>)}</tbody></TableFrame> : <Empty>История заказов пуста.</Empty>}</div></Modal>}</>
}

function AdminsPage() {
  const { data, loading, error, refresh, setError } = useApiData(() => api.get('/api/admin/admins').then(r => r.data))
  const [search, setSearch] = useState('')
  const [creating, setCreating] = useState(false)
  const [form, setForm] = useState({ username: '', password: '' })
  const [busy, setBusy] = useState(false)
  const [toast, setToast] = useToast()
  const filtered = (data || []).filter(a => a.username.toLowerCase().includes(search.toLowerCase()))
  async function create(e) {
    e.preventDefault(); setBusy(true); setError('')
    try { await api.put('/api/admin/create', form); setToast('Администратор создан.'); setCreating(false); setForm({username:'',password:''}); refresh() }
    catch(err) { setError(errorMessage(err)) }
    finally { setBusy(false) }
  }
  async function remove(a) {
    if (!window.confirm(`Удалить администратора ${a.username}?`)) return
    setError('')
    try { await api.delete(`/api/admin/delete/${encodeURIComponent(a.username)}`); setToast('Администратор удалён.'); refresh() }
    catch(err) { setError(errorMessage(err)) }
  }
  return <><PageHeading eyebrow="УПРАВЛЕНИЕ ДОСТУПОМ" title="Администраторы" description="Учётные записи с доступом к панели управления." actions={<Button onClick={() => setCreating(true)}><Plus size={17}/> Добавить администратора</Button>}/><ErrorBanner>{error}</ErrorBanner><div className="toolbar"><SearchBox value={search} onChange={setSearch} placeholder="Поиск администратора…"/><span className="toolbar-count">{data?.length ?? 0} аккаунтов</span></div><section className="panel table-panel">{loading ? <Loading/> : filtered.length ? <TableFrame><thead><tr><th>Аккаунт</th><th>Роль</th><th>Действия</th></tr></thead><tbody>{filtered.map(a => <tr key={a.username}><td><div className="user-cell"><span className="avatar admin-avatar"><ShieldCheck size={17}/></span><div><strong>{a.username}</strong><small className="cell-sub">{a.username === localStorage.getItem('mineshop_username') ? 'Текущий аккаунт' : 'Администратор'}</small></div></div></td><td><span className="status status-active">Администратор</span></td><td><Button variant="danger-ghost" onClick={() => remove(a)}><Trash2 size={15}/> Удалить</Button></td></tr>)}</tbody></TableFrame> : <Empty>Администраторы не найдены.</Empty>}</section>{toast && <div className="toast"><Check size={17}/>{toast}</div>}{creating && <Modal title="Новый администратор" onClose={() => setCreating(false)}><form className="modal-body" onSubmit={create}><Field label="Имя пользователя"><input required minLength="3" autoComplete="off" value={form.username} onChange={e => setForm({...form,username:e.target.value})} placeholder="new-admin"/></Field><Field label="Пароль"><input required minLength="8" autoComplete="new-password" type="password" value={form.password} onChange={e => setForm({...form,password:e.target.value})} placeholder="Не менее 8 символов"/></Field><p className="form-hint">Пароль отправляется backend по HTTPS в production; хранение хеша выполняет сервер.</p><div className="modal-actions"><Button variant="secondary" type="button" onClick={() => setCreating(false)}>Отмена</Button><Button disabled={busy}>{busy ? 'Создание…' : 'Создать администратора'}</Button></div></form></Modal>}</>
}

export default function App() {
  const [username, setUsername] = useState(() => localStorage.getItem('mineshop_username') || '')
  const [authenticated, setAuthenticated] = useState(() => Boolean(localStorage.getItem('mineshop_token')))
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [meError, setMeError] = useState('')
  const navigate = useNavigate()
  const location = useLocation()
  useEffect(() => {
    const expired = () => { setAuthenticated(false); setUsername(''); setMeError('Сессия истекла. Войдите снова.') }
    window.addEventListener('auth:expired', expired)
    return () => window.removeEventListener('auth:expired', expired)
  }, [])
  useEffect(() => {
    if (!authenticated) return
    api.get('/api/admin/auth/me').then(r => {
      const name = typeof r.data === 'string' ? r.data : r.data?.username
      if (name) { setUsername(name); localStorage.setItem('mineshop_username', name) }
    }).catch(() => {})
  }, [authenticated])
  async function logout() {
    const token = localStorage.getItem('mineshop_token')
    try { if (token) await api.post('/api/admin/auth/logout', null, { headers: { Authorization: `Bearer ${token}` } }) } catch {}
    localStorage.removeItem('mineshop_token'); localStorage.removeItem('mineshop_username')
    setAuthenticated(false); setUsername(''); navigate('/'); setSidebarOpen(false)
  }
  if (!authenticated) return <><Login onLogin={name => { setUsername(name); setAuthenticated(true); setMeError('') }}/>{meError && <div className="floating-error"><ErrorBanner>{meError}</ErrorBanner></div>}</>
  const pageTitle = navItems.find(i => i.to === location.pathname)?.label || (location.pathname.startsWith('/orders') ? 'Заказы' : 'Mineshop')
  return <div className="app-shell"><aside className={`sidebar ${sidebarOpen ? 'sidebar-open' : ''}`}><div className="sidebar-brand"><div className="brand-mark"><Command size={21}/></div><div><strong>Mineshop</strong><small>ADMIN CONSOLE</small></div><button className="mobile-close icon-btn" onClick={() => setSidebarOpen(false)}><X size={18}/></button></div><div className="workspace"><span className="workspace-dot"/><span>Основной магазин</span><span className="workspace-caret">⌄</span></div><div className="nav-label">РАБОЧАЯ ОБЛАСТЬ</div><nav className="side-nav">{navItems.map(item => <NavLink key={item.to} to={item.to} end={item.end} onClick={() => setSidebarOpen(false)} className={({isActive}) => `nav-item ${isActive ? 'nav-active' : ''}`}><item.icon size={18}/><span>{item.label}</span>{item.to === '/orders' && <span className="nav-arrow">↗</span>}</NavLink>)}</nav><div className="sidebar-bottom"><div className="server-status"><span className="server-dot"/><div><strong>API подключение</strong><small>Статус проверяется запросами</small></div></div><div className="sidebar-user"><span className="avatar">{String(username || 'A').slice(0,1).toUpperCase()}</span><div className="sidebar-user-name"><strong>{username || 'Администратор'}</strong><small>Администратор</small></div><button className="icon-btn" onClick={logout} title="Выйти"><LogOut size={17}/></button></div></div></aside>{sidebarOpen && <div className="sidebar-overlay" onClick={() => setSidebarOpen(false)}/>}<main className="main-area"><header className="topbar"><button className="menu-btn icon-btn" onClick={() => setSidebarOpen(true)}><Menu size={20}/></button><div className="breadcrumbs"><span>Панель управления</span><ChevronRight size={14}/><strong>{pageTitle}</strong></div><div className="topbar-right"><span className="topbar-date"><Clock3 size={15}/>{new Date().toLocaleDateString('ru-RU',{day:'2-digit',month:'short',year:'numeric'})}</span><span className="topbar-divider"/><div className="topbar-avatar">{String(username || 'A').slice(0,1).toUpperCase()}</div><div className="topbar-user"><strong>{username}</strong><small>Администратор</small></div><button className="icon-btn logout-top" onClick={logout} title="Выйти"><LogOut size={17}/></button></div></header><div className="page-content"><Routes><Route path="/" element={<Dashboard/>}/><Route path="/orders" element={<OrdersPage/>}/><Route path="/products" element={<ProductsPage/>}/><Route path="/users" element={<UsersPage/>}/><Route path="/admins" element={<AdminsPage/>}/><Route path="*" element={<Dashboard/>}/></Routes></div><footer className="app-footer"><span>MINESHOP ADMIN <span className="footer-separator">·</span> CONTROL PANEL</span><span>Java backend integration <span className="footer-separator">·</span> React + Axios</span></footer></main></div>
}
