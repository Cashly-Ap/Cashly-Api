# Authenticator MFA verification

## Scope
MFA only, as explicitly confirmed by the user. No Google identity integration.

## Completed checks
- Backend: 64 focused authentication, refresh, controller, authorization coverage, cryptography and MFA tests passed across scoped Docker runs, including three real MySQL transaction/concurrency tests in the dedicated `mfa_verification` schema.
- Frontend: 120 focused authentication, login, layout, route, interceptor and Security component tests passed in Docker. After the final lockout-message improvement, the 43 affected tests passed, including two new regression tests. Production Angular build passed before that message-only improvement.
- Browser: 10 Chromium scenarios passed in Docker covering challenge login, recovery, invalid/expired verification, desktop/mobile Security navigation, enrollment QR/recovery presentation and disable. Mobile layout checked for horizontal overflow.
- Manual visual review: clean desktop/mobile Security and mobile MFA login screenshots reviewed. Screenshots are under `cashly-app/output/playwright/mfa/` in the neighboring frontend checkout.
- Independent security reviews found no blocking bypass/replay issue. A refresh/enrollment race identified during implementation was addressed by locking the MFA row before a locking read of the refresh token.

## Operational configuration
`MFA_ENCRYPTION_KEY` is a stable Base64-encoded 32-byte AES key. Compose passes it to the API; the development environment key lives only in the ignored `.env`. Never commit the key. Preserve it with backups of enrollment data. See `docs/SETUP.md` for generation and operation instructions.

## Limits
Enabling/disabling MFA revokes existing refresh tokens. Previously issued access JWTs retain their existing expiration. Existing token storage architecture is unchanged. Browser scenarios mock API responses; the dedicated MySQL tests validate actual failure persistence and single-use concurrency separately. A full repository-wide test suite was not run.

## Final integration
Live HTTP smoke passed against the running API and MySQL after V28 migration and encryption-key configuration: anonymous settings denied; public verification reaches challenge validation; password-confirmed enrollment; TOTP confirmation; previous refresh revocation; no access token before MFA; rejection of enrollment-code replay; recovery login; challenge consumption; recovery reuse denied; concurrent verification permits exactly one session; disable and refresh revocation; password-only login restored; five separate failed requests persist and prevent replacement challenges. The disposable test user and its MFA/refresh records were removed.

The first smoke run discovered that the endpoint permission coverage interceptor rejected MFA handlers. Explicit `isAuthenticated()` method guards were added to management endpoints, and `permitAll()` to the challenge verification endpoint, with a regression test covering all five mappings. The complete smoke then passed.
