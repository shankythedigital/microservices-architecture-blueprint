/** Matches helpdesk-service `RelatedService` enum. */
export type RelatedService =
  | 'AUTH_SERVICE'
  | 'NOTIFICATION_SERVICE'
  | 'ASSET_SERVICE'
  | 'HELPDESK_SERVICE'
  | 'UPCOMING_PROJECT'

export const HELP_DESK_RELATED_SERVICES: { value: RelatedService; label: string; hint: string }[] = [
  { value: 'ASSET_SERVICE', label: 'Assets', hint: 'Appliances, warranties, documents' },
  { value: 'AUTH_SERVICE', label: 'Sign-in & account', hint: 'Login, OTP, profile' },
  { value: 'NOTIFICATION_SERVICE', label: 'Notifications', hint: 'Alerts & messages' },
  { value: 'HELPDESK_SERVICE', label: 'Helpdesk', hint: 'Tickets & support' },
  { value: 'UPCOMING_PROJECT', label: 'Other', hint: 'General questions' },
]
