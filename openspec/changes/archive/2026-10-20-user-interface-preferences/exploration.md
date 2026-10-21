# Exploration: User interface preferences

The Angular application has a root signal-based ThemeService that resolves OS preference only initially and persists the resolved light/dark value in a global `theme` key. It needs a distinct persisted system preference and live media-query listener. Existing theme consumers, including dashboard chart computed data, use `isDarkMode`; retain that interface.

`core/layouts/config/menu.config.ts` defines role/module-filtered business groups. Empresa and Perfil already live exclusively in the user menu. Configuración must be a separate personal-preferences entry, not a new Plataforma item. Module shortcuts should reuse the filtered menu.

`core/forms/components/headers/form-actions/` centralizes save/cancel controls. Explicit registration there and in list/filter/dialog owners avoids unsafe button-text scanning. PrimeNG dialogs require scope-aware dispatch so background pages cannot execute actions.

Dashboard uses Chart.js via ng2-charts and computed palettes. Informative mode can extend these computed values plus shared navigation/section accent tokens, preserving existing card classes, shadows, geometry, and semantic states.

User confirmed account synchronization across devices. A self-only GET/PUT account API with user/company-owned persistence and a user-keyed startup cache is selected. Legacy global theme is migration input only, never an authority over an existing account preference.

OpenSpec is centralized in cashly-api even though the implementation spans both repositories. No config.yaml or relevant user-interface-preferences capability exists. These artifacts define the new capability locally; archive will promote the verified specification later. No commits, PRs, or unrelated documentation changes are included.
