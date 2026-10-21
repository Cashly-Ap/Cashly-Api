# Design: Simple shortcuts and screen hints

## Decisions
- Reuse explicit action registrations and their visibility/disabled/permission/modal guards for dispatch and contextual help. This avoids divergence between advertised and executable actions.
- Use KeyboardEvent.code for N/F/B/G/M and Escape for cancel, with no modifiers. Ignore letters in inputs, textareas, selects and editable content; do not intercept native Ctrl/Meta/Alt combinations or composition/repeats/defaultPrevented events. Escape preserves open popup behavior and may cancel only its active form/dialog.
- Extend UserPreferences with showShortcutHints boolean. GET and PUT return four fields; PUT requires all four. This change is local/unpublished, so strict validation is selected over silently resetting an omitted preference.
- V33 adds show_shortcut_hints boolean not null default true; existing rows retain theme/color/shortcut choices and gain true. JPA reads and atomic upserts include the field. CurrentActor remains the sole identity source.
- Frontend defaults include true and cache schema advances to v2. Old caches are ignored; the account API restores the authoritative four-field preferences. Optimistic queued saves retain identity/epoch protections and rollback.
- The settings page adds a discoverable hints toggle. The shared page/dialog help preserves existing spacing/shadows and shows only available scope actions when both toggles are on.

## Files
Backend platform/auth/preferences domain, controller and JPA adapter/repository/entity; V33 migration; preference service/controller tests. Frontend core/preferences model/service/tests, core/shortcuts dispatcher/reference/help component, settings template, MainLayout integration and Playwright preferences tests.

## Verification
Backend tests first: defaults, saving false, required/non-null field and account identity. Run focused Maven and permission coverage/enforcement. Real MySQL/HTTP smoke verifies V33, default true, persisted false across sessions, ownership and disposable-account cleanup. Frontend units cover simple-key dispatch, editable/native safety, dynamic help availability and legacy cache normalization. Browser tests cover page/dialog help toggle and actual simple actions. Build in Docker.

## Rollout
No dependency or permission changes. Keep previous OpenSpec archive immutable. Publish no commits/PRs. Archive only after tests/build pass and the delta is merged into main specs.

## Contextual rendering
A coalesced MutationObserver refreshes the shared list using the same availability predicates as dispatch. Stable identities prevent self-render loops. Help is inline at the start of the workspace; Angular Renderer2 moves its managed host into active dialog content and restores its anchor on close. Native dialog Escape is marked as native-only before bubbling, so closing a dialog cannot also cancel the background form. Native close controls advertise Esc; unrendered PrimeNG dialog placeholders are ignored.
