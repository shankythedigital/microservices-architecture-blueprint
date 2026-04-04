import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { getAssetById } from '../api/assetsApi'
import { ASSET_PHOTO_DOC_TYPE, uploadAssetDocument } from '../api/documentsApi'
import { ApiError } from '../api/http'
import type { AssetRecord } from '../api/assetsApi'
import { parseJwtPayload, tokenDisplayInfo } from '../auth/jwtClaims'
import { AuthenticatedDocImage } from '../components/AuthenticatedDocImage'
import { MediaEntityCard } from '../components/MediaEntityCard'
import { ResponsiveImage } from '../components/ResponsiveImage'

const MAX_PRODUCT_PHOTO_BYTES = 10 * 1024 * 1024

function resolveUploadUsername(
  token: string | null,
  profileUsername: string | undefined,
  uid: number | null,
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
  if (uid != null) return `user_${uid}`
  return 'user'
}

function userCanEditAssetPhoto(
  asset: AssetRecord,
  uid: number | null,
  token: string | null,
  profileUsername: string | undefined,
): boolean {
  if (uid == null || !token) return false
  if (asset.ownerUserId != null) return uid === asset.ownerUserId
  const createdBy = asset.createdByUsername?.trim().toLowerCase()
  if (!createdBy) return false
  const uname = resolveUploadUsername(token, profileUsername, uid).trim().toLowerCase()
  return uname === createdBy
}

function assetHasVisuals(a: AssetRecord, authToken: string | null): boolean {
  const hasDocSlot =
    !!authToken &&
    (a.warrantyDocumentId != null || a.amcDocumentId != null || a.assetPhotoDocumentId != null)
  const hasComponents = (a.components ?? []).length > 0
  return (
    Boolean(
      a.imageUrl ||
        a.categoryImageUrl ||
        a.subCategoryImageUrl ||
        a.makeImageUrl ||
        a.modelImageUrl ||
        a.vendorImageUrl ||
        a.outletImageUrl,
    ) ||
    hasDocSlot ||
    hasComponents
  )
}

export function AssetDetailPage() {
  const { id } = useParams()
  const { token, userId, profile } = useAuth()
  const effectiveUserId = userId ?? (token ? tokenDisplayInfo(token).userId : null)
  const [asset, setAsset] = useState<AssetRecord | null>(null)
  const [err, setErr] = useState<string | null>(null)
  const productPhotoInputRef = useRef<HTMLInputElement>(null)
  const [productPhotoBusy, setProductPhotoBusy] = useState(false)
  const [productPhotoErr, setProductPhotoErr] = useState<string | null>(null)

  const reloadAsset = useCallback(async () => {
    if (!token || !id) return
    const num = Number(id)
    if (!Number.isFinite(num)) return
    try {
      const res = await getAssetById(token, num)
      setAsset(res.data ?? null)
      setErr(null)
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : 'Not found')
    }
  }, [token, id])

  useEffect(() => {
    void reloadAsset()
  }, [reloadAsset])

  async function onProductPhotoSelected(file: File | null) {
    setProductPhotoErr(null)
    if (!file || !token || !id || !asset) return
    if (!userCanEditAssetPhoto(asset, effectiveUserId, token, profile?.username)) {
      setProductPhotoErr('Only the owner of this appliance can add or replace its photo.')
      return
    }
    const assetPk = asset.assetId ?? Number(id)
    if (!Number.isFinite(assetPk)) return
    if (!file.type.toLowerCase().startsWith('image/')) {
      setProductPhotoErr('Please choose an image file (JPEG, PNG, WebP, or GIF).')
      return
    }
    if (file.size > MAX_PRODUCT_PHOTO_BYTES) {
      setProductPhotoErr('Photo must be 10 MB or smaller.')
      return
    }
    const uid = effectiveUserId
    if (uid == null) {
      setProductPhotoErr('Could not determine your user id from the session.')
      return
    }
    const username = resolveUploadUsername(token, profile?.username, uid)
    setProductPhotoBusy(true)
    try {
      await uploadAssetDocument(token, {
        assetId: assetPk,
        userId: uid,
        username,
        file,
        docType: ASSET_PHOTO_DOC_TYPE,
      })
      await reloadAsset()
    } catch (e) {
      setProductPhotoErr(e instanceof ApiError ? e.message : 'Upload failed')
    } finally {
      setProductPhotoBusy(false)
      if (productPhotoInputRef.current) productPhotoInputRef.current.value = ''
    }
  }

  if (!id) return null

  const title = asset?.assetNameUdv || 'Appliance'
  const canEditAssetPhoto = asset ? userCanEditAssetPhoto(asset, effectiveUserId, token, profile?.username) : false

  return (
    <div className="page-pad">
      <Link to="/home/assets" className="back-link">
        ← Assets
      </Link>
      <h1>{title}</h1>
      {err && <p className="error-banner">{err}</p>}
      {!asset && !err && <p className="muted">Loading…</p>}
      {asset && (
        <>
          <section className="sheet">
            <h2>Photos &amp; references</h2>
            <p className="muted small" style={{ marginTop: 0 }}>
              Card layout scales from one column on phones to several on larger screens; images load lazily except the
              main asset photo.
            </p>
            {token && Number.isFinite(Number(asset.assetId ?? id)) && canEditAssetPhoto && (
                <div className="sheet profile-photo-actions asset-detail-product-photo" style={{ marginTop: '0.5rem' }}>
                  <input
                    ref={productPhotoInputRef}
                    type="file"
                    accept="image/jpeg,image/png,image/webp,image/gif"
                    className="visually-hidden"
                    tabIndex={-1}
                    onChange={(e) => void onProductPhotoSelected(e.target.files?.[0] ?? null)}
                  />
                  <div className="profile-photo-actions__row">
                    <button
                      type="button"
                      className="btn secondary"
                      disabled={productPhotoBusy}
                      onClick={() => productPhotoInputRef.current?.click()}
                    >
                      {productPhotoBusy
                        ? 'Uploading…'
                        : asset.assetPhotoDocumentId
                          ? 'Replace product photo'
                          : 'Add product photo'}
                    </button>
                    <span className="muted small">Your picture of this appliance · JPEG, PNG, WebP, or GIF · up to 10 MB</span>
                  </div>
                  {productPhotoErr && <p className="error-banner tight">{productPhotoErr}</p>}
                </div>
              )}
            <div className="media-entity-card-grid">
              {asset.imageUrl && (
                <MediaEntityCard
                  badge="Asset"
                  title={title}
                  media={
                    <ResponsiveImage
                      src={asset.imageUrl}
                      alt={`${title} — asset photo`}
                      className="media-entity-card__img"
                      priority
                    />
                  }
                />
              )}
              {asset.categoryImageUrl && (
                <MediaEntityCard
                  badge="Category"
                  title={asset.categoryName ?? 'Category'}
                  media={
                    <ResponsiveImage
                      src={asset.categoryImageUrl}
                      alt={`${title} — category`}
                      className="media-entity-card__img"
                    />
                  }
                />
              )}
              {asset.subCategoryImageUrl && (
                <MediaEntityCard
                  badge="Subcategory"
                  title={asset.subCategoryName ?? 'Subcategory'}
                  media={
                    <ResponsiveImage
                      src={asset.subCategoryImageUrl}
                      alt={`${title} — subcategory`}
                      className="media-entity-card__img"
                    />
                  }
                />
              )}
              {asset.makeImageUrl && (
                <MediaEntityCard
                  badge="Brand"
                  title={asset.makeName ?? 'Brand'}
                  media={
                    <ResponsiveImage
                      src={asset.makeImageUrl}
                      alt={`${title} — brand`}
                      className="media-entity-card__img"
                    />
                  }
                />
              )}
              {asset.modelImageUrl && (
                <MediaEntityCard
                  badge="Product"
                  title={asset.modelName ?? 'Model'}
                  subtitle="Catalog model / SKU image"
                  media={
                    <ResponsiveImage
                      src={asset.modelImageUrl}
                      alt={`${title} — product`}
                      className="media-entity-card__img"
                    />
                  }
                />
              )}
              {asset.vendorImageUrl && (
                <MediaEntityCard
                  badge="Vendor"
                  title={asset.vendorName ?? 'Vendor'}
                  media={
                    <ResponsiveImage
                      src={asset.vendorImageUrl}
                      alt={`${title} — vendor`}
                      className="media-entity-card__img"
                    />
                  }
                />
              )}
              {asset.outletImageUrl && (
                <MediaEntityCard
                  badge="Outlet"
                  title={asset.outletName ?? 'Outlet'}
                  media={
                    <ResponsiveImage
                      src={asset.outletImageUrl}
                      alt={`${title} — outlet`}
                      className="media-entity-card__img"
                    />
                  }
                />
              )}
              {asset.assetPhotoDocumentId != null && token && (
                <MediaEntityCard
                  key={`asset-photo-${asset.assetPhotoDocumentId}`}
                  badge="Your photo"
                  title="Appliance photo"
                  subtitle="Your picture of this appliance"
                  media={
                    <AuthenticatedDocImage
                      token={token}
                      documentId={asset.assetPhotoDocumentId}
                      docTypeHint="asset_photo"
                      alt={`${title} — your photo`}
                      className="media-entity-card__img"
                    />
                  }
                />
              )}
              {asset.warrantyDocumentId != null && token && (
                <MediaEntityCard
                  badge="Warranty"
                  title="Linked document"
                  subtitle="Preview when the file is an image"
                  media={
                    <AuthenticatedDocImage
                      token={token}
                      documentId={asset.warrantyDocumentId}
                      docTypeHint={asset.warrantyDocumentType ?? undefined}
                      alt="Warranty document preview"
                      className="media-entity-card__img"
                    />
                  }
                />
              )}
              {asset.amcDocumentId != null && token && (
                <MediaEntityCard
                  badge="AMC"
                  title="Linked document"
                  subtitle="Preview when the file is an image"
                  media={
                    <AuthenticatedDocImage
                      token={token}
                      documentId={asset.amcDocumentId}
                      docTypeHint={asset.amcDocumentType ?? undefined}
                      alt="AMC document preview"
                      className="media-entity-card__img"
                    />
                  }
                />
              )}
              {(asset.components ?? []).map((c) => (
                <MediaEntityCard
                  badge="Component"
                  title={c.componentName ?? `ID ${c.componentId ?? '—'}`}
                  key={c.componentId ?? c.componentName}
                  media={
                    c.imageUrl ? (
                      <ResponsiveImage
                        src={c.imageUrl}
                        alt={`${title} — ${c.componentName ?? 'component'}`}
                        className="media-entity-card__img"
                      />
                    ) : (
                      <div className="media-entity-card__placeholder" aria-hidden>
                        <svg viewBox="0 0 48 48" width="40" height="40" fill="none" stroke="currentColor" strokeWidth="2">
                          <rect x="8" y="12" width="32" height="24" rx="2" />
                          <path d="M16 22h16M20 16h8" />
                        </svg>
                      </div>
                    )
                  }
                />
              ))}
            </div>
            {!assetHasVisuals(asset, token) && (
              <p className="muted small" style={{ marginBottom: 0 }}>
                {canEditAssetPhoto
                  ? 'No catalog or warranty thumbnails yet—add your own product photo above, or they may appear when master data includes images.'
                  : 'No images yet. Only the appliance owner can add a product photo; catalog thumbnails may appear when master data includes images.'}
              </p>
            )}
          </section>
          <section className="sheet">
            <h2>Details</h2>
            <dl className="dl-grid">
              <dt>Status</dt>
              <dd>{asset.assetStatus ?? '—'}</dd>
              <dt>Category</dt>
              <dd>{asset.categoryName ?? '—'}</dd>
              <dt>Subcategory</dt>
              <dd>{asset.subCategoryName ?? '—'}</dd>
              <dt>Brand</dt>
              <dd>{asset.makeName ?? '—'}</dd>
              <dt>Model</dt>
              <dd>{asset.modelName ?? '—'}</dd>
              {asset.vendorName && (
                <>
                  <dt>Vendor</dt>
                  <dd>{asset.vendorName}</dd>
                </>
              )}
              {asset.outletName && (
                <>
                  <dt>Outlet</dt>
                  <dd>{asset.outletName}</dd>
                </>
              )}
            </dl>
          </section>
          <section className="sheet warranty-card" style={{ marginTop: '0.85rem' }}>
            <h2>Warranty &amp; care</h2>
            <p className="muted small">
              Renewal and reminders use your asset and notification data. Browse self-service help or open the support
              hub.
            </p>
            <div className="asset-detail-actions-row">
              <Link to="/home/tips" className="btn secondary tight">
                Tips &amp; knowledge
              </Link>
              <Link to="/home/helpdesk" className="btn ghost tight">
                Help &amp; support
              </Link>
            </div>
          </section>
          <p>
            <Link to={`/home/issues/new?assetId=${id}`} className="btn keeply-submit block" style={{ textDecoration: 'none' }}>
              Raise a service issue
            </Link>
          </p>
        </>
      )}
    </div>
  )
}
