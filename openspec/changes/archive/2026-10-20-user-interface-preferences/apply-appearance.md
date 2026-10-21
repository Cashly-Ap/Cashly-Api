# Appearance implementation progress

## Scope

Autonomous local work unit: appearance foundation and module/chart accents (tasks 2.1, 2.4, 2.5). No commits or pull requests requested. Implementation mode: Standard (no strict-TDD project configuration found); tests authored before corresponding service/palette changes.

## Implemented

- ThemeService delegates preference writes, resolves explicit/system mode, tracks live media-query changes, cleans listener up, preserves toggle/isDarkMode consumer API and applies browser color-scheme.
- Central module palette and URL-group resolver cover all eight navigation groups. Desktop/mobile menu entries inherit group identity from descendant routes. Workspace records active group.
- Informative-only CSS changes group/header/table/section/metric/chart accents; original dimensions, border widths, radii and shadows remain intact. Semantic status classes remain unchanged.
- Dashboard exposes explicit concept identities and computed category-color datasets; switching modes preserves values and avoids refetching data.

## Verification

- RED: module-appearance and ThemeService tests failed import resolution before new implementations existed.
- GREEN: ThemeService5, module appearance3, Dashboard5 pass in Docker Vitest.
- Build: `docker exec cashly-app-dev node node_modules/@angular/cli/bin/ng.js build` passed (existing CommonJS qrcode/stomp warnings).
- Layout25 passed after updating stale profile-nav expectations and adding group-inheritance/settings-route assertions. Combined appearance/layout suite38 passed.
- Browser probe validated light informative, dark informative and restoration to dark minimal. Module icons and metric tints changed as intended; computed radius28px and complete box-shadow string stayed identical across states. Light/dark screenshots inspected under output/playwright/preferences-appearance-{light,dark}.png. Full six-combination/responsive matrix remains integration verification responsibility.

## Visual correction

Informative tinted headers now inherit top-start/top-end corner radii through their Angular host from the existing card. Browser verified28px desktop and20px mobile for all three settings cards, with identical parent shadows and no overflow clipping. Responsive screenshots inspected; subsequent production build passed.
