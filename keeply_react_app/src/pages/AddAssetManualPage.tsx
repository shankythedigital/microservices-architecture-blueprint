import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { createAsset, createAssetWithDocument } from '../api/assetsApi'
import { listMakes, listModels, listSubCategories } from '../api/masterDataApi'
import { ApiError, apiJson } from '../api/http'
import { url } from '../config'
import type { ResponseWrapper } from '../api/types'

type OptionCard = { id: string; label: string }

type CategoryDto = {
  categoryId?: number
  categoryName?: string
  description?: string
}

function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const s = reader.result as string
      const comma = s.indexOf(',')
      resolve(comma >= 0 ? s.slice(comma + 1) : s)
    }
    reader.onerror = () => reject(reader.error ?? new Error('Could not read file'))
    reader.readAsDataURL(file)
  })
}

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

export function AddAssetManualPage() {
  const { token } = useAuth()
  const nav = useNavigate()
  const [categoryId, setCategoryId] = useState('')
  const [subCategoryId, setSubCategoryId] = useState('')
  const [categories, setCategories] = useState<{ id: string; label: string }[]>([])
  const [subCategories, setSubCategories] = useState<{ id: string; label: string; categoryId?: string }[]>([])
  const [makes, setMakes] = useState<{ id: string; label: string; subCategoryId?: string }[]>([])
  const [models, setModels] = useState<{ id: string; label: string; makeId?: string }[]>([])

  const [makeId, setMakeId] = useState('')
  const [modelId, setModelId] = useState('')
  const [brand, setBrand] = useState('') // fallback when master data empty
  const [model, setModel] = useState('') // fallback when master data empty
  const [serial, setSerial] = useState('')
  const [purchase, setPurchase] = useState('')
  const [invoiceFile, setInvoiceFile] = useState<File | null>(null)
  const [warrantyType, setWarrantyType] = useState<'MANUFACTURER' | 'EXTENDED' | 'AMC' | ''>('')
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  const [err, setErr] = useState<string | null>(null)

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
            }))
            .filter((r) => r.id),
        )
      } catch (e) {
        setErr(e instanceof ApiError ? e.message : 'Could not load categories')
      }
    })()
  }, [token])

  // reset cascading fields when parent changes
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
            return (
              <button
                key={opt.id}
                type="button"
                className={selected ? 'select-card is-selected' : 'select-card'}
                disabled={disabled}
                onClick={() => onPick(opt.id)}
              >
                <span>{opt.label}</span>
                {selected && <span className="select-card__check">✓</span>}
              </button>
            )
          })}
        </div>
      </div>
    )
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setErr(null)
    if (!token) {
      setErr('You are not signed in.')
      return
    }
    if (!categoryId || !subCategoryId) {
      setErr('Please select category and subcategory.')
      return
    }
    setBusy(true)
    try {
      const makeLabel =
        makes.find((m) => m.id === makeId)?.label || brand.trim()
      const modelLabel =
        models.find((m) => m.id === modelId)?.label || model.trim()
      const assetNameUdv = `${makeLabel} ${modelLabel}${serial.trim() ? ` (${serial.trim()})` : ''}`.trim()
      const payload = {
        assetNameUdv,
        serialNumber: serial.trim() || undefined,
        purchaseDate: purchase || undefined,
        categoryId: categoryId ? Number(categoryId) : undefined,
        subCategoryId: subCategoryId ? Number(subCategoryId) : undefined,
        makeId: makeId ? Number(makeId) : undefined,
        modelId: modelId ? Number(modelId) : undefined,
      }
      // userId / username omitted — asset-service fills from Bearer JWT
      const res = invoiceFile
        ? await createAssetWithDocument(
            token,
            {},
            payload,
            await fileToBase64(invoiceFile),
            docTypeFromFile(invoiceFile),
          )
        : await createAsset(token, {}, payload)
      setMessage(`Saved: ${res.data?.assetNameUdv ?? 'asset'}`)
      // Keep behavior non-disruptive: navigate to Assets list after save
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
      <p className="muted small">FR-13–17 — mirrors PDF manual path; invoice upload uses document API next.</p>
      <div className="sheet">
        <form onSubmit={onSubmit} className="stack">
          <SelectCards
            title="Category"
            value={categoryId}
            options={categories}
            onPick={setCategoryId}
            helper="No categories found"
          />

          <SelectCards
            title="Subcategory"
            value={subCategoryId}
            options={subCategories
              .filter((s) => !s.categoryId || s.categoryId === categoryId)
              .map((s) => ({ id: s.id, label: s.label }))}
            disabled={!categoryId}
            onPick={setSubCategoryId}
            helper={categoryId ? 'No subcategories found' : 'Select category first'}
          />

          <SelectCards
            title="Brand (Make)"
            value={makeId}
            options={makes
              .filter((m) => !m.subCategoryId || m.subCategoryId === subCategoryId)
              .map((m) => ({ id: m.id, label: m.label }))}
            disabled={!subCategoryId}
            onPick={setMakeId}
            helper={subCategoryId ? 'No makes found' : 'Select subcategory first'}
          />

          <SelectCards
            title="Model"
            value={modelId}
            options={models
              .filter((m) => (makeId ? m.makeId === makeId : false))
              .map((m) => ({ id: m.id, label: m.label }))}
            disabled={!makeId}
            onPick={setModelId}
            helper={makeId ? 'No models found' : 'Select make first'}
          />

          <SelectCards
            title="Warranty Type (optional)"
            value={warrantyType}
            options={[
              { id: 'MANUFACTURER', label: 'Manufacturer Warranty' },
              { id: 'EXTENDED', label: 'Extended Warranty' },
              { id: 'AMC', label: 'AMC Plan' },
            ]}
            onPick={(v) => setWarrantyType(v as 'MANUFACTURER' | 'EXTENDED' | 'AMC')}
            helper="Optional"
          />

          {makeId === '' && (
            <label className="field">
              <span>Brand (manual)</span>
              <input value={brand} onChange={(e) => setBrand(e.target.value)} required />
            </label>
          )}
          {modelId === '' && (
            <label className="field">
              <span>Model (manual)</span>
              <input value={model} onChange={(e) => setModel(e.target.value)} required />
            </label>
          )}
          <label className="field">
            <span>Serial number</span>
            <input value={serial} onChange={(e) => setSerial(e.target.value)} required />
          </label>
          <label className="field">
            <span>Purchase date</span>
            <input type="date" value={purchase} onChange={(e) => setPurchase(e.target.value)} />
          </label>
          <label className="field">
            <span>Invoice (optional)</span>
            <input
              type="file"
              accept="application/pdf,image/*"
              onChange={(e) => setInvoiceFile(e.target.files?.[0] ?? null)}
            />
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
