# Design

Extend the single installation company with a nullable document-image storage path in a new Flyway migration. Reuse validated raster storage and transaction-safe lifecycle handling. Add /api/company/pdf-logo GET/POST/DELETE, with multipart file on POST and COMPANY:UPDATE for writes.

Resolve image bytes through an injected shared PdfBranding service for every generator. Return a fresh iText Div containing a proportionally bounded Image or the literal text “Logotipo” for each document. Resolve dedicated PDF image, existing company image, literal text placeholder “Logotipo” in that order.

Reuse the shared Angular file input, company service and authenticated blob/object-URL lifecycle. Save company identity and logo changes through the existing form, preserving established loading, error and permission behavior. Add focused storage/resolution/generator and frontend service/browser verification.

Follow-up: trim empty outer image padding in the shared document-branding renderer so every PDF uses identical fitting, including previously saved logos. Extract reusable validated image normalization to allow transient multipart preview without file-storage writes. Provide permission-protected POST /api/company/pdf-logo/preview returning no-store application/pdf, accepting optional file/companyLogo and removePdfLogo/removeLogo flags. Use a real representative commercial document renderer for the sample. Angular reuses the shared PDF modal and keeps preview state independent of saving.

Automatic optimization follow-up: remove original-file byte caps from company image endpoints and multipart settings; share server-side normalization with bounded decode subsampling and a 2048-pixel longest side. Add opt-in client optimization to the shared file input and enable it for both company logos. PrimeNG size validation is disabled in this mode; the chosen file and preview use optimized bytes. Expose processing state to prevent save/preview races and retain malformed-image feedback.
