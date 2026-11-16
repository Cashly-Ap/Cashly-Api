# Verification

- Backend: 23 focused test classes, 60 tests passed, zero failures/errors. Includes company media independence/fallback, aspect ratio and bounded height, actual custom image extraction from a generated PDF, generator configuration, endpoint permission coverage and ownership guard across all 18 PDF generators.
- Frontend: production Angular build passed; 5 company-service tests passed; 11 company browser tests passed, including dedicated PDF logo upload, preview, reload/removal and navbar isolation.
- Screenshot reviewed: cashly-app/output/playwright/company/pdf-logo.png. Existing shared form and file-input styling preserved.
- Independent static review found two additional cash-register PDF generators; both were corrected and re-reviewed. All 18 PDF generators now use shared branding. No remaining findings.
- Flyway V34 applied to the development database. Existing company/navigation logo remains independent.
- Implementation was verified before creating the requested commits.

## Safe fitting and transient preview follow-up

- Backend: 45 focused tests across 13 classes passed. Matrix covers 4000×20, 20×4000, 4096-square, opaque white padding, transparent padding, white/translucent art on transparency, full-bleed and blank images.
- Preview uses the real invoice renderer and same shared image fitting. Tests extract the cropped embedded image and verify no persistence writes and pending selection/removal fallback.
- Generated target/company-logo-preview.pdf rendered with pdftoppm and visually reviewed: single page, bounded logo, no overlap or clipping of document sections.
- Independent review caught possible removal of white art on transparency; corrected to preserve all visible alpha pixels whenever the image has transparency. Final review approved.
- At this verification stage, the original limits were 5 MB and 4096×4096 pixels. The automatic optimization follow-up below supersedes these limits for company logos.
- Frontend follow-up: Angular build and 6 company-service tests passed. Existing 11 company E2E tests and 2 preview E2E tests passed; preview checks pending upload/no mutation, close/URL revocation, loading/cancellation and validation recovery. Preview E2E additionally used the real generated PDF fixture. Native PDF iframe capture in Docker Chromium is blank; actual PDF layout was verified independently with pdftoppm. Shared modal includes accessible close label and iframe title.

## Automatic image optimization follow-up

- Removed original-file MB rejection from both company logo inputs, company media endpoints and multipart request configuration.
- Client optimization and server decode-subsampling/proportional normalization bound the longest stored image side to 2048 pixels, preserving transparency. Unsupported/malformed images remain rejected.
- Backend: 24 focused tests passed, including a valid original above 5 MB, dimensions above 4096, transparency, aspect ratio, extreme dimensions and identical preview/save normalization. Runtime health HTTP200 UP.
- Frontend: production build passed, 18 initial focused unit tests passed and all 14 company browser tests passed, including a 6 MB original accepted and sent reduced at 2048×1024. Independent static review approved stale-selection cancellation, clear/reset/destroy handling and save/preview processing guards.

## Default placeholder and delivery

When no company/document image exists, PDFs and transient preview display the literal text “Logotipo”, replacing the bundled default image. Configured company or document images continue to work. Changes are delivered as separate commits for server image optimization, document branding/preview, client image optimization, company configuration and login animation.
