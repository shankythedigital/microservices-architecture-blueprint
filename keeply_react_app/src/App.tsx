import type { ReactElement } from 'react'
import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/AuthContext'
import { MobileShell } from './components/MobileShell'
import { AddAssetManualPage } from './pages/AddAssetManualPage'
import { AddAssetMethodPage } from './pages/AddAssetMethodPage'
import { AddAssetScanPage } from './pages/AddAssetScanPage'
import { AccountPage } from './pages/AccountPage'
import { AlertsPage } from './pages/AlertsPage'
import { AssetDetailPage } from './pages/AssetDetailPage'
import { AssetsPage } from './pages/AssetsPage'
import { DashboardPage } from './pages/DashboardPage'
import { IssuesPage } from './pages/IssuesPage'
import { KnowledgePage } from './pages/KnowledgePage'
import { LoginPage } from './pages/LoginPage'
import { NewIssuePage } from './pages/NewIssuePage'
import { NotificationsPage } from './pages/NotificationsPage'
import { ProfilePage } from './pages/ProfilePage'
import { SettingsPage } from './pages/SettingsPage'
import { WelcomePage } from './pages/WelcomePage'

function RequireAuth({ children }: { children: ReactElement }) {
  const { token } = useAuth()
  const { pathname } = useLocation()
  if (!token) return <Navigate to="/login" replace state={{ from: pathname }} />
  return children
}

function HomeGate() {
  const { token } = useAuth()
  if (token) return <Navigate to="/home" replace />
  return <Navigate to="/welcome" replace />
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomeGate />} />
      <Route path="/welcome" element={<WelcomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/home"
        element={
          <RequireAuth>
            <MobileShell />
          </RequireAuth>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="assets" element={<AssetsPage />} />
        <Route path="assets/:id" element={<AssetDetailPage />} />
        <Route path="assets/add" element={<AddAssetMethodPage />} />
        <Route path="assets/add/scan" element={<AddAssetScanPage />} />
        <Route path="assets/add/manual" element={<AddAssetManualPage />} />
        <Route path="alerts" element={<AlertsPage />} />
        <Route path="account" element={<AccountPage />} />
        <Route path="account/profile" element={<ProfilePage />} />
        <Route path="account/settings" element={<SettingsPage />} />
        <Route path="account/notifications" element={<NotificationsPage />} />
        <Route path="tips" element={<KnowledgePage />} />
        <Route path="issues" element={<IssuesPage />} />
        <Route path="issues/new" element={<NewIssuePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}
