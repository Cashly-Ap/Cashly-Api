# Authenticator MFA requirements

## Enrollment
- Given an authenticated user without MFA, when they confirm their password, then Cashly returns a time-limited enrollment secret and provisioning URI without enabling MFA yet.
- Given pending enrollment, when a valid current authenticator code is submitted before expiration, then MFA becomes active and recovery codes are returned once.
- Given invalid, expired or reused enrollment/code, when submitted, then enrollment remains inactive and no session is issued.
- Given enrollment data, when rendered in the browser, then QR generation does not disclose the secret to a third-party service.

## Login
- Given a user without MFA, when valid credentials are submitted, then the existing login succeeds.
- Given an MFA-enabled user, when valid credentials are submitted, then only a short-lived challenge is returned and neither access nor refresh tokens are issued.
- Given a valid challenge, when a fresh authenticator code or unused recovery code is submitted, then a session is issued and the challenge is consumed.
- Given an expired, consumed or repeatedly failed challenge, when submitted, then access is denied.
- Given a consumed TOTP timestep or recovery code, when submitted again, then access is denied, including concurrent requests.
- Given an inactive account, when completing a challenge, then no session is issued.

## Security settings
- Given an authenticated user, when opening Profile, then Configuration, Security and Logout appear on desktop and mobile.
- Given enabled MFA, when opening Security, then the enabled state and remaining recovery-code count appear.
- Given enabled MFA, when confirming the current password and a valid second factor, then MFA is disabled and previous refresh sessions are revoked.
- Given persisted enrollment, then secrets are encrypted, recovery codes are hashed and raw login challenge tokens are not persisted.
