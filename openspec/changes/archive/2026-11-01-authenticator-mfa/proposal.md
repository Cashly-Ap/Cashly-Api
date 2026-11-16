# Authenticator MFA

## Intent
Allow existing Cashly users to protect password login with an authenticator application such as Microsoft Authenticator. The profile menu exposes Configuration, Security and Logout. Google login is explicitly excluded from this change.

## Scope
Authenticated enrollment with password confirmation, local QR/manual secret, first-code confirmation, encrypted TOTP secrets, hashed one-use recovery codes, optional MFA challenge at login, disable with password and second factor, and session revocation when security settings change.

## Risks and rollback
The encryption key must remain stable and private. Losing both the authenticator and recovery codes prevents self-service login. Preserve existing password-only behavior for users who have not enabled MFA. Roll back application behavior only after arranging recovery for enrolled users; do not discard enrollment data or the encryption key.
