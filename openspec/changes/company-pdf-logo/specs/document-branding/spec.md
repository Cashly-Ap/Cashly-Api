# Document branding

## Requirements

### Dedicated document logo
Given an authenticated user with COMPANY:UPDATE, when they edit the company, they can upload, preview, replace or remove a PNG/JPEG document logo. The selection persists across sessions and does not change the navigation logo. Existing image validation and authorization apply.

### All generated documents
Given a configured document logo, when any invoice, quote, order, purchase, payment, catalogue, inventory or financial report PDF is generated, its branding uses the configured image with preserved aspect ratio and bounded dimensions.

### Fallback
Given no document logo, when generating a PDF, use the company logo if available, otherwise the literal text “Logotipo”. Removing the document logo restores this fallback.

### Permissions
Given an authenticated user without COMPANY:UPDATE, document-logo mutation is forbidden. Reads use the existing authenticated company-media access policy.

### Automatic safe fitting
Given a very wide or tall logo, when generating a document, preserve the artwork aspect ratio and fit it within the bounded header without overlapping other content. Trim only empty transparent or near-white outer padding; never crop the actual lettering or symbol. Malformed or unsupported images produce a clear validation error. Large valid originals are resized automatically before storage, preserving proportions and transparency.

### Preview before saving
Given pending PDF-logo or company-logo changes (including removal), when the administrator clicks Preview PDF, render a sample document with the effective pending logo and the same fitting used by real documents. This operation never persists company data or files. Resolve pending selections first, then existing images unless marked for removal, then the literal text “Logotipo”. Show loading and recoverable error feedback; release preview URLs when closed or destroyed.

### Automatic optimization without an original-file MB cap
Given a valid PNG/JPEG original above the former 5 MB or 4096-pixel thresholds, when selecting either company logo, accept and optimize it automatically instead of showing a size rejection. Resize proportionally to a maximum 2048-pixel side and preserve transparency. Save and preview remain unavailable while optimization runs.

Given an original uploaded directly to the API, normalize using decode subsampling and proportional resizing without the former byte or dimension rejection; persist only the optimized image. Preview applies identical normalization without storage writes. Malformed/unsupported content remains rejected.
