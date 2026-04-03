import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { parseJwtPayload, tokenDisplayInfo } from '../auth/jwtClaims'
import { createAssetComplete } from '../api/assetsApi'
import { DEFAULT_PROJECT_TYPE } from '../constants/project'
import { listMakes, listModels, listSubCategories } from '../api/masterDataApi'
import { ApiError, apiJson } from '../api/http'
import { url } from '../config'
import type { ResponseWrapper } from '../api/types'
import type { CategoryDto } from '../api/categoriesApi'
import { ResponsiveImage } from '../components/ResponsiveImage'

type OptionCard = { id: string; label: string; imageUrl?: string; icon?: string }

const MAX_INVOICE_BYTES = 10 * 1024 * 1024
const ASSET_NAME_MAX = 255
const SERIAL_MAX = 120

/** docType must match document_type_master.code (asset-service), e.g. pdf, jpeg, png */
function docTypeFromFile(file: File): string {
  const mime = file.type.toLowerCase()
  if (mime === 'application/pdf') return 'pdf'
  if (mime === 'image/jpeg') return 'jpeg'
  if (mime === 'image/png') return 'png'
  if (mime === 'image/gif') return 'gif'
  if (mime === 'image/webp') return 'webp'
  const ext = file.name.split('.').pop()?.toLowerCase()
  if (ext && /^[a-z0-9]+$/.test(ext)) return ext
  return 'pdf'
}

function allowedInvoiceMime(file: File): boolean {
  const mime = file.type.toLowerCase()
  if (mime === 'application/pdf') return true
  return mime.startsWith('image/')
}

function appendOptional(fd: FormData, key: string, value: string | number | undefined | null) {
  if (value === undefined || value === null) return
  const s = typeof value === 'string' ? value.trim() : String(value)
  if (s === '') return
  fd.append(key, s)
}

function resolveUsername(
  token: string | null,
  profileUsername: string | undefined,
  userId: number | null,
): string {
  const fromProfile = profileUsername?.trim()
  if (fromProfile) return fromProfile
  const fromJwt = token ? tokenDisplayInfo(token).username?.trim() : ''
  if (fromJwt) return fromJwt
  if (token) {
    const p = parseJwtPayload(token)
    const pref = p?.preferred_username
    if (typeof pref === 'string' && pref.trim()) return pref.trim()
  }
  if (userId != null) return `user_${userId}`
  return ''
}

export function AddAssetManualPage() {
  const { token, userId, profile } = useAuth()
  const nav = useNavigate()
  const [categoryId, setCategoryId] = useState('')
  const [subCategoryId, setSubCategoryId] = useState('')
  const [categories, setCategories] = useState<
    { id: string; label: string; imageUrl?: string }[]
  >([])
  const [subCategories, setSubCategories] = useState<
    { id: string; label: string; categoryId?: string; imageUrl?: string }[]
  >([])
  const [makes, setMakes] = useState<
    { id: string; label: string; subCategoryId?: string; imageUrl?: string }[]
  >([])
  const [models, setModels] = useState<
    { id: string; label: string; makeId?: string; imageUrl?: string }[]
  >([])

  const [makeId, setMakeId] = useState('')
  const [modelId, setModelId] = useState('')
  const [serial, setSerial] = useState('')
  const [warrantyStart, setWarrantyStart] = useState('')
  const [warrantyEnd, setWarrantyEnd] = useState('')
  const [assetDisplayName, setAssetDisplayName] = useState('')
  const [warrantyProvider, setWarrantyProvider] = useState('')
  const [invoiceFile, setInvoiceFile] = useState<File | null>(null)
  const [warrantyType, setWarrantyType] = useState<'MANUFACTURER' | 'EXTENDED' | 'AMC' | ''>('')
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  const [err, setErr] = useState<string | null>(null)

  const suggestedName = useMemo(() => {
    const makeLabel = makes.find((m) => m.id === makeId)?.label ?? ''
    const modelLabel = models.find((m) => m.id === modelId)?.label ?? ''
    const base = `${makeLabel} ${modelLabel}`.trim()
    if (!base && !serial.trim()) return ''
    return serial.trim() ? `${base} (${serial.trim()})`.trim() : base
  }, [makeId, modelId, serial, makes, models])

  useEffect(() => {
    if (!token) return
    ;(async () => {
      try {
        const res = await apiJson<ResponseWrapper<CategoryDto[]>>(
          url('asset', '/api/asset/v1/categories'),
          { token },
        )
        const rows =
          res.data?.map((c) => ({
            id: String(c.categoryId ?? ''),
            label: c.categoryName ?? 'Category',
            imageUrl: c.imageUrl?.trim() || undefined,
          })) ?? []
        setCategories(rows.filter((r) => r.id))

        const [sc, mk, md] = await Promise.all([
          listSubCategories(token),
          listMakes(token),
          listModels(token),
        ])
        setSubCategories(
          (sc.data ?? [])
            .map((s) => ({
              id: String(s.subCategoryId ?? ''),
              label: s.subCategoryName ?? 'Subcategory',
              categoryId: s.category?.categoryId != null ? String(s.category.categoryId) : undefined,
              imageUrl: s.imageUrl?.trim() || undefined,
            }))
            .filter((r) => r.id),
        )
        setMakes(
          (mk.data ?? [])
            .map((m) => ({
              id: String(m.makeId ?? ''),
              label: m.makeName ?? 'Make',
              subCategoryId:
                m.subCategory?.subCategoryId != null ? String(m.subCategory.subCategoryId) : undefined,
              imageUrl: m.imageUrl?.trim() || undefined,
            }))
            .filter((r) => r.id),
        )
        setModels(
          (md.data ?? [])
            .map((m) => ({
              id: String(m.modelId ?? ''),
              label: m.modelName ?? 'Model',
              makeId:
                m.make?.makeId != null
                  ? String(m.make.makeId)
                  : m.makeId != null
                    ? String(m.makeId)
                    : undefined,
              imageUrl: m.imageUrl?.trim() || undefined,
            }))
            .filter((r) => r.id),
        )
      } catch (e) {
        setErr(e instanceof ApiError ? e.message : 'Could not load categories')
      }
    })()
  }, [token])

  useEffect(() => {
    setSubCategoryId('')
    setMakeId('')
    setModelId('')
  }, [categoryId])

  useEffect(() => {
    setMakeId('')
    setModelId('')
  }, [subCategoryId])

  useEffect(() => {
    setModelId('')
  }, [makeId])

  function SelectCards({
    title,
    value,
    options,
    disabled,
    onPick,
    helper,
  }: {
    title: string
    value: string
    options: OptionCard[]
    disabled?: boolean
    onPick: (id: string) => void
    helper?: string
  }) {
    return (
      <div className="field">
        <span>{title}</span>
        <div className={disabled ? 'select-card-grid is-disabled' : 'select-card-grid'}>
          {options.length === 0 && (
            <div className="select-card-placeholder muted small">{helper || 'No options'}</div>
          )}
          {options.map((opt) => {
            const selected = opt.id === value
            const thumb = opt.imageUrl?.trim()
            const icon = opt.icon?.trim()
            const rich = Boolean(thumb || icon)
            return (
              <button
                key={opt.id}
                type="button"
                className={[
                  'select-card',
                  rich ? 'select-card--thumb' : '',
                  selected ? 'is-selected' : '',
                ]
                  .filter(Boolean)
                  .join(' ')}
                disabled={disabled}
                onClick={() => onPick(opt.id)}
              >
                {thumb ? (
                  <ResponsiveImage src={thumb} alt="" className="select-card__visual" />
                ) : icon ? (
                  <div className="select-card__plan-strip" aria-hidden>
                    {icon}
                  </div>
                ) : null}
                {rich ? (
                  <span className="select-card__row">
                    <span>{opt.label}</span>
                    {selected && <span className="select-card__check">✓</span>}
                  </span>
                ) : (
                  <>
                    <span>{opt.label}</span>
                    {selected && <span className="select-card__check">✓</span>}
                  </>
                )}
              </button>
            )
          })}
        </div>
      </div>
    )
  }

  function validate(): string | null {
    if (!categoryId) return 'Please select a category.'
    if (!subCategoryId) return 'Please select a subcategory.'
    if (!makeId) return 'Please select a brand (make).'
    if (!modelId) return 'Please select a model. Full registration requires a catalog model ID.'
    const uid = userId != null && Number.isFinite(Number(userId)) ? Number(userId) : null
    if (uid == null) return 'Your account ID is missing — sign out and sign in again, then retry.'
    const name = (assetDisplayName.trim() || suggestedName).trim()
    if (name.length < 2) return 'Enter a display name (at least 2 characters), or finish selecting model and serial.'
    if (name.length > ASSET_NAME_MAX) return `Display name must be at most ${ASSET_NAME_MAX} characters.`
    const serialTrim = serial.trim()
    if (!serialTrim) return 'Serial number is required.'
    if (serialTrim.length > SERIAL_MAX) return `Serial number must be at most ${SERIAL_MAX} characters.`
    if (!warrantyStart) return 'Warranty start date (purchase / installation) is required.'
    if (!warrantyEnd) return 'Warranty end date is required.'
    const start = new Date(`${warrantyStart}T12:00:00`)
    const end = new Date(`${warrantyEnd}T12:00:00`)
    if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) return 'One of the warranty dates is invalid.'
    if (end.getTime() <= start.getTime()) return 'Warranty end date must be after the start date.'
    if (!invoiceFile) return 'Purchase invoice or proof document is required for full registration.'
    if (!allowedInvoiceMime(invoiceFile)) return 'Invoice must be a PDF or an image (JPEG, PNG, GIF, or WebP).'
    if (invoiceFile.size > MAX_INVOICE_BYTES) return 'Invoice file is too large (maximum 10 MB).'
    const uname = resolveUsername(token ?? null, profile?.username, uid)
    if (!uname) return 'Username could not be determined — update your profile or sign in again.'
    return null
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setErr(null)
    setMessage(null)
    if (!token) {
      setErr('You are not signed in.')
      return
    }
    const validationErr = validate()
    if (validationErr) {
      setErr(validationErr)
      return
    }
    const uid = Number(userId)
    const username = resolveUsername(token, profile?.username, uid)
    const assetNameUdv = (assetDisplayName.trim() || suggestedName).trim()

    setBusy(true)
    try {
      const fd = new FormData()
      fd.set('userId', String(uid))
      fd.set('username', username)
      fd.set('projectType', DEFAULT_PROJECT_TYPE)
      fd.set('assetNameUdv', assetNameUdv)
      fd.set('modelId', modelId)
      fd.set('serialNumber', serial.trim())
      fd.set('warrantyStartDate', warrantyStart)
      fd.set('warrantyEndDate', warrantyEnd)
      fd.set('targetUserId', String(uid))
      appendOptional(fd, 'targetUsername', username)
      appendOptional(fd, 'categoryId', Number(categoryId))
      appendOptional(fd, 'subCategoryId', Number(subCategoryId))
      appendOptional(fd, 'makeId', Number(makeId))
      appendOptional(fd, 'warrantyProvider', warrantyProvider)
      if (warrantyType) fd.set('warrantyStatus', warrantyType)
      fd.set('document', invoiceFile!)
      fd.set('docType', docTypeFromFile(invoiceFile!))

      const res = await createAssetComplete(token, fd)
      const data = res.data as { assetNameUdv?: string } | undefined
      setMessage(`Saved: ${data?.assetNameUdv ?? res.message ?? 'asset'}`)
      nav('/home/assets')
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : 'Could not save asset')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page-pad">
      <Link to="/home/assets/add" className="back-link">
        ← Back
      </Link>
      <h1>Manual entry</h1>
      <p className="muted small">
        Registers the appliance in one step (asset, warranty, proof document, and assignment to your account). You need
        catalog category → brand → model, warranty dates, serial number, and an invoice or image of proof.
      </p>
      <div className="sheet">
        <form onSubmit={onSubmit} className="stack" noValidate>
          <SelectCards
            title="Category"
            value={categoryId}
            options={categories.map((c) => ({
              id: c.id,
              label: c.label,
              imageUrl: c.imageUrl,
            }))}
            onPick={setCategoryId}
            helper="No categories found"
          />

          <SelectCards
            title="Subcategory"
            value={subCategoryId}
            options={subCategories
              .filter((s) => !s.categoryId || s.categoryId === categoryId)
              .map((s) => ({ id: s.id, label: s.label, imageUrl: s.imageUrl }))}
            disabled={!categoryId}
            onPick={setSubCategoryId}
            helper={categoryId ? 'No subcategories found' : 'Select category first'}
          />

          <SelectCards
            title="Brand (Make)"
            value={makeId}
            options={makes
              .filter((m) => !m.subCategoryId || m.subCategoryId === subCategoryId)
              .map((m) => ({ id: m.id, label: m.label, imageUrl: m.imageUrl }))}
            disabled={!subCategoryId}
            onPick={setMakeId}
            helper={subCategoryId ? 'No makes found' : 'Select subcategory first'}
          />

          <SelectCards
            title="Model (product)"
            value={modelId}
            options={models
              .filter((m) => (makeId ? m.makeId === makeId : false))
              .map((m) => ({ id: m.id, label: m.label, imageUrl: m.imageUrl }))}
            disabled={!makeId}
            onPick={setModelId}
            helper={makeId ? 'No models found' : 'Select make first'}
          />

          <label className="field">
            <span>Display name</span>
            <input
              value={assetDisplayName}
              onChange={(e) => setAssetDisplayName(e.target.value)}
              placeholder={suggestedName || 'Shown in your appliance list'}
              maxLength={ASSET_NAME_MAX}
              autoComplete="off"
            />
            <span className="muted small">
              {suggestedName
                ? `If you leave this blank, we use: ${suggestedName}`
                : 'Select make and model (and serial) to suggest a name, or type your own.'}
            </span>
          </label>

          <label className="field">
            <span>Serial number</span>
            <input
              value={serial}
              onChange={(e) => setSerial(e.target.value)}
              required
              maxLength={SERIAL_MAX}
              autoComplete="off"
            />
          </label>

          <label className="field">
            <span>Warranty start (purchase / installation)</span>
            <input type="date" value={warrantyStart} onChange={(e) => setWarrantyStart(e.target.value)} required />
          </label>

          <label className="field">
            <span>Warranty end</span>
            <input type="date" value={warrantyEnd} onChange={(e) => setWarrantyEnd(e.target.value)} required />
          </label>

          <label className="field">
            <span>Warranty provider (optional)</span>
            <input
              value={warrantyProvider}
              onChange={(e) => setWarrantyProvider(e.target.value)}
              placeholder="e.g. Manufacturer, retailer"
              maxLength={200}
            />
          </label>

          <SelectCards
            title="Warranty type (optional)"
            value={warrantyType}
            options={[
              { id: 'MANUFACTURER', label: 'Manufacturer', icon: '🛡' },
              { id: 'EXTENDED', label: 'Extended', icon: '📄' },
              { id: 'AMC', label: 'AMC', icon: '🔧' },
            ]}
            onPick={(v) => setWarrantyType(v as 'MANUFACTURER' | 'EXTENDED' | 'AMC')}
            helper="Optional — sent as warranty status when selected"
          />

          <label className="field">
            <span>Invoice or proof (required)</span>
            <input
              type="file"
              accept="application/pdf,image/jpeg,image/png,image/gif,image/webp"
              required
              onChange={(e) => setInvoiceFile(e.target.files?.[0] ?? null)}
            />
            <span className="muted small">PDF or image, up to 10 MB.</span>
          </label>
          {err && <p className="error-banner">{err}</p>}
          {message && <p className="ok-banner">{message}</p>}
          <button type="submit" className="btn keeply-submit" disabled={busy}>
            {busy ? 'Saving…' : 'Save asset'}
          </button>
        </form>
      </div>
    </div>
  )
}
