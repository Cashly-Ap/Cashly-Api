# Backend apply progress

Work unit: preference foundation, tasks 1.1–1.3. Local implementation only; no commits or PRs. Standard mode with tests written before implementation; no strict_tdd project configuration exists.

## Implementation

- Domain record validates supported theme/color values and supplies system/minimal/false defaults.
- Repository port and application service resolve the authenticated CurrentActor for every operation; no selectable account id exists.
- JPA adapter reads by UUID and performs one atomic native MySQL upsert, avoiding concurrent first-insert races.
- Self-only GET/PUT `/api/auth/preferences` uses explicit isAuthenticated authorization, Jakarta request validation and Cache-Control: no-store.
- V32 creates auth_user_preferences with user primary key/FK cascade, required installation-company FK/default, and theme/color constraints. UserEntity and existing response contracts are unchanged.

## Test evidence

| Stage | Evidence |
|---|---|
| RED | UserPreferencesServiceTest and UserPreferencesControllerTest written first; Docker Maven failed test compilation because preference classes did not exist |
| GREEN | Docker Maven selected tests completed exit 0; 4 service and 7 controller tests passed |
| Regression | EndpointPermissionCoverageTest and EndpointPermissionEnforcementTest passed in the same run |

Command: `docker exec cashly-api-app ./mvnw test -Dtest=UserPreferencesServiceTest,UserPreferencesControllerTest,EndpointPermissionCoverageTest,EndpointPermissionEnforcementTest -q`.

New tests cover defaults without writes, authenticated account isolation, update identity, domain validation, ordinary/admin self-service, anonymous denial, invalid/missing/null request values and no-store responses. The selected run totals 585 passing tests: service 4, controller 7, endpoint coverage 4, endpoint enforcement 570.

## Live integration and security verification

The resumed development watcher reloaded the API. Health returned 200 and Flyway recorded V32 success. Database metadata confirms the user primary key and both user/company foreign keys.

Using disposable operator accounts and actual HTTP requests verified:

- GET defaults returns system/minimal/false without inserting a row; Cache-Control is no-store.
- PUT persists preferences; a separate login session reads the saved values.
- A forged userId query cannot change the target account; another account retains defaults.
- Anonymous GET and PUT return 401.
- Invalid theme, color, null shortcuts and missing values return 400; persisted values stay intact.
- Two simultaneous first PUT requests return 200; the account has exactly one stored row with the installation company id.
- Disposable users, cascaded preferences, and issued refresh-token rows were deleted after verification; existing accounts and preferences were untouched.

Existing application-wide limitation observed: an authenticated request for the nonexistent `/api/auth/preferences/{id}` path returns 500 for NoResourceFoundException rather than 404. It exposes no preference data and is unrelated to the new endpoint contract. No production change was made for that existing error-handler behavior.

No deviation from design. Native SQL is intentionally contained in the JPA repository adapter to retain atomic MySQL upsert behavior.
