import { Link } from 'react-router-dom'
import { NotificationInbox } from '../components/NotificationInbox'

export function NotificationsPage() {
  return (
    <div className="page-pad">
      <Link to="/home/account" className="back-link">
        ← Account
      </Link>
      <h1>Notifications</h1>
      <NotificationInbox />
    </div>
  )
}
