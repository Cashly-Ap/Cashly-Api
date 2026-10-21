# Authenticator MFA design

Extend the existing authentication boundary and preserve existing token shape for successful sessions. A login requiring MFA returns a distinct challenge response which the Angular application must never save as authenticated state. A public verification endpoint consumes the challenge and issues tokens after checking account status.

Authenticated MFA endpoints expose status, password-protected enrollment, code confirmation and password-plus-factor disable. Store enrollment and challenge data separately from user profile fields. Use a dedicated environment encryption key, authenticated encryption, time-limited pending enrollment and challenges, bounded attempts and atomic single-use consumption. Revoke existing refresh tokens when enrollment changes.

Angular adds a lazy-loaded Security page and a second-factor step in the existing login page. Generate QR locally; allow manual secret entry. Show recovery codes only immediately after enrollment with an explicit save action. Preserve existing PrimeNG/Tailwind conventions and responsive navigation.

## Verification
Exercise standard TOTP vectors, encryption round trips and wrong-key failure; enrollment confirmation/expiry; challenge throttling, expiry, concurrency and no token issuance before verification; one-use recovery codes and active account checks. Run focused existing authentication regressions, production frontend compilation, browser login/security flows and mobile layout checks. Record actual results and any unavailable checks in verify-report.md.
