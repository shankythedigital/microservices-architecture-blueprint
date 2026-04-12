import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { acknowledgeInsecureHttpIfNeeded } from './insecureApiGate'

const rootEl = document.getElementById('root')!

if (!acknowledgeInsecureHttpIfNeeded()) {
  rootEl.innerHTML =
    '<p style="font-family:system-ui,sans-serif;padding:1.5rem;max-width:28rem;margin:2rem auto;line-height:1.5;">You chose not to continue. This build talks to APIs over HTTP. Reload the page if you want to accept the warning.</p>'
} else {
  createRoot(rootEl).render(
    <StrictMode>
      <App />
    </StrictMode>,
  )
}
