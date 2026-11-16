# Own-account profile requirements

- Given an authenticated account without USERS READ, when GET /api/auth/profile is requested, return only the current authenticated user's public profile fields.
- Given a supplied user identifier in a query string, the endpoint must never use it to select another account. Unauthenticated requests are denied.
- Given the response, no password/hash/token/secret field is included.
- Given a complete profile, the account page displays real first/last names, username, email, one phone, address components, role and department names, active and verified state, creation/update times.
- Given missing optional data or a load failure, render explicit missing-data or retry states without inventing values.
- Given current access, render effective module/action permissions read-only and distinguish system-administrator status from legacy type USER.
- Given desktop or mobile layouts, reuse shared detail cards and grids; trailing fields fill their rows. Existing account action routes remain available for self service.

## Editing personal information

- Given an authenticated non-administrator, when submitting valid username, names, phone and address to PUT /api/auth/profile, update only their personal fields.
- Given any additional property, including roleId, departmentId, permissions, role, active, emailVerified, email, password or id, reject the entire request without changes.
- Given a concurrent administrator assignment change, self-profile updates must preserve the latest role, department and access grants.
- Given the own-profile form, render standard tabs/inputs/actions with no editable organization, state or permission controls; show load/retry/save failures and validate before submitting.
- Given successful saving, refresh the displayed username/profile and return to My profile without changing effective permissions.
- Given the own-profile screen, do not display the account UUID.
