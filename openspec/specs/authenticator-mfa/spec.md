# Authenticator MFA

Users may optionally protect password login with six-digit, 30-second TOTP codes. Compatible authenticator applications use RFC 6238 SHA-1 with one timestep of clock tolerance.

- Profile navigation exposes Configuration, Security and Logout on desktop and mobile.
- Enrollment requires the current password. A pending secret expires after ten minutes; MFA is activated only after a valid authenticator code is confirmed.
- The browser generates QR codes locally and supports manual secret entry. Recovery codes are displayed once and may be saved by the user.
- Enabled accounts receive only a five-minute opaque challenge after password validation. No access or refresh token is issued until a valid factor is supplied.
- Challenges, accepted TOTP steps and recovery codes cannot be reused, including concurrent requests. A new password login replaces the prior challenge without resetting failure limits.
- Five failed MFA or password-reauthentication attempts lock MFA verification for fifteen minutes. Failure counters persist despite authentication exceptions.
- Settings endpoints require an authenticated account. Public verification requires an unexpired challenge, a valid factor and an active account.
- Disable requires the current password plus an authenticator or recovery code. Enable/disable revoke existing refresh tokens; issued access JWTs retain their expiration.
- Secrets are encrypted using a dedicated stable encryption key. Only hashes of recovery codes and login challenges are persisted.

Implementation and verification history: `openspec/changes/authenticator-mfa/`.
