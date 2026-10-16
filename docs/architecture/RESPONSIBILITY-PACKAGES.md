# Responsibility package structure

This document is generated from the actual Java source inventory. Module and submodule ownership stays intact; leaf folders identify a class responsibility.

| Layer | Responsibility folders |
| --- | --- |
| application | Operations contain use_case, request, response, services, validation or model as needed; contracts stay in ports/in and ports/out. |
| domain | entity, repository, valueobjects, services and exceptions. |
| infrastructure | web, config, integration, pdf, excel and persistence subfolders identify the implementation technology/responsibility. |
| tests | Class tests mirror the production package; architecture, integration_tests and fixtures identify cross-cutting test responsibilities. |

No empty role folders are required. A folder with a responsibility name is a leaf and may contain files. Structural containers (module, submodule, layer and operation) contain folders. CashlyApplication is the bootstrap entry point and the sole Java exception at the global package root.

The full historical comparison, including every source and test file, is recorded in [structure-before-after.md](../../openspec/changes/responsibility-subpackages/structure-before-after.md).

## Current source and tests

### auth

```text
├── main
│   └── auth
│       ├── authentication
│       │   ├── application
│       │   │   ├── dto
│       │   │   │   └── response
│       │   │   │       ├── AuthenticatedEntityResponse.java
│       │   │   │       └── AuthenticationResponse.java
│       │   │   ├── exceptions
│       │   │   │   └── InvalidCredentialsException.java
│       │   │   ├── login
│       │   │   │   ├── request
│       │   │   │   │   └── LoginRequest.java
│       │   │   │   └── use_case
│       │   │   │       └── LoginUserUseCase.java
│       │   │   └── mappers
│       │   │       └── AuthenticationMapper.java
│       │   ├── domain
│       │   │   ├── entity
│       │   │   │   └── Authenticable.java
│       │   │   ├── services
│       │   │   │   ├── AuthenticableResolver.java
│       │   │   │   └── AuthenticationDomainService.java
│       │   │   └── valueobjects
│       │   │       └── AuthenticableType.java
│       │   └── infrastructure
│       │       ├── config
│       │       │   ├── AuthenticationConfig.java
│       │       │   └── SecurityConfig.java
│       │       ├── integration
│       │       │   └── UserAuthenticableAdapter.java
│       │       ├── security
│       │       │   ├── JwtAuthenticationEntryPoint.java
│       │       │   └── JwtAuthenticationFilter.java
│       │       ├── services
│       │       │   └── ChainedAuthenticableResolver.java
│       │       └── web
│       │           └── AuthController.java
│       ├── token
│       │   ├── application
│       │   │   ├── exceptions
│       │   │   │   ├── InvalidTokenException.java
│       │   │   │   └── TokenExpiredException.java
│       │   │   ├── logout
│       │   │   │   ├── request
│       │   │   │   │   └── LogoutRequest.java
│       │   │   │   └── use_case
│       │   │   │       └── LogoutUserUseCase.java
│       │   │   ├── ports
│       │   │   │   └── out
│       │   │   │       └── AuthenticableSubjectGateway.java
│       │   │   └── refresh
│       │   │       ├── request
│       │   │       │   └── RefreshTokenRequest.java
│       │   │       └── use_case
│       │   │           └── RefreshTokenUseCase.java
│       │   ├── domain
│       │   │   ├── entity
│       │   │   │   └── RefreshToken.java
│       │   │   ├── repository
│       │   │   │   └── RefreshTokenRepository.java
│       │   │   ├── services
│       │   │   │   └── TokenService.java
│       │   │   └── valueobjects
│       │   │       └── RefreshTokenId.java
│       │   └── infrastructure
│       │       ├── config
│       │       │   └── JwtTokenService.java
│       │       ├── integration
│       │       │   └── LocalAuthenticableSubjectGateway.java
│       │       └── persistence
│       │           ├── adapters
│       │           │   └── JpaRefreshTokenRepository.java
│       │           ├── entities
│       │           │   └── RefreshTokenEntity.java
│       │           └── repositories
│       │               └── SpringDataRefreshTokenRepository.java
│       └── user
│           ├── application
│           │   ├── delete
│           │   │   └── use_case
│           │   │       └── DeleteUserUseCase.java
│           │   ├── dto
│           │   │   └── response
│           │   │       └── UserResponse.java
│           │   ├── email
│           │   │   ├── request
│           │   │   │   └── UpdateUserEmailRequest.java
│           │   │   └── use_case
│           │   │       └── UpdateUserEmailUseCase.java
│           │   ├── exceptions
│           │   │   ├── DuplicateEmailException.java
│           │   │   ├── UserHasAssociatedRecordsException.java
│           │   │   └── UserNotFoundException.java
│           │   ├── mappers
│           │   │   └── UserMapper.java
│           │   ├── password
│           │   │   ├── request
│           │   │   │   └── UpdateUserPasswordRequest.java
│           │   │   └── use_case
│           │   │       └── UpdateUserPasswordUseCase.java
│           │   ├── permissions
│           │   │   ├── request
│           │   │   │   └── UpdateUserModulePermissionsRequest.java
│           │   │   └── use_case
│           │   │       └── UpdateUserModulePermissionsUseCase.java
│           │   ├── ports
│           │   │   └── in
│           │   │       ├── ActorIdentityQuery.java
│           │   │       └── NotificationRecipientsQuery.java
│           │   ├── queries
│           │   │   ├── services
│           │   │   │   ├── RepositoryActorIdentityQuery.java
│           │   │   │   └── RepositoryNotificationRecipientsQuery.java
│           │   │   └── use_case
│           │   │       ├── GetAllUsersUseCase.java
│           │   │       └── GetUserByIdUseCase.java
│           │   ├── register
│           │   │   ├── request
│           │   │   │   └── RegisterUserRequest.java
│           │   │   └── use_case
│           │   │       └── RegisterUserUseCase.java
│           │   └── update
│           │       ├── request
│           │       │   └── UpdateUserRequest.java
│           │       └── use_case
│           │           └── UpdateUserUseCase.java
│           ├── domain
│           │   ├── entity
│           │   │   └── User.java
│           │   ├── repository
│           │   │   └── UserRepository.java
│           │   ├── services
│           │   │   └── PasswordEncoder.java
│           │   └── valueobjects
│           │       ├── BusinessModule.java
│           │       ├── Email.java
│           │       ├── Password.java
│           │       ├── UserId.java
│           │       ├── UserName.java
│           │       └── UserRole.java
│           └── infrastructure
│               ├── config
│               │   ├── ActorIdentityQueryConfig.java
│               │   ├── BcryptPasswordEncoder.java
│               │   └── UserQueryConfig.java
│               ├── persistence
│               │   ├── adapters
│               │   │   └── JpaUserRepository.java
│               │   ├── entities
│               │   │   ├── UserEntity.java
│               │   │   └── UserModulePermissionEntity.java
│               │   └── repositories
│               │       └── SpringDataUserRepository.java
│               └── web
│                   └── UserController.java
└── test
    └── auth
        ├── application
        │   └── architecture
        │       └── RefreshTokenBoundaryTest.java
        ├── authentication
        │   ├── application
        │   │   ├── exceptions
        │   │   │   └── InvalidCredentialsExceptionUnitTest.java
        │   │   └── login
        │   │       ├── request
        │   │       │   ├── validation
        │   │       │   │   └── LoginRequestUnitTest.java
        │   │       │   └── LoginRequestUnitTest.java
        │   │       └── use_case
        │   │           └── LoginUserUseCaseTest.java
        │   ├── domain
        │   │   └── services
        │   │       └── AuthenticationDomainServiceUnitTest.java
        │   └── infrastructure
        │       ├── security
        │       │   ├── JwtAuthenticationEntryPointUnitTest.java
        │       │   └── JwtAuthenticationFilterUnitTest.java
        │       └── web
        │           └── AuthControllerUnitTest.java
        ├── token
        │   ├── application
        │   │   ├── exceptions
        │   │   │   ├── InvalidTokenExceptionUnitTest.java
        │   │   │   └── TokenExpiredExceptionUnitTest.java
        │   │   ├── logout
        │   │   │   ├── request
        │   │   │   │   ├── validation
        │   │   │   │   │   └── LogoutRequestUnitTest.java
        │   │   │   │   └── LogoutRequestUnitTest.java
        │   │   │   └── use_case
        │   │   │       └── LogoutUserUseCaseUnitTest.java
        │   │   └── refresh
        │   │       ├── request
        │   │       │   ├── validation
        │   │       │   │   └── RefreshTokenRequestUnitTest.java
        │   │       │   └── RefreshTokenRequestUnitTest.java
        │   │       └── use_case
        │   │           └── RefreshTokenUseCaseUnitTest.java
        │   ├── domain
        │   │   ├── entity
        │   │   │   └── RefreshTokenUnitTest.java
        │   │   └── valueobjects
        │   │       └── RefreshTokenIdUnitTest.java
        │   └── infrastructure
        │       ├── config
        │       │   └── JwtTokenServiceUnitTest.java
        │       ├── integration
        │       │   └── LocalAuthenticableSubjectGatewayTest.java
        │       └── persistence
        │           ├── adapters
        │           │   └── JpaRefreshTokenRepositoryUnitTest.java
        │           └── entities
        │               └── RefreshTokenEntityUnitTest.java
        └── user
            ├── application
            │   ├── dto
            │   │   └── response
            │   │       └── UserResponseUnitTest.java
            │   ├── email
            │   │   └── use_case
            │   │       └── UpdateUserEmailUseCaseTest.java
            │   ├── exceptions
            │   │   ├── DuplicateEmailExceptionUnitTest.java
            │   │   └── UserNotFoundExceptionUnitTest.java
            │   ├── mappers
            │   │   └── UserMapperUnitTest.java
            │   ├── password
            │   │   └── use_case
            │   │       └── UpdateUserPasswordUseCaseTest.java
            │   ├── permissions
            │   │   └── use_case
            │   │       └── UpdateUserModulePermissionsUseCaseTest.java
            │   ├── queries
            │   │   ├── services
            │   │   │   ├── RepositoryActorIdentityQueryTest.java
            │   │   │   └── RepositoryNotificationRecipientsQueryTest.java
            │   │   └── use_case
            │   │       ├── GetAllUsersUseCaseTest.java
            │   │       └── GetUserByIdUseCaseUnitTest.java
            │   └── register
            │       ├── request
            │       │   └── RegisterUserRequestUnitTest.java
            │       └── use_case
            │           └── RegisterUserUseCaseUnitTest.java
            ├── domain
            │   ├── entity
            │   │   └── UserUnitTest.java
            │   └── valueobjects
            │       ├── BusinessModuleUnitTest.java
            │       ├── EmailUnitTest.java
            │       ├── PasswordUnitTest.java
            │       ├── UserIdUnitTest.java
            │       ├── UserNameUnitTest.java
            │       └── UserRoleUnitTest.java
            └── infrastructure
                ├── config
                │   └── BcryptPasswordEncoderUnitTest.java
                └── persistence
                    └── adapters
                        └── JpaUserRepositoryUnitTest.java
```

### cash_register

```text
├── main
│   └── cash_register
│       ├── application
│       │   └── ports
│       │       └── out
│       │           └── CashRegisterQueryGateway.java
│       ├── infrastructure
│       │   ├── config
│       │   │   └── CashRegisterConfig.java
│       │   └── persistence
│       │       └── adapters
│       │           └── CashRegisterRepositoryAdapter.java
│       ├── reporting
│       │   ├── application
│       │   │   ├── dto
│       │   │   │   └── model
│       │   │   │       ├── CashRegisterExportRow.java
│       │   │   │       ├── CashRegisterReportFilter.java
│       │   │   │       ├── CustomerSummary.java
│       │   │   │       ├── ReportFormat.java
│       │   │   │       └── ReportType.java
│       │   │   ├── excel
│       │   │   │   ├── services
│       │   │   │   │   └── GenerateCashRegisterExcelUseCaseImpl.java
│       │   │   │   └── use_case
│       │   │   │       └── GenerateCashRegisterExcelUseCase.java
│       │   │   ├── ports
│       │   │   │   └── out
│       │   │   │       ├── CashRegisterExcelPort.java
│       │   │   │       ├── CashRegisterRangeReportPort.java
│       │   │   │       └── CashRegisterReportPort.java
│       │   │   ├── range
│       │   │   │   └── use_case
│       │   │   │       └── GenerateCashRegisterRangeReportUseCase.java
│       │   │   └── session
│       │   │       └── use_case
│       │   │           └── GenerateCashRegisterReportUseCase.java
│       │   └── infrastructure
│       │       ├── excel
│       │       │   ├── CashRegisterExcelExportService.java
│       │       │   └── CashRegisterExcelGenerator.java
│       │       ├── report
│       │       │   ├── CashRegisterRangeReportGenerator.java
│       │       │   └── CashRegisterSessionReportGenerator.java
│       │       └── web
│       │           └── CashRegisterExportController.java
│       └── sessions
│           ├── application
│           │   ├── close
│           │   │   ├── request
│           │   │   │   └── CloseRegisterRequest.java
│           │   │   └── use_case
│           │   │       └── CloseRegisterUseCase.java
│           │   ├── dto
│           │   │   └── response
│           │   │       ├── CashRegisterDetailResponse.java
│           │   │       ├── CashRegisterMetrics.java
│           │   │       ├── CashRegisterResponse.java
│           │   │       ├── DailyTotals.java
│           │   │       ├── ExpenseSummaryItem.java
│           │   │       └── RevenueSummaryItem.java
│           │   ├── mappers
│           │   │   └── CashRegisterMapper.java
│           │   ├── movements
│           │   │   ├── request
│           │   │   │   └── CashMovementRequest.java
│           │   │   ├── response
│           │   │   │   └── CashMovementResponse.java
│           │   │   ├── services
│           │   │   │   └── CashMovementService.java
│           │   │   └── use_case
│           │   │       └── RecordCashMovementUseCase.java
│           │   ├── open
│           │   │   ├── request
│           │   │   │   └── OpenRegisterRequest.java
│           │   │   └── use_case
│           │   │       └── OpenRegisterUseCase.java
│           │   ├── ports
│           │   │   ├── in
│           │   │   │   ├── CashRegisterOperations.java
│           │   │   │   ├── CreditNoteCashApi.java
│           │   │   │   ├── DebitNoteCashApi.java
│           │   │   │   ├── QueryPaymentMethodUsage.java
│           │   │   │   └── ReturnCashRecording.java
│           │   │   └── out
│           │   │       ├── CashRegisterActorGateway.java
│           │   │       └── CashRegisterPaymentMethodGateway.java
│           │   ├── queries
│           │   │   └── use_case
│           │   │       ├── GetActiveRegisterUseCase.java
│           │   │       └── GetRegisterHistoryUseCase.java
│           │   └── services
│           │       ├── CashRegisterOperationsService.java
│           │       └── ReturnCashRecordingService.java
│           ├── domain
│           │   ├── entity
│           │   │   ├── CashMovement.java
│           │   │   ├── CashRegister.java
│           │   │   └── CashRegisterDetail.java
│           │   ├── repository
│           │   │   └── CashRegisterRepository.java
│           │   └── valueobjects
│           │       ├── CashMovementDirection.java
│           │       ├── CashMovementType.java
│           │       ├── RegisterId.java
│           │       └── RegisterStatus.java
│           └── infrastructure
│               ├── config
│               │   ├── CashRegisterOperationsConfig.java
│               │   └── ReturnCashRecordingConfig.java
│               ├── integration
│               │   ├── LocalCashRegisterActorGateway.java
│               │   ├── LocalCashRegisterPaymentMethodGateway.java
│               │   ├── LocalCreditNoteCashApi.java
│               │   └── LocalDebitNoteCashApi.java
│               ├── persistence
│               │   ├── entities
│               │   │   ├── CashMovementJpaEntity.java
│               │   │   ├── CashRegisterDetailJpaEntity.java
│               │   │   └── CashRegisterJpaEntity.java
│               │   ├── queries
│               │   │   └── JpaPaymentMethodUsageQuery.java
│               │   └── repositories
│               │       ├── CashMovementJpaRepository.java
│               │       └── CashRegisterJpaRepository.java
│               └── web
│                   └── CashRegisterController.java
└── test
    └── cash_register
        ├── application
        │   └── architecture
        │       └── CashRegisterBoundaryTest.java
        ├── infrastructure
        │   ├── config
        │   │   └── CashRegisterConfigTest.java
        │   └── persistence
        │       └── adapters
        │           ├── CashRegisterRepositoryAdapterSessionTotalsTest.java
        │           └── CashRegisterRepositoryAdapterTest.java
        ├── reporting
        │   ├── application
        │   │   ├── dto
        │   │   │   └── model
        │   │   │       └── CashRegisterReportFilterTest.java
        │   │   ├── excel
        │   │   │   └── services
        │   │   │       └── GenerateCashRegisterExcelUseCaseImplTest.java
        │   │   ├── range
        │   │   │   └── use_case
        │   │   │       └── GenerateCashRegisterRangeReportUseCaseTest.java
        │   │   └── session
        │   │       └── use_case
        │   │           └── GenerateCashRegisterReportUseCaseTest.java
        │   └── infrastructure
        │       ├── excel
        │       │   ├── CashRegisterExcelExportServiceUnitTest.java
        │       │   ├── CashRegisterExcelGeneratorTest.java
        │       │   └── VerifyCellsTest.java
        │       ├── report
        │       │   ├── CashRegisterRangeReportGeneratorTest.java
        │       │   └── CashRegisterSessionReportGeneratorTest.java
        │       └── web
        │           └── CashRegisterExportControllerTest.java
        └── sessions
            ├── application
            │   ├── close
            │   │   └── use_case
            │   │       └── CloseRegisterUseCaseUnitTest.java
            │   ├── dto
            │   │   └── response
            │   │       ├── CashRegisterMetricsTest.java
            │   │       └── DailyTotalsTest.java
            │   ├── mappers
            │   │   └── CashRegisterMapperTest.java
            │   ├── movements
            │   │   ├── services
            │   │   │   └── CashMovementServiceTest.java
            │   │   └── use_case
            │   │       └── RecordCashMovementUseCaseTest.java
            │   ├── open
            │   │   └── use_case
            │   │       └── OpenRegisterUseCaseUnitTest.java
            │   └── queries
            │       └── use_case
            │           ├── GetActiveRegisterUseCaseTest.java
            │           └── GetRegisterHistoryUseCaseUnitTest.java
            ├── domain
            │   ├── entity
            │   │   ├── CashMovementUnitTest.java
            │   │   ├── CashRegisterDetailTest.java
            │   │   └── CashRegisterUnitTest.java
            │   └── valueobjects
            │       ├── RegisterIdUnitTest.java
            │       └── RegisterStatusUnitTest.java
            └── infrastructure
                ├── integration
                │   ├── LocalCreditNoteCashApiTest.java
                │   └── LocalDebitNoteCashApiTest.java
                ├── persistence
                │   └── queries
                │       └── JpaPaymentMethodUsageQueryTest.java
                └── web
                    └── CashRegisterControllerTest.java
```

### categories

```text
├── main
│   └── categories
│       ├── application
│       │   ├── create
│       │   │   ├── request
│       │   │   │   └── CreateCategoryRequest.java
│       │   │   └── use_case
│       │   │       └── CreateCategoryUseCase.java
│       │   ├── delete
│       │   │   └── use_case
│       │   │       └── DeleteCategoryUseCase.java
│       │   ├── dto
│       │   │   └── response
│       │   │       ├── CategoryDetailResponse.java
│       │   │       ├── CategoryListResponse.java
│       │   │       └── CategorySummary.java
│       │   ├── exceptions
│       │   │   ├── CategoryHasActiveProductsException.java
│       │   │   ├── CategoryNotFoundException.java
│       │   │   └── DuplicateCategoryNameException.java
│       │   ├── mappers
│       │   │   └── CategoryMapper.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   └── ProductCategoryQuery.java
│       │   │   └── out
│       │   │       └── CategoryUsageGateway.java
│       │   ├── queries
│       │   │   ├── services
│       │   │   │   └── RepositoryProductCategoryQuery.java
│       │   │   └── use_case
│       │   │       ├── GetCategoryUseCase.java
│       │   │       └── ListCategoriesUseCase.java
│       │   └── update
│       │       ├── request
│       │       │   └── UpdateCategoryRequest.java
│       │       └── use_case
│       │           └── UpdateCategoryUseCase.java
│       ├── domain
│       │   ├── entity
│       │   │   └── Category.java
│       │   └── repository
│       │       └── CategoryRepository.java
│       └── infrastructure
│           ├── config
│           │   ├── CategoryConfig.java
│           │   └── ProductCategoryQueryConfig.java
│           ├── integration
│           │   └── ProductCategoryUsageAdapter.java
│           ├── persistence
│           │   ├── adapters
│           │   │   └── JpaCategoryRepository.java
│           │   ├── entities
│           │   │   └── CategoryEntity.java
│           │   └── repositories
│           │       └── SpringDataCategoryRepository.java
│           └── web
│               └── CategoryController.java
└── test
    └── categories
        ├── application
        │   ├── create
        │   │   └── use_case
        │   │       └── CreateCategoryUseCaseTest.java
        │   ├── delete
        │   │   └── use_case
        │   │       └── DeleteCategoryUseCaseTest.java
        │   ├── exceptions
        │   │   └── CategoryNotFoundExceptionTest.java
        │   ├── mappers
        │   │   └── CategoryMapperTest.java
        │   ├── queries
        │   │   └── use_case
        │   │       ├── GetCategoryUseCaseTest.java
        │   │       └── ListCategoriesUseCaseTest.java
        │   └── update
        │       └── use_case
        │           └── UpdateCategoryUseCaseTest.java
        ├── domain
        │   └── entity
        │       ├── CatalogUsageBoundaryTest.java
        │       └── CategoryUnitTest.java
        └── infrastructure
            ├── config
            │   └── CategoryConfigTest.java
            ├── persistence
            │   └── adapters
            │       └── JpaCategoryRepositoryTest.java
            └── web
                └── CategoryControllerTest.java
```

### credit_notes

```text
├── main
│   └── credit_notes
│       ├── application
│       │   ├── create
│       │   │   ├── request
│       │   │   │   └── CreateCreditNoteRequest.java
│       │   │   └── use_case
│       │   │       └── CreateCreditNoteUseCase.java
│       │   ├── delete
│       │   │   └── use_case
│       │   │       └── DeleteCreditNoteUseCase.java
│       │   ├── dto
│       │   │   └── response
│       │   │       └── CreditNoteResponse.java
│       │   ├── get
│       │   │   └── use_case
│       │   │       └── GetCreditNoteByIdUseCase.java
│       │   ├── list
│       │   │   └── use_case
│       │   │       └── GetCreditNotesUseCase.java
│       │   ├── mappers
│       │   │   └── CreditNoteMapper.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   ├── CreditNoteLedgerQuery.java
│       │   │   │   └── InvoiceCreditNoteDetailsQuery.java
│       │   │   └── out
│       │   │       ├── CreditNoteEffectsGateway.java
│       │   │       ├── CreditNoteFinancialPositionGateway.java
│       │   │       └── CreditNoteInvoiceGateway.java
│       │   └── queries
│       │       └── services
│       │           ├── InvoiceCreditNoteDetailsQueryService.java
│       │           └── RepositoryCreditNoteLedgerQuery.java
│       ├── domain
│       │   ├── entity
│       │   │   └── CreditNote.java
│       │   ├── repository
│       │   │   └── CreditNoteRepository.java
│       │   └── valueobjects
│       │       ├── CreditNoteId.java
│       │       └── CreditNoteNumber.java
│       └── infrastructure
│           ├── config
│           │   └── CreditNoteLedgerQueryConfig.java
│           ├── integration
│           │   ├── LocalCreditNoteEffectsGateway.java
│           │   ├── LocalCreditNoteFinancialPositionGateway.java
│           │   └── LocalCreditNoteInvoiceGateway.java
│           ├── persistence
│           │   ├── adapters
│           │   │   └── CreditNoteRepositoryAdapter.java
│           │   ├── entities
│           │   │   ├── CreditNoteEntity.java
│           │   │   └── CreditNoteNumberSequenceEntity.java
│           │   ├── mappers
│           │   │   └── CreditNoteJpaMapper.java
│           │   └── repositories
│           │       ├── CreditNoteJpaRepository.java
│           │       └── CreditNoteNumberSequenceJpaRepository.java
│           └── web
│               └── CreditNoteController.java
└── test
    └── credit_notes
        ├── application
        │   ├── architecture
        │   │   └── CreditNoteModuleBoundaryTest.java
        │   ├── create
        │   │   └── use_case
        │   │       └── CreateCreditNoteUseCaseTest.java
        │   ├── delete
        │   │   └── use_case
        │   │       └── DeleteCreditNoteUseCaseTest.java
        │   ├── get
        │   │   └── use_case
        │   │       └── GetCreditNoteByIdUseCaseTest.java
        │   ├── list
        │   │   └── use_case
        │   │       └── GetCreditNotesUseCaseTest.java
        │   ├── mappers
        │   │   └── CreditNoteMapperTest.java
        │   └── queries
        │       └── services
        │           └── RepositoryCreditNoteLedgerQueryTest.java
        ├── domain
        │   ├── entity
        │   │   └── CreditNoteUnitTest.java
        │   └── valueobjects
        │       ├── CreditNoteIdUnitTest.java
        │       └── CreditNoteNumberUnitTest.java
        └── infrastructure
            ├── integration
            │   ├── LocalCreditNoteEffectsGatewayTest.java
            │   ├── LocalCreditNoteFinancialPositionGatewayTest.java
            │   └── LocalCreditNoteInvoiceGatewayTest.java
            ├── persistence
            │   └── adapters
            │       └── CreditNoteRepositoryAdapterTest.java
            └── web
                └── CreditNoteControllerTest.java
```

### customers

```text
├── main
│   └── customers
│       ├── balance
│       │   ├── application
│       │   │   ├── add
│       │   │   │   └── use_case
│       │   │   │       └── AddToCustomerBalanceUseCase.java
│       │   │   ├── advance
│       │   │   │   ├── request
│       │   │   │   │   └── CreateAdvanceRequest.java
│       │   │   │   └── use_case
│       │   │   │       └── CreateCustomerAdvanceUseCase.java
│       │   │   ├── consume
│       │   │   │   └── use_case
│       │   │   │       └── ConsumeCustomerBalanceUseCase.java
│       │   │   ├── dto
│       │   │   │   └── response
│       │   │   │       ├── BalanceTransactionResponse.java
│       │   │   │       ├── CustomerBalanceMutationResponse.java
│       │   │   │       └── CustomerBalanceResponse.java
│       │   │   ├── get
│       │   │   │   └── use_case
│       │   │   │       └── GetCustomerBalanceUseCase.java
│       │   │   ├── mappers
│       │   │   │   └── BalanceMapper.java
│       │   │   ├── ports
│       │   │   │   ├── in
│       │   │   │   │   ├── CreditNoteBalanceApi.java
│       │   │   │   │   ├── InvoiceBalanceConsumption.java
│       │   │   │   │   └── ReturnBalanceMutation.java
│       │   │   │   └── out
│       │   │   │       ├── CustomerActorGateway.java
│       │   │   │       └── CustomerCashGateway.java
│       │   │   ├── refund
│       │   │   │   ├── request
│       │   │   │   │   └── CreateRefundRequest.java
│       │   │   │   └── use_case
│       │   │   │       └── RefundCustomerBalanceUseCase.java
│       │   │   └── services
│       │   │       ├── InvoiceBalanceConsumptionService.java
│       │   │       └── ReturnBalanceMutationService.java
│       │   ├── domain
│       │   │   ├── entity
│       │   │   │   ├── BalanceTransaction.java
│       │   │   │   └── CustomerBalance.java
│       │   │   ├── repository
│       │   │   │   └── CustomerBalanceRepository.java
│       │   │   └── valueobjects
│       │   │       └── BalanceType.java
│       │   └── infrastructure
│       │       ├── config
│       │       │   ├── InvoiceBalanceConsumptionConfig.java
│       │       │   └── ReturnBalanceMutationConfig.java
│       │       ├── integration
│       │       │   ├── LocalCreditNoteBalanceApi.java
│       │       │   ├── LocalCustomerActorGateway.java
│       │       │   └── LocalCustomerCashGateway.java
│       │       ├── persistence
│       │       │   ├── adapters
│       │       │   │   └── CustomerBalanceRepositoryAdapter.java
│       │       │   ├── entities
│       │       │   │   ├── BalanceTransactionEntity.java
│       │       │   │   ├── BalanceTxSequenceEntity.java
│       │       │   │   └── CustomerBalanceEntity.java
│       │       │   ├── mappers
│       │       │   │   ├── BalanceTransactionJpaMapper.java
│       │       │   │   └── CustomerBalanceJpaMapper.java
│       │       │   └── repositories
│       │       │       └── CustomerBalanceJpaRepository.java
│       │       └── web
│       │           └── CustomerBalanceController.java
│       ├── credit
│       │   ├── application
│       │   │   ├── dto
│       │   │   │   └── response
│       │   │   │       ├── CreditStatusResponse.java
│       │   │   │       └── OverdueInvoiceResponse.java
│       │   │   ├── exceptions
│       │   │   │   └── CreditLimitExceededException.java
│       │   │   ├── overdue
│       │   │   │   └── use_case
│       │   │   │       └── GetOverdueInvoicesUseCase.java
│       │   │   ├── ports
│       │   │   │   ├── in
│       │   │   │   │   ├── CustomerCreditContextEntry.java
│       │   │   │   │   ├── CustomerCreditContextQuery.java
│       │   │   │   │   ├── CustomerCreditEligibilityQuery.java
│       │   │   │   │   └── CustomerCreditLimits.java
│       │   │   │   └── out
│       │   │   │       └── CreditValidationGateway.java
│       │   │   ├── queries
│       │   │   │   └── services
│       │   │   │       └── RepositoryCustomerCreditContextQuery.java
│       │   │   ├── services
│       │   │   │   └── CustomerCreditEligibilityService.java
│       │   │   └── status
│       │   │       └── use_case
│       │   │           └── GetCustomerCreditStatusUseCase.java
│       │   └── infrastructure
│       │       ├── config
│       │       │   └── CustomerCreditQueryConfig.java
│       │       ├── integration
│       │       │   └── LocalCreditValidationGateway.java
│       │       └── web
│       │           └── CustomerCreditController.java
│       ├── infrastructure
│       │   └── config
│       │       └── CustomerConfig.java
│       ├── profile
│       │   ├── application
│       │   │   ├── delete
│       │   │   │   ├── services
│       │   │   │   │   └── CustomerHasAssociatedRecordsException.java
│       │   │   │   └── use_case
│       │   │   │       └── DeleteCustomerUseCase.java
│       │   │   ├── dto
│       │   │   │   ├── request
│       │   │   │   │   ├── AddressRequest.java
│       │   │   │   │   ├── EmailRequest.java
│       │   │   │   │   └── PhoneRequest.java
│       │   │   │   └── response
│       │   │   │       ├── AddressResponse.java
│       │   │   │       ├── CustomerDetailResponse.java
│       │   │   │       ├── CustomerListResponse.java
│       │   │   │       ├── EmailResponse.java
│       │   │   │       └── PhoneResponse.java
│       │   │   ├── exceptions
│       │   │   │   ├── CustomerNotFoundException.java
│       │   │   │   └── DuplicateCustomerRifException.java
│       │   │   ├── get
│       │   │   │   └── use_case
│       │   │   │       └── GetCustomerUseCase.java
│       │   │   ├── list
│       │   │   │   └── use_case
│       │   │   │       └── ListCustomersUseCase.java
│       │   │   ├── mappers
│       │   │   │   └── CustomerMapper.java
│       │   │   ├── ports
│       │   │   │   └── in
│       │   │   │       ├── CustomerCreditTermsQuery.java
│       │   │   │       ├── CustomerProductPricingQuery.java
│       │   │   │       ├── CustomerProfileQuery.java
│       │   │   │       ├── CustomerProfileSnapshot.java
│       │   │   │       └── InvoiceCustomerQuery.java
│       │   │   ├── queries
│       │   │   │   └── services
│       │   │   │       ├── RepositoryCustomerCreditTermsQuery.java
│       │   │   │       ├── RepositoryCustomerProductPricingQuery.java
│       │   │   │       ├── RepositoryCustomerProfileQuery.java
│       │   │   │       └── RepositoryInvoiceCustomerQuery.java
│       │   │   ├── register
│       │   │   │   ├── request
│       │   │   │   │   └── CreateCustomerRequest.java
│       │   │   │   ├── response
│       │   │   │   │   └── CustomerCreateResponse.java
│       │   │   │   └── use_case
│       │   │   │       └── RegisterCustomerUseCase.java
│       │   │   └── update
│       │   │       ├── request
│       │   │       │   └── UpdateCustomerRequest.java
│       │   │       └── use_case
│       │   │           └── UpdateCustomerUseCase.java
│       │   ├── domain
│       │   │   ├── entity
│       │   │   │   ├── Customer.java
│       │   │   │   ├── CustomerAddress.java
│       │   │   │   ├── CustomerEmail.java
│       │   │   │   └── CustomerPhone.java
│       │   │   ├── repository
│       │   │   │   └── CustomerRepository.java
│       │   │   ├── services
│       │   │   │   └── CustomerCodeGenerator.java
│       │   │   └── valueobjects
│       │   │       ├── AddressId.java
│       │   │       ├── CreditDays.java
│       │   │       ├── CreditLimit.java
│       │   │       ├── CustomerAlias.java
│       │   │       ├── CustomerCode.java
│       │   │       ├── CustomerId.java
│       │   │       ├── CustomerType.java
│       │   │       ├── EmailAddress.java
│       │   │       ├── EmailId.java
│       │   │       ├── LegalName.java
│       │   │       ├── PaymentType.java
│       │   │       ├── PhoneId.java
│       │   │       ├── PhoneNumber.java
│       │   │       └── Rif.java
│       │   └── infrastructure
│       │       ├── config
│       │       │   ├── CustomerCreditTermsConfig.java
│       │       │   ├── CustomerProductPricingConfig.java
│       │       │   ├── CustomerProfileQueryConfig.java
│       │       │   └── InvoiceCustomerQueryConfig.java
│       │       ├── persistence
│       │       │   ├── adapters
│       │       │   │   └── JpaCustomerRepository.java
│       │       │   ├── entities
│       │       │   │   ├── CustomerAddressEntity.java
│       │       │   │   ├── CustomerEmailEntity.java
│       │       │   │   ├── CustomerEntity.java
│       │       │   │   └── CustomerPhoneEntity.java
│       │       │   └── repositories
│       │       │       └── SpringDataCustomerRepository.java
│       │       ├── services
│       │       │   └── SequentialCustomerCodeGenerator.java
│       │       └── web
│       │           └── CustomerController.java
│       └── warehouse_access
│           ├── application
│           │   ├── dto
│           │   │   └── response
│           │   │       └── CustomerWarehouseAccessResponse.java
│           │   ├── exceptions
│           │   │   ├── CustomerWarehouseAccessAlreadyExistsException.java
│           │   │   └── CustomerWarehouseAccessNotFoundException.java
│           │   ├── grant
│           │   │   └── use_case
│           │   │       └── GrantWarehouseAccessUseCase.java
│           │   ├── list
│           │   │   └── use_case
│           │   │       └── ListCustomerWarehousesUseCase.java
│           │   ├── ports
│           │   │   ├── in
│           │   │   │   └── CustomerWarehouseAccessQuery.java
│           │   │   └── out
│           │   │       ├── model
│           │   │       │   └── WarehouseSnapshot.java
│           │   │       └── WarehouseDirectoryGateway.java
│           │   ├── queries
│           │   │   └── services
│           │   │       └── RepositoryCustomerWarehouseAccessQuery.java
│           │   └── revoke
│           │       └── use_case
│           │           └── RevokeWarehouseAccessUseCase.java
│           ├── domain
│           │   ├── entity
│           │   │   └── CustomerWarehouseAccess.java
│           │   └── repository
│           │       └── CustomerWarehouseAccessRepository.java
│           └── infrastructure
│               ├── config
│               │   └── CustomerWarehouseAccessQueryConfig.java
│               ├── integration
│               │   └── LocalWarehouseDirectoryGateway.java
│               ├── persistence
│               │   ├── adapters
│               │   │   └── JpaCustomerWarehouseAccessRepository.java
│               │   ├── entities
│               │   │   └── CustomerWarehouseAccessEntity.java
│               │   └── repositories
│               │       └── SpringDataCustomerWarehouseAccessRepository.java
│               └── web
│                   └── CustomerWarehouseAccessController.java
└── test
    └── customers
        ├── balance
        │   ├── application
        │   │   ├── add
        │   │   │   └── use_case
        │   │   │       └── AddToCustomerBalanceUseCaseTest.java
        │   │   ├── consume
        │   │   │   └── use_case
        │   │   │       └── ConsumeCustomerBalanceUseCaseTest.java
        │   │   ├── get
        │   │   │   └── use_case
        │   │   │       └── GetCustomerBalanceUseCaseTest.java
        │   │   ├── mappers
        │   │   │   └── BalanceMapperTest.java
        │   │   ├── services
        │   │   │   ├── InvoiceBalanceConsumptionServiceTest.java
        │   │   │   └── ReturnBalanceMutationServiceTest.java
        │   │   └── use_case
        │   │       └── CreateCustomerCashBalanceOperationUseCaseTest.java
        │   ├── domain
        │   │   └── entity
        │   │       ├── BalanceTransactionTest.java
        │   │       ├── CustomerBalanceBoundaryTest.java
        │   │       └── CustomerBalanceTest.java
        │   └── infrastructure
        │       ├── integration
        │       │   ├── LocalCreditNoteBalanceApiTest.java
        │       │   ├── LocalCustomerActorGatewayTest.java
        │       │   └── LocalCustomerCashGatewayTest.java
        │       ├── persistence
        │       │   ├── adapters
        │       │   │   └── CustomerBalanceRepositoryAdapterTest.java
        │       │   └── mappers
        │       │       ├── BalanceTransactionJpaMapperTest.java
        │       │       └── CustomerBalanceJpaMapperTest.java
        │       └── web
        │           └── CustomerBalanceControllerTest.java
        ├── credit
        │   ├── application
        │   │   ├── architecture
        │   │   │   └── CustomerCreditContractTest.java
        │   │   ├── exceptions
        │   │   │   └── CreditLimitExceededExceptionTest.java
        │   │   ├── overdue
        │   │   │   └── use_case
        │   │   │       └── GetOverdueInvoicesUseCaseTest.java
        │   │   ├── queries
        │   │   │   └── services
        │   │   │       └── RepositoryCustomerCreditContextQueryTest.java
        │   │   ├── services
        │   │   │   └── CustomerCreditEligibilityServiceTest.java
        │   │   └── status
        │   │       └── use_case
        │   │           └── GetCustomerCreditStatusUseCaseTest.java
        │   └── infrastructure
        │       ├── integration
        │       │   └── LocalCreditValidationGatewayTest.java
        │       └── web
        │           └── CustomerCreditControllerTest.java
        ├── profile
        │   ├── application
        │   │   ├── delete
        │   │   │   └── use_case
        │   │   │       └── DeleteCustomerUseCaseTest.java
        │   │   ├── exceptions
        │   │   │   └── CustomerNotFoundExceptionTest.java
        │   │   ├── get
        │   │   │   └── use_case
        │   │   │       └── GetCustomerUseCaseTest.java
        │   │   ├── list
        │   │   │   └── use_case
        │   │   │       └── ListCustomersUseCaseTest.java
        │   │   ├── mappers
        │   │   │   └── CustomerMapperTest.java
        │   │   ├── register
        │   │   │   └── use_case
        │   │   │       └── RegisterCustomerUseCaseTest.java
        │   │   └── update
        │   │       └── use_case
        │   │           └── UpdateCustomerUseCaseTest.java
        │   ├── domain
        │   │   ├── entity
        │   │   │   ├── CustomerAddressUnitTest.java
        │   │   │   ├── CustomerEmailUnitTest.java
        │   │   │   ├── CustomerPhoneUnitTest.java
        │   │   │   └── CustomerUnitTest.java
        │   │   └── valueobjects
        │   │       ├── AddressIdUnitTest.java
        │   │       ├── CreditDaysUnitTest.java
        │   │       ├── CreditLimitUnitTest.java
        │   │       ├── CustomerAliasUnitTest.java
        │   │       ├── CustomerCodeUnitTest.java
        │   │       ├── CustomerIdUnitTest.java
        │   │       ├── CustomerTypeUnitTest.java
        │   │       ├── EmailAddressUnitTest.java
        │   │       ├── EmailIdUnitTest.java
        │   │       ├── LegalNameUnitTest.java
        │   │       ├── PaymentTypeUnitTest.java
        │   │       ├── PhoneIdUnitTest.java
        │   │       ├── PhoneNumberUnitTest.java
        │   │       └── RifUnitTest.java
        │   └── infrastructure
        │       ├── persistence
        │       │   └── adapters
        │       │       └── JpaCustomerRepositoryTest.java
        │       └── web
        │           └── CustomerControllerTest.java
        ├── testing
        │   └── CustomerTestHelper.java
        └── warehouse_access
            ├── application
            │   ├── architecture
            │   │   └── CustomerWarehouseBoundaryTest.java
            │   ├── exceptions
            │   │   └── CustomerWarehouseAccessAlreadyExistsExceptionTest.java
            │   ├── grant
            │   │   └── use_case
            │   │       └── GrantWarehouseAccessUseCaseTest.java
            │   ├── list
            │   │   └── use_case
            │   │       └── ListCustomerWarehousesUseCaseTest.java
            │   └── revoke
            │       └── use_case
            │           └── RevokeWarehouseAccessUseCaseTest.java
            ├── domain
            │   └── entity
            │       └── CustomerWarehouseAccessUnitTest.java
            └── infrastructure
                ├── integration
                │   └── LocalWarehouseDirectoryGatewayTest.java
                ├── persistence
                │   └── adapters
                │       └── JpaCustomerWarehouseAccessRepositoryTest.java
                └── web
                    └── CustomerWarehouseAccessControllerTest.java
```

### dashboard

```text
├── main
│   └── dashboard
│       ├── application
│       │   ├── dto
│       │   │   └── response
│       │   │       ├── DashboardSummaryResponse.java
│       │   │       ├── InvoiceDueResponse.java
│       │   │       ├── MonthlySalesResponse.java
│       │   │       └── TopItemResponse.java
│       │   ├── ports
│       │   │   └── out
│       │   │       └── DashboardQueryGateway.java
│       │   └── summary
│       │       └── use_case
│       │           └── GetDashboardUseCase.java
│       └── infrastructure
│           ├── persistence
│           │   └── queries
│           │       └── JdbcDashboardQueryAdapter.java
│           └── web
│               └── DashboardController.java
└── test
    └── dashboard
        ├── application
        │   ├── ports
        │   │   └── out
        │   │       └── DashboardQueryGatewayTest.java
        │   └── summary
        │       └── architecture
        │           └── DashboardArchitectureTest.java
        └── infrastructure
            └── persistence
                └── queries
                    └── JdbcDashboardQueryAdapterTest.java
```

### debit_notes

```text
├── main
│   └── debit_notes
│       ├── application
│       │   ├── create
│       │   │   ├── request
│       │   │   │   └── CreateDebitNoteRequest.java
│       │   │   └── use_case
│       │   │       └── CreateDebitNoteUseCase.java
│       │   ├── delete
│       │   │   └── use_case
│       │   │       └── DeleteDebitNoteUseCase.java
│       │   ├── dto
│       │   │   └── response
│       │   │       └── DebitNoteResponse.java
│       │   ├── get
│       │   │   └── use_case
│       │   │       └── GetDebitNoteByIdUseCase.java
│       │   ├── list
│       │   │   └── use_case
│       │   │       └── GetDebitNotesUseCase.java
│       │   ├── mappers
│       │   │   └── DebitNoteMapper.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   ├── DebitNoteLedgerQuery.java
│       │   │   │   └── InvoiceDebitNoteDetailsQuery.java
│       │   │   └── out
│       │   │       ├── DebitNoteCashGateway.java
│       │   │       └── DebitNoteInvoiceGateway.java
│       │   └── queries
│       │       └── services
│       │           ├── InvoiceDebitNoteDetailsQueryService.java
│       │           └── RepositoryDebitNoteLedgerQuery.java
│       ├── domain
│       │   ├── entity
│       │   │   └── DebitNote.java
│       │   ├── repository
│       │   │   └── DebitNoteRepository.java
│       │   └── valueobjects
│       │       ├── DebitNoteId.java
│       │       └── DebitNoteNumber.java
│       └── infrastructure
│           ├── config
│           │   └── DebitNoteLedgerQueryConfig.java
│           ├── integration
│           │   ├── LocalDebitNoteCashGateway.java
│           │   └── LocalDebitNoteInvoiceGateway.java
│           ├── persistence
│           │   ├── adapters
│           │   │   └── DebitNoteRepositoryAdapter.java
│           │   ├── entities
│           │   │   ├── DebitNoteEntity.java
│           │   │   └── DebitNoteNumberSequenceEntity.java
│           │   ├── mappers
│           │   │   └── DebitNoteJpaMapper.java
│           │   └── repositories
│           │       ├── DebitNoteJpaRepository.java
│           │       └── DebitNoteNumberSequenceJpaRepository.java
│           └── web
│               └── DebitNoteController.java
└── test
    └── debit_notes
        ├── application
        │   ├── architecture
        │   │   └── DebitNoteModuleBoundaryTest.java
        │   ├── create
        │   │   └── use_case
        │   │       └── CreateDebitNoteUseCaseTest.java
        │   ├── delete
        │   │   └── use_case
        │   │       └── DeleteDebitNoteUseCaseTest.java
        │   ├── get
        │   │   └── use_case
        │   │       └── GetDebitNoteByIdUseCaseTest.java
        │   ├── list
        │   │   └── use_case
        │   │       └── GetDebitNotesUseCaseTest.java
        │   ├── mappers
        │   │   └── DebitNoteMapperTest.java
        │   └── queries
        │       └── services
        │           └── RepositoryDebitNoteLedgerQueryTest.java
        ├── domain
        │   ├── entity
        │   │   └── DebitNoteUnitTest.java
        │   └── valueobjects
        │       ├── DebitNoteIdUnitTest.java
        │       └── DebitNoteNumberUnitTest.java
        └── infrastructure
            ├── integration
            │   └── LocalDebitNoteInvoiceGatewayTest.java
            ├── persistence
            │   └── adapters
            │       └── DebitNoteRepositoryAdapterTest.java
            └── web
                └── DebitNoteControllerTest.java
```

### infrastructure

```text
├── main
│   └── infrastructure
│       ├── config
│       │   ├── CorsConfig.java
│       │   ├── GlobalExceptionHandler.java
│       │   ├── JpaAuditingConfig.java
│       │   ├── MultipartConfig.java
│       │   └── OpenApiConfig.java
│       └── web
│           └── PartMultipartFile.java
└── test
    └── infrastructure
        └── config
            └── GlobalExceptionHandlerUnitTest.java
```

### invoices

```text
├── main
│   └── invoices
│       ├── application
│       │   ├── cancel
│       │   │   ├── request
│       │   │   │   └── CancelInvoiceRequest.java
│       │   │   └── use_case
│       │   │       └── CancelInvoiceUseCase.java
│       │   ├── confirm
│       │   │   ├── request
│       │   │   │   └── ConfirmInvoiceRequest.java
│       │   │   └── use_case
│       │   │       └── ConfirmInvoiceUseCase.java
│       │   ├── create
│       │   │   ├── from_order
│       │   │   │   └── services
│       │   │   │       └── OrderInvoiceCreationService.java
│       │   │   ├── from_quote
│       │   │   │   └── services
│       │   │   │       └── QuoteInvoiceCreationService.java
│       │   │   ├── request
│       │   │   │   └── CreateInvoiceRequest.java
│       │   │   └── use_case
│       │   │       └── CreateInvoiceUseCase.java
│       │   ├── delete
│       │   │   └── use_case
│       │   │       └── DeleteInvoiceUseCase.java
│       │   ├── dto
│       │   │   └── response
│       │   │       ├── InvoiceDetailResponse.java
│       │   │       └── InvoiceListResponse.java
│       │   ├── duplicate
│       │   │   ├── request
│       │   │   │   └── DuplicateInvoiceRequest.java
│       │   │   └── use_case
│       │   │       └── DuplicateInvoiceUseCase.java
│       │   ├── get
│       │   │   └── use_case
│       │   │       └── GetInvoiceByIdUseCase.java
│       │   ├── issue
│       │   │   ├── model
│       │   │   │   └── InvoiceOrigin.java
│       │   │   ├── request
│       │   │   │   └── IssueInvoiceCommand.java
│       │   │   ├── services
│       │   │   │   ├── InvoicePaymentService.java
│       │   │   │   └── IssueInvoiceService.java
│       │   │   └── validation
│       │   │       └── InvoiceIssueValidator.java
│       │   ├── list
│       │   │   └── use_case
│       │   │       └── GetInvoicesUseCase.java
│       │   ├── mappers
│       │   │   └── InvoiceMapper.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   ├── InvoiceCreditLedgerEntry.java
│       │   │   │   ├── InvoiceCreditLedgerQuery.java
│       │   │   │   ├── InvoiceDocumentFilter.java
│       │   │   │   ├── InvoiceDocumentQuery.java
│       │   │   │   ├── InvoiceDocumentRow.java
│       │   │   │   ├── InvoiceDocumentSnapshot.java
│       │   │   │   ├── InvoiceNoteApi.java
│       │   │   │   ├── InvoiceReturnAdjustments.java
│       │   │   │   ├── InvoiceReturnCommand.java
│       │   │   │   ├── OrderInvoiceCreationApi.java
│       │   │   │   ├── ProductReferenceQuery.java
│       │   │   │   ├── QuoteInvoiceCreationApi.java
│       │   │   │   └── ReturnInvoiceQuery.java
│       │   │   └── out
│       │   │       ├── ActorIdentityGateway.java
│       │   │       ├── CashRegisterGateway.java
│       │   │       ├── CustomerBalanceGateway.java
│       │   │       ├── CustomerCreditGateway.java
│       │   │       ├── CustomerCreditTermsGateway.java
│       │   │       ├── InventoryGateway.java
│       │   │       ├── InvoiceCatalogGateway.java
│       │   │       ├── InvoiceCustomerGateway.java
│       │   │       ├── InvoiceFinancialPositionGateway.java
│       │   │       ├── InvoiceNoteDetailsGateway.java
│       │   │       ├── InvoiceNotificationGateway.java
│       │   │       ├── InvoiceOrderGateway.java
│       │   │       ├── InvoicePaymentSettlementGateway.java
│       │   │       ├── InvoicePaymentTotalsGateway.java
│       │   │       ├── InvoiceQuoteGateway.java
│       │   │       ├── InvoiceReturnDetailsGateway.java
│       │   │       ├── StockAvailabilityGateway.java
│       │   │       └── StockDeductionGateway.java
│       │   ├── queries
│       │   │   └── services
│       │   │       ├── InvoiceDocumentQueryService.java
│       │   │       ├── RepositoryInvoiceCreditLedgerQuery.java
│       │   │       ├── RepositoryInvoiceReturnAdjustments.java
│       │   │       └── RepositoryReturnInvoiceQuery.java
│       │   └── update
│       │       ├── request
│       │       │   └── UpdateInvoiceRequest.java
│       │       └── use_case
│       │           └── UpdateInvoiceUseCase.java
│       ├── domain
│       │   ├── entity
│       │   │   ├── Invoice.java
│       │   │   ├── InvoiceLine.java
│       │   │   └── InvoiceLineAllocation.java
│       │   ├── exceptions
│       │   │   ├── InvoiceNotFoundException.java
│       │   │   ├── InvoiceNotReturnableException.java
│       │   │   └── ReturnQuantityExceedsException.java
│       │   ├── repository
│       │   │   └── InvoiceRepository.java
│       │   ├── services
│       │   │   └── InvoiceStatusService.java
│       │   └── valueobjects
│       │       ├── InvoiceId.java
│       │       ├── InvoiceLineId.java
│       │       ├── InvoiceNumber.java
│       │       ├── InvoiceReturnAdjustment.java
│       │       ├── InvoiceReturnStatus.java
│       │       ├── InvoiceStatus.java
│       │       └── PaymentType.java
│       ├── infrastructure
│       │   ├── config
│       │   │   ├── InvoiceCreditQueryConfig.java
│       │   │   ├── InvoiceDocumentQueryConfig.java
│       │   │   ├── InvoiceReturnAdjustmentConfig.java
│       │   │   ├── OrderInvoiceCreationConfig.java
│       │   │   ├── QuoteInvoiceCreationConfig.java
│       │   │   └── ReturnInvoiceQueryConfig.java
│       │   ├── integration
│       │   │   ├── InvoiceCustomerCreditAdapter.java
│       │   │   ├── InvoiceStockAvailabilityAdapter.java
│       │   │   ├── LocalActorIdentityGateway.java
│       │   │   ├── LocalCustomerBalanceGateway.java
│       │   │   ├── LocalCustomerCreditTermsGateway.java
│       │   │   ├── LocalInventoryGatewayAdapter.java
│       │   │   ├── LocalInvoiceCashRegisterGateway.java
│       │   │   ├── LocalInvoiceCatalogGateway.java
│       │   │   ├── LocalInvoiceCustomerGateway.java
│       │   │   ├── LocalInvoiceFinancialPositionGateway.java
│       │   │   ├── LocalInvoiceNoteApi.java
│       │   │   ├── LocalInvoiceNoteDetailsAdapter.java
│       │   │   ├── LocalInvoiceNotificationGateway.java
│       │   │   ├── LocalInvoiceOrderGateway.java
│       │   │   ├── LocalInvoicePaymentSettlementGateway.java
│       │   │   ├── LocalInvoicePaymentTotalsAdapter.java
│       │   │   ├── LocalInvoiceQuoteGateway.java
│       │   │   ├── LocalInvoiceReturnDetailsAdapter.java
│       │   │   └── LocalStockDeductionGateway.java
│       │   ├── persistence
│       │   │   ├── adapters
│       │   │   │   └── InvoiceRepositoryAdapter.java
│       │   │   ├── entities
│       │   │   │   ├── InvoiceEntity.java
│       │   │   │   ├── InvoiceLineAllocationEntity.java
│       │   │   │   ├── InvoiceLineEntity.java
│       │   │   │   └── InvoiceNumberSequenceEntity.java
│       │   │   ├── mappers
│       │   │   │   └── InvoiceJpaMapper.java
│       │   │   ├── queries
│       │   │   │   └── JpaInvoicesProductReferenceQuery.java
│       │   │   └── repositories
│       │   │       ├── InvoiceJpaRepository.java
│       │   │       ├── InvoiceNumberSequenceJpaRepository.java
│       │   │       └── SpringDataInvoicesProductReferenceRepository.java
│       │   ├── services
│       │   │   └── InvoiceStatusServiceImpl.java
│       │   └── web
│       │       └── InvoiceController.java
│       └── reporting
│           ├── application
│           │   ├── dto
│           │   │   └── model
│           │   │       ├── InvoiceReportData.java
│           │   │       ├── InvoiceReportFilter.java
│           │   │       └── InvoiceReportRow.java
│           │   ├── generate_invoice_pdf
│           │   │   ├── services
│           │   │   │   └── GenerateInvoicePdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GenerateInvoicePdfUseCase.java
│           │   ├── generate_invoices_report_pdf
│           │   │   ├── services
│           │   │   │   └── GenerateInvoicesReportPdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GenerateInvoicesReportPdfUseCase.java
│           │   └── ports
│           │       └── out
│           │           ├── InvoicePdfPort.java
│           │           ├── InvoiceReportQueryGateway.java
│           │           └── InvoicesReportPdfPort.java
│           └── infrastructure
│               ├── config
│               │   └── InvoicesReportConfig.java
│               ├── integration
│               │   └── InvoiceDocumentQueryAdapter.java
│               ├── pdf
│               │   ├── InvoicePdfGenerator.java
│               │   └── InvoicesReportPdfGenerator.java
│               └── web
│                   └── InvoicesReportController.java
└── test
    └── invoices
        ├── application
        │   ├── architecture
        │   │   ├── operations
        │   │   │   └── services
        │   │   │       └── InvoiceEditingBoundaryTest.java
        │   │   ├── CreditConsumerBoundaryTest.java
        │   │   ├── InvoiceAuthorizationBoundaryTest.java
        │   │   ├── InvoiceCreationBoundaryTest.java
        │   │   ├── InvoiceEditingBoundaryTest.java
        │   │   ├── InvoiceInventoryBoundaryTest.java
        │   │   └── StockReadBoundaryTest.java
        │   ├── cancel
        │   │   └── use_case
        │   │       └── CancelInvoiceUseCaseUnitTest.java
        │   ├── confirm
        │   │   └── use_case
        │   │       └── ConfirmInvoiceUseCaseTest.java
        │   ├── create
        │   │   └── use_case
        │   │       └── CreateInvoiceUseCaseTest.java
        │   ├── delete
        │   │   └── use_case
        │   │       └── DeleteInvoiceUseCaseTest.java
        │   ├── duplicate
        │   │   └── use_case
        │   │       └── DuplicateInvoiceUseCaseTest.java
        │   ├── get
        │   │   └── use_case
        │   │       └── GetInvoiceByIdUseCaseUnitTest.java
        │   ├── issue
        │   │   ├── architecture
        │   │   │   └── InvoiceIssuanceArchitectureTest.java
        │   │   └── services
        │   │       ├── InvoiceIssuanceFinancialFlowTest.java
        │   │       └── InvoiceIssuanceTest.java
        │   ├── list
        │   │   └── use_case
        │   │       └── GetInvoicesUseCaseTest.java
        │   ├── mappers
        │   │   └── InvoiceMapperTest.java
        │   ├── ports
        │   │   └── in
        │   │       └── InvoiceReturnCommandValidationTest.java
        │   ├── queries
        │   │   └── services
        │   │       ├── RepositoryInvoiceCreditLedgerQueryTest.java
        │   │       └── RepositoryInvoiceReturnAdjustmentsTest.java
        │   └── update
        │       └── use_case
        │           └── UpdateInvoiceUseCaseTest.java
        ├── domain
        │   ├── entity
        │   │   ├── GetInvoiceByIdUseCaseNotesTest.java
        │   │   ├── InvoiceLineAllocationUnitTest.java
        │   │   ├── InvoiceLineUnitTest.java
        │   │   ├── InvoiceReturnBoundaryTest.java
        │   │   └── InvoiceUnitTest.java
        │   ├── exceptions
        │   │   └── InvoiceNotFoundExceptionTest.java
        │   └── valueobjects
        │       ├── InvoiceIdUnitTest.java
        │       ├── InvoiceLineIdUnitTest.java
        │       ├── InvoiceNumberUnitTest.java
        │       ├── InvoiceReturnAdjustmentTest.java
        │       ├── InvoiceStatusUnitTest.java
        │       └── PaymentTypeUnitTest.java
        ├── infrastructure
        │   ├── integration
        │   │   ├── CustomerCreditTermsContractTest.java
        │   │   ├── InvoiceAuthorizationWiringTest.java
        │   │   ├── LocalInventoryGatewayAdapterTest.java
        │   │   ├── LocalInvoiceFinancialPositionGatewayTest.java
        │   │   ├── LocalInvoiceNoteApiTest.java
        │   │   ├── LocalInvoiceNoteDetailsAdapterTest.java
        │   │   └── LocalInvoiceReturnDetailsAdapterTest.java
        │   ├── integration_tests
        │   │   └── CashInvoiceConversionMySqlIT.java
        │   ├── persistence
        │   │   ├── adapters
        │   │   │   └── InvoiceRepositoryAdapterTest.java
        │   │   └── entities
        │   │       └── InvoiceLineEntityUnitTest.java
        │   ├── services
        │   │   └── InvoiceStatusServiceImplTest.java
        │   └── web
        │       └── InvoiceControllerTest.java
        └── reporting
            ├── application
            │   ├── architecture
            │   │   └── InvoiceReportsBoundaryTest.java
            │   ├── generate_invoice_pdf
            │   │   └── services
            │   │       └── GenerateInvoicePdfUseCaseImplTest.java
            │   └── generate_invoices_report_pdf
            │       └── services
            │           └── GenerateInvoicesReportPdfUseCaseImplTest.java
            └── infrastructure
                ├── config
                │   └── InvoicesReportConfigTest.java
                ├── integration
                │   └── InvoiceDocumentQueryAdapterTest.java
                ├── pdf
                │   └── InvoicePdfContentTest.java
                └── web
                    └── InvoicesReportControllerTest.java
```

### notifications

```text
├── main
│   └── notifications
│       ├── application
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   ├── QuoteConversionNotifications.java
│       │   │   │   └── QuoteRejectionNotifications.java
│       │   │   └── out
│       │   │       ├── NotificationDeliveryGateway.java
│       │   │       └── NotificationRecipientGateway.java
│       │   ├── quotes
│       │   │   └── services
│       │   │       ├── QuoteConversionNotificationService.java
│       │   │       └── QuoteRejectionNotificationService.java
│       │   └── services
│       │       └── NotificationService.java
│       ├── domain
│       │   ├── entity
│       │   │   └── Notification.java
│       │   ├── repository
│       │   │   └── NotificationRepository.java
│       │   └── valueobjects
│       │       ├── NotificationId.java
│       │       └── NotificationType.java
│       └── infrastructure
│           ├── config
│           │   └── WebSocketConfig.java
│           ├── integration
│           │   └── AuthNotificationRecipientAdapter.java
│           ├── messaging
│           │   └── WebSocketNotificationDeliveryAdapter.java
│           ├── persistence
│           │   ├── adapters
│           │   │   └── NotificationRepositoryAdapter.java
│           │   ├── entities
│           │   │   └── NotificationEntity.java
│           │   ├── mappers
│           │   │   └── NotificationJpaMapper.java
│           │   └── repositories
│           │       └── NotificationJpaRepository.java
│           └── web
│               └── NotificationController.java
└── test
    └── notifications
        ├── application
        │   └── services
        │       └── NotificationServiceTest.java
        └── infrastructure
            └── integration
                └── AuthNotificationRecipientAdapterTest.java
```

### orders

```text
├── main
│   └── orders
│       ├── application
│       │   ├── cancel
│       │   │   ├── request
│       │   │   │   └── CancelOrderRequest.java
│       │   │   └── use_case
│       │   │       └── CancelOrderUseCase.java
│       │   ├── confirm
│       │   │   └── use_case
│       │   │       └── ConfirmOrderUseCase.java
│       │   ├── convert_to_invoice
│       │   │   ├── request
│       │   │   │   └── ConvertToInvoiceRequest.java
│       │   │   ├── response
│       │   │   │   └── ConvertToInvoiceResponse.java
│       │   │   └── use_case
│       │   │       └── ConvertToInvoiceUseCase.java
│       │   ├── create
│       │   │   ├── from_quote
│       │   │   │   └── services
│       │   │   │       └── QuoteOrderCreationService.java
│       │   │   ├── request
│       │   │   │   └── CreateOrderRequest.java
│       │   │   └── use_case
│       │   │       └── CreateOrderUseCase.java
│       │   ├── delete
│       │   │   └── use_case
│       │   │       └── DeleteOrderUseCase.java
│       │   ├── dto
│       │   │   └── response
│       │   │       ├── OrderDetailResponse.java
│       │   │       ├── OrderListResponse.java
│       │   │       └── OrderProductResponse.java
│       │   ├── get
│       │   │   └── use_case
│       │   │       └── GetOrderByIdUseCase.java
│       │   ├── list
│       │   │   └── use_case
│       │   │       └── GetOrdersUseCase.java
│       │   ├── mappers
│       │   │   └── OrderMapper.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   ├── OrderDocumentFilter.java
│       │   │   │   ├── OrderDocumentQuery.java
│       │   │   │   ├── OrderDocumentRow.java
│       │   │   │   ├── OrderDocumentSnapshot.java
│       │   │   │   ├── ProductReferenceQuery.java
│       │   │   │   └── QuoteOrderCreationApi.java
│       │   │   └── out
│       │   │       ├── CustomerCreditGateway.java
│       │   │       ├── InvoiceCreationGateway.java
│       │   │       ├── OrderActorGateway.java
│       │   │       ├── OrderCustomerGateway.java
│       │   │       ├── OrderInventoryGateway.java
│       │   │       ├── OrderNotificationGateway.java
│       │   │       ├── OrderProductCatalogGateway.java
│       │   │       └── StockAvailabilityGateway.java
│       │   ├── products
│       │   │   └── use_case
│       │   │       └── GetProductsForOrderUseCase.java
│       │   ├── queries
│       │   │   └── services
│       │   │       └── OrderDocumentQueryService.java
│       │   └── update
│       │       ├── request
│       │       │   └── UpdateOrderRequest.java
│       │       └── use_case
│       │           └── UpdateOrderUseCase.java
│       ├── domain
│       │   ├── entity
│       │   │   ├── Order.java
│       │   │   ├── OrderLine.java
│       │   │   └── OrderLineAllocation.java
│       │   ├── exceptions
│       │   │   ├── OrderCannotBeDeletedException.java
│       │   │   ├── OrderNotFoundException.java
│       │   │   └── OrderNotPendingException.java
│       │   ├── repository
│       │   │   └── OrderRepository.java
│       │   └── valueobjects
│       │       ├── OrderId.java
│       │       ├── OrderLineId.java
│       │       ├── OrderNumber.java
│       │       └── OrderStatus.java
│       ├── infrastructure
│       │   ├── config
│       │   │   ├── OrderConfig.java
│       │   │   ├── OrderDocumentQueryConfig.java
│       │   │   └── QuoteOrderCreationConfig.java
│       │   ├── integration
│       │   │   ├── LocalInvoiceCreationGateway.java
│       │   │   ├── LocalOrderActorAdapter.java
│       │   │   ├── LocalOrderCustomerAdapter.java
│       │   │   ├── LocalOrderInventoryGateway.java
│       │   │   ├── LocalOrderNotificationAdapter.java
│       │   │   ├── LocalOrderProductCatalogAdapter.java
│       │   │   ├── OrderCustomerCreditAdapter.java
│       │   │   └── OrderStockAvailabilityAdapter.java
│       │   ├── persistence
│       │   │   ├── adapters
│       │   │   │   └── OrderRepositoryAdapter.java
│       │   │   ├── entities
│       │   │   │   ├── OrderEntity.java
│       │   │   │   ├── OrderLineAllocationEntity.java
│       │   │   │   ├── OrderLineCategorySnapshotEntity.java
│       │   │   │   ├── OrderLineEntity.java
│       │   │   │   └── OrderNumberSequenceEntity.java
│       │   │   ├── mappers
│       │   │   │   └── OrderJpaMapper.java
│       │   │   ├── queries
│       │   │   │   └── JpaOrdersProductReferenceQuery.java
│       │   │   └── repositories
│       │   │       ├── OrderJpaRepository.java
│       │   │       ├── OrderNumberSequenceJpaRepository.java
│       │   │       └── SpringDataOrdersProductReferenceRepository.java
│       │   └── web
│       │       └── OrderController.java
│       └── reporting
│           ├── application
│           │   ├── dto
│           │   │   └── model
│           │   │       ├── OrderReportData.java
│           │   │       ├── OrderReportFilter.java
│           │   │       └── OrderReportRow.java
│           │   ├── generate_order_pdf
│           │   │   ├── services
│           │   │   │   └── GenerateOrderPdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GenerateOrderPdfUseCase.java
│           │   ├── generate_orders_report_pdf
│           │   │   ├── services
│           │   │   │   └── GenerateOrdersReportPdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GenerateOrdersReportPdfUseCase.java
│           │   └── ports
│           │       └── out
│           │           ├── OrderPdfPort.java
│           │           ├── OrderReportQueryGateway.java
│           │           └── OrdersReportPdfPort.java
│           └── infrastructure
│               ├── config
│               │   └── OrdersReportConfig.java
│               ├── integration
│               │   └── OrderDocumentQueryAdapter.java
│               ├── pdf
│               │   ├── OrderPdfGenerator.java
│               │   └── OrdersReportPdfGenerator.java
│               └── web
│                   └── OrdersReportController.java
└── test
    └── orders
        ├── application
        │   ├── architecture
        │   │   ├── GetOrdersCategoryQueryBoundaryTest.java
        │   │   └── OrderInvoiceConversionBoundaryTest.java
        │   ├── cancel
        │   │   └── use_case
        │   │       └── CancelOrderUseCaseUnitTest.java
        │   ├── confirm
        │   │   └── use_case
        │   │       └── ConfirmOrderUseCaseUnitTest.java
        │   ├── convert_to_invoice
        │   │   └── use_case
        │   │       └── ConvertToInvoiceUseCaseUnitTest.java
        │   ├── create
        │   │   └── use_case
        │   │       └── CreateOrderUseCaseUnitTest.java
        │   ├── delete
        │   │   └── use_case
        │   │       └── DeleteOrderUseCaseUnitTest.java
        │   ├── get
        │   │   └── use_case
        │   │       └── GetOrderByIdUseCaseUnitTest.java
        │   ├── list
        │   │   └── use_case
        │   │       └── GetOrdersUseCaseTest.java
        │   ├── mappers
        │   │   └── OrderMapperTest.java
        │   ├── products
        │   │   └── use_case
        │   │       └── GetProductsForOrderUseCaseUnitTest.java
        │   └── update
        │       └── use_case
        │           └── UpdateOrderUseCaseUnitTest.java
        ├── domain
        │   ├── entity
        │   │   ├── OrderLineUnitTest.java
        │   │   └── OrderUnitTest.java
        │   ├── exceptions
        │   │   ├── OrderCannotBeDeletedExceptionTest.java
        │   │   ├── OrderNotFoundExceptionTest.java
        │   │   └── OrderNotPendingExceptionTest.java
        │   └── valueobjects
        │       ├── OrderIdUnitTest.java
        │       ├── OrderNumberUnitTest.java
        │       └── OrderStatusUnitTest.java
        ├── infrastructure
        │   ├── config
        │   │   └── OrderConfigTest.java
        │   ├── integration
        │   │   └── InvoiceCreationContractTest.java
        │   ├── integration_tests
        │   │   └── OrderHistoricalCategoryMySqlIT.java
        │   ├── persistence
        │   │   ├── adapters
        │   │   │   └── OrderRepositoryAdapterTest.java
        │   │   └── mappers
        │   │       └── OrderCategorySnapshotMappingTest.java
        │   └── web
        │       ├── OrderControllerTest.java
        │       └── OrderWarehouseReassignmentRemovalTest.java
        └── reporting
            ├── application
            │   ├── architecture
            │   │   └── OrderReportsBoundaryTest.java
            │   ├── generate_order_pdf
            │   │   └── services
            │   │       └── GenerateOrderPdfUseCaseImplTest.java
            │   └── generate_orders_report_pdf
            │       └── services
            │           └── GenerateOrdersReportPdfUseCaseImplTest.java
            └── infrastructure
                ├── config
                │   └── OrdersReportConfigTest.java
                ├── integration
                │   └── OrderDocumentQueryAdapterTest.java
                ├── pdf
                │   └── OrderPdfContentTest.java
                └── web
                    └── OrdersReportControllerTest.java
```

### payment_methods

```text
├── main
│   └── payment_methods
│       ├── application
│       │   ├── create
│       │   │   ├── request
│       │   │   │   └── CreatePaymentMethodRequest.java
│       │   │   └── use_case
│       │   │       └── CreatePaymentMethodUseCase.java
│       │   ├── delete
│       │   │   └── use_case
│       │   │       └── DeletePaymentMethodUseCase.java
│       │   ├── dto
│       │   │   └── response
│       │   │       └── PaymentMethodResponse.java
│       │   ├── mappers
│       │   │   └── PaymentMethodMapper.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   └── PaymentMethodDetailsQuery.java
│       │   │   └── out
│       │   │       └── PaymentMethodUsageGateway.java
│       │   ├── queries
│       │   │   ├── services
│       │   │   │   └── RepositoryPaymentMethodDetailsQuery.java
│       │   │   └── use_case
│       │   │       ├── GetPaymentMethodByIdUseCase.java
│       │   │       └── ListPaymentMethodsUseCase.java
│       │   ├── toggle
│       │   │   └── use_case
│       │   │       └── TogglePaymentMethodUseCase.java
│       │   └── update
│       │       ├── request
│       │       │   └── UpdatePaymentMethodRequest.java
│       │       └── use_case
│       │           └── UpdatePaymentMethodUseCase.java
│       ├── domain
│       │   ├── entity
│       │   │   └── PaymentMethod.java
│       │   ├── repository
│       │   │   └── PaymentMethodRepository.java
│       │   └── valueobjects
│       │       └── PaymentMethodId.java
│       └── infrastructure
│           ├── integration
│           │   └── LocalPaymentMethodUsageGateway.java
│           ├── persistence
│           │   ├── adapters
│           │   │   └── PaymentMethodRepositoryAdapter.java
│           │   ├── entities
│           │   │   └── PaymentMethodEntity.java
│           │   ├── mappers
│           │   │   └── PaymentMethodJpaMapper.java
│           │   └── repositories
│           │       └── PaymentMethodJpaRepository.java
│           └── web
│               └── PaymentMethodController.java
└── test
    └── payment_methods
        ├── application
        │   ├── architecture
        │   │   └── PaymentMethodBoundaryTest.java
        │   ├── create
        │   │   └── use_case
        │   │       └── CreatePaymentMethodUseCaseTest.java
        │   ├── delete
        │   │   └── use_case
        │   │       └── DeletePaymentMethodUseCaseTest.java
        │   ├── mappers
        │   │   └── PaymentMethodMapperTest.java
        │   ├── queries
        │   │   └── use_case
        │   │       ├── GetPaymentMethodByIdUseCaseTest.java
        │   │       └── ListPaymentMethodsUseCaseTest.java
        │   ├── toggle
        │   │   └── use_case
        │   │       └── TogglePaymentMethodUseCaseTest.java
        │   └── update
        │       └── use_case
        │           └── UpdatePaymentMethodUseCaseTest.java
        ├── domain
        │   ├── entity
        │   │   └── PaymentMethodUnitTest.java
        │   └── valueobjects
        │       └── PaymentMethodIdUnitTest.java
        └── infrastructure
            ├── integration
            │   ├── LocalPaymentMethodUsageGatewayTest.java
            │   └── PaymentMethodUsageWiringTest.java
            ├── persistence
            │   └── adapters
            │       └── PaymentMethodRepositoryAdapterTest.java
            └── web
                └── PaymentMethodControllerTest.java
```

### payments

```text
├── main
│   └── payments
│       ├── collection
│       │   ├── application
│       │   │   ├── approve
│       │   │   │   ├── request
│       │   │   │   │   └── ApprovePaymentRequest.java
│       │   │   │   └── use_case
│       │   │   │       └── ApprovePaymentUseCase.java
│       │   │   ├── create
│       │   │   │   ├── request
│       │   │   │   │   └── CreatePaymentRequest.java
│       │   │   │   └── use_case
│       │   │   │       └── CreatePaymentUseCase.java
│       │   │   ├── delete
│       │   │   │   └── use_case
│       │   │   │       └── DeletePaymentUseCase.java
│       │   │   ├── dto
│       │   │   │   └── response
│       │   │   │       ├── PaymentListResponse.java
│       │   │   │       ├── PaymentResponse.java
│       │   │   │       └── PaymentResultResponse.java
│       │   │   ├── mappers
│       │   │   │   └── PaymentMapper.java
│       │   │   ├── pay_customer_debt
│       │   │   │   ├── request
│       │   │   │   │   └── PayCustomerDebtRequest.java
│       │   │   │   └── use_case
│       │   │   │       └── PayAllCustomerDebtUseCase.java
│       │   │   ├── pay_invoice
│       │   │   │   ├── request
│       │   │   │   │   └── PayInvoiceRequest.java
│       │   │   │   └── use_case
│       │   │   │       └── PayInvoiceUseCase.java
│       │   │   ├── pay_partial_debt
│       │   │   │   ├── request
│       │   │   │   │   └── PayPartialDebtRequest.java
│       │   │   │   └── use_case
│       │   │   │       └── PayPartialDebtUseCase.java
│       │   │   ├── ports
│       │   │   │   ├── in
│       │   │   │   │   ├── CashInvoiceSettlement.java
│       │   │   │   │   ├── InvoicePaymentTotalQuery.java
│       │   │   │   │   ├── InvoicePaymentTotalsQuery.java
│       │   │   │   │   ├── PaymentDocumentQuery.java
│       │   │   │   │   ├── PaymentDocumentSnapshot.java
│       │   │   │   │   ├── QueryPaymentMethodUsage.java
│       │   │   │   │   └── ReturnPaymentOperations.java
│       │   │   │   └── out
│       │   │   │       ├── CashRegisterGateway.java
│       │   │   │       ├── PaymentActorGateway.java
│       │   │   │       ├── PaymentCustomerGateway.java
│       │   │   │       ├── PaymentInvoiceGateway.java
│       │   │   │       └── PaymentNotificationGateway.java
│       │   │   ├── queries
│       │   │   │   └── use_case
│       │   │   │       ├── GetAllPaymentsUseCase.java
│       │   │   │       └── GetPaymentByIdUseCase.java
│       │   │   ├── reject
│       │   │   │   ├── request
│       │   │   │   │   └── RejectPaymentRequest.java
│       │   │   │   └── use_case
│       │   │   │       └── RejectPaymentUseCase.java
│       │   │   ├── services
│       │   │   │   ├── CashInvoiceSettlementService.java
│       │   │   │   ├── PaymentDocumentQueryService.java
│       │   │   │   └── ReturnPaymentOperationsService.java
│       │   │   └── update
│       │   │       ├── request
│       │   │       │   └── UpdatePaymentRequest.java
│       │   │       └── use_case
│       │   │           └── UpdatePaymentUseCase.java
│       │   ├── domain
│       │   │   ├── entity
│       │   │   │   ├── Payment.java
│       │   │   │   └── PaymentAllocation.java
│       │   │   ├── repository
│       │   │   │   ├── PaymentAllocationRepository.java
│       │   │   │   └── PaymentRepository.java
│       │   │   ├── services
│       │   │   │   └── PaymentAllocationService.java
│       │   │   └── valueobjects
│       │   │       ├── InvoiceRemainingBalance.java
│       │   │       ├── PaymentId.java
│       │   │       ├── PaymentNumber.java
│       │   │       ├── PaymentSource.java
│       │   │       └── PaymentStatus.java
│       │   └── infrastructure
│       │       ├── config
│       │       │   ├── CashInvoiceSettlementConfig.java
│       │       │   ├── PaymentDocumentQueryConfig.java
│       │       │   └── ReturnPaymentOperationsConfig.java
│       │       ├── integration
│       │       │   ├── LocalPaymentActorGateway.java
│       │       │   ├── LocalPaymentCashRegisterGateway.java
│       │       │   ├── LocalPaymentCustomerGateway.java
│       │       │   ├── LocalPaymentInvoiceGateway.java
│       │       │   ├── LocalPaymentNotificationGateway.java
│       │       │   ├── RepositoryInvoicePaymentTotalQuery.java
│       │       │   └── RepositoryInvoicePaymentTotalsQuery.java
│       │       ├── persistence
│       │       │   ├── adapters
│       │       │   │   ├── PaymentAllocationRepositoryAdapter.java
│       │       │   │   └── PaymentRepositoryAdapter.java
│       │       │   ├── entities
│       │       │   │   ├── PaymentAllocationEntity.java
│       │       │   │   └── PaymentEntity.java
│       │       │   ├── mappers
│       │       │   │   ├── PaymentAllocationJpaMapper.java
│       │       │   │   └── PaymentJpaMapper.java
│       │       │   ├── queries
│       │       │   │   └── JpaPaymentMethodUsageQuery.java
│       │       │   └── repositories
│       │       │       ├── PaymentAllocationJpaRepository.java
│       │       │       └── PaymentJpaRepository.java
│       │       └── web
│       │           └── PaymentController.java
│       ├── credit
│       │   ├── application
│       │   │   ├── ports
│       │   │   │   ├── in
│       │   │   │   │   ├── CreditStatusSnapshot.java
│       │   │   │   │   ├── CustomerCreditQuery.java
│       │   │   │   │   └── OverdueInvoiceSnapshot.java
│       │   │   │   └── out
│       │   │   │       ├── model
│       │   │   │       │   ├── CustomerCreditContext.java
│       │   │   │       │   └── InvoiceCreditEntry.java
│       │   │   │       ├── CreditNoteLedgerGateway.java
│       │   │   │       ├── CustomerCreditContextGateway.java
│       │   │   │       ├── DebitNoteLedgerGateway.java
│       │   │   │       └── InvoiceCreditLedgerGateway.java
│       │   │   └── services
│       │   │       └── CustomerCreditQueryService.java
│       │   ├── domain
│       │   │   └── services
│       │   │       └── CustomerCreditPolicy.java
│       │   └── infrastructure
│       │       └── integration
│       │           ├── LocalCreditNoteLedgerGateway.java
│       │           ├── LocalCustomerCreditContextGateway.java
│       │           ├── LocalDebitNoteLedgerGateway.java
│       │           └── LocalInvoiceCreditLedgerGateway.java
│       ├── infrastructure
│       │   └── config
│       │       └── PaymentConfig.java
│       └── reporting
│           ├── application
│           │   ├── dto
│           │   │   └── model
│           │   │       ├── PaymentHistoryReportData.java
│           │   │       └── PaymentReportData.java
│           │   ├── generate_payment_history_report_pdf
│           │   │   ├── services
│           │   │   │   └── GeneratePaymentHistoryReportPdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GeneratePaymentHistoryReportPdfUseCase.java
│           │   ├── generate_payment_pdf
│           │   │   ├── services
│           │   │   │   └── GeneratePaymentPdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GeneratePaymentPdfUseCase.java
│           │   └── ports
│           │       └── out
│           │           ├── PaymentHistoryReportPdfPort.java
│           │           ├── PaymentPdfPort.java
│           │           └── PaymentReportQueryGateway.java
│           └── infrastructure
│               ├── config
│               │   └── PaymentReportingConfig.java
│               ├── integration
│               │   └── PaymentDocumentQueryAdapter.java
│               ├── pdf
│               │   ├── PaymentHistoryReportPdfGenerator.java
│               │   └── PaymentPdfGenerator.java
│               └── web
│                   └── PaymentReportsController.java
└── test
    └── payments
        ├── application
        │   └── architecture
        │       ├── CustomerCreditQueryBoundaryTest.java
        │       └── PaymentModuleBoundaryTest.java
        ├── collection
        │   ├── application
        │   │   ├── approve
        │   │   │   └── use_case
        │   │   │       └── ApprovePaymentUseCaseTest.java
        │   │   ├── create
        │   │   │   ├── request
        │   │   │   │   └── CreatePaymentRequestTest.java
        │   │   │   └── use_case
        │   │   │       └── CreatePaymentUseCaseUnitTest.java
        │   │   ├── delete
        │   │   │   └── use_case
        │   │   │       └── DeletePaymentUseCaseTest.java
        │   │   ├── mappers
        │   │   │   └── PaymentMapperTest.java
        │   │   ├── pay_customer_debt
        │   │   │   └── use_case
        │   │   │       └── PayAllCustomerDebtUseCaseTest.java
        │   │   ├── pay_invoice
        │   │   │   └── use_case
        │   │   │       └── PayInvoiceUseCaseTest.java
        │   │   ├── pay_partial_debt
        │   │   │   └── use_case
        │   │   │       └── PayPartialDebtUseCaseTest.java
        │   │   ├── queries
        │   │   │   └── use_case
        │   │   │       ├── GetAllPaymentsUseCaseTest.java
        │   │   │       └── GetPaymentByIdUseCaseUnitTest.java
        │   │   ├── reject
        │   │   │   └── use_case
        │   │   │       └── RejectPaymentUseCaseTest.java
        │   │   ├── services
        │   │   │   ├── CashInvoiceSettlementServiceTest.java
        │   │   │   └── ReturnPaymentOperationsServiceTest.java
        │   │   └── update
        │   │       └── use_case
        │   │           └── UpdatePaymentUseCaseTest.java
        │   ├── domain
        │   │   ├── entity
        │   │   │   ├── PaymentAllocationTest.java
        │   │   │   └── PaymentUnitTest.java
        │   │   ├── services
        │   │   │   └── PaymentAllocationServiceTest.java
        │   │   └── valueobjects
        │   │       ├── InvoiceRemainingBalanceUnitTest.java
        │   │       ├── PaymentIdUnitTest.java
        │   │       ├── PaymentNumberUnitTest.java
        │   │       ├── PaymentSourceTest.java
        │   │       └── PaymentStatusTest.java
        │   └── infrastructure
        │       ├── integration
        │       │   ├── CreditLedgerWiringTest.java
        │       │   ├── LocalCreditLedgerGatewaysTest.java
        │       │   ├── LocalPaymentCashRegisterGatewayTest.java
        │       │   ├── LocalPaymentInvoiceGatewayTest.java
        │       │   └── RepositoryInvoicePaymentTotalQueryTest.java
        │       ├── persistence
        │       │   ├── adapters
        │       │   │   ├── PaymentAllocationRepositoryAdapterTest.java
        │       │   │   └── PaymentRepositoryAdapterTest.java
        │       │   └── queries
        │       │       └── JpaPaymentMethodUsageQueryTest.java
        │       └── web
        │           └── PaymentControllerTest.java
        ├── credit
        │   ├── application
        │   │   └── services
        │   │       └── CustomerCreditQueryServiceTest.java
        │   └── domain
        │       └── services
        │           └── CustomerCreditPolicyTest.java
        ├── infrastructure
        │   └── config
        │       └── PaymentConfigTest.java
        └── reporting
            ├── application
            │   ├── architecture
            │   │   └── PaymentReportsBoundaryTest.java
            │   ├── dto
            │   │   └── model
            │   │       └── PaymentHistoryReportDataTest.java
            │   ├── generate_payment_history_report_pdf
            │   │   └── services
            │   │       └── GeneratePaymentHistoryReportPdfUseCaseImplTest.java
            │   ├── generate_payment_pdf
            │   │   └── services
            │   │       └── GeneratePaymentPdfUseCaseImplTest.java
            │   └── ports
            │       └── out
            │           └── PaymentPdfPortTest.java
            └── infrastructure
                ├── config
                │   └── PaymentReportingConfigTest.java
                ├── integration
                │   └── PaymentDocumentQueryAdapterTest.java
                ├── pdf
                │   └── PaymentPdfContentTest.java
                └── web
                    └── PaymentReportsControllerTest.java
```

### price_types

```text
├── main
│   └── price_types
│       ├── application
│       │   ├── create
│       │   │   ├── request
│       │   │   │   └── CreatePriceTypeRequest.java
│       │   │   └── use_case
│       │   │       └── CreatePriceTypeUseCase.java
│       │   ├── delete
│       │   │   └── use_case
│       │   │       └── DeletePriceTypeUseCase.java
│       │   ├── dto
│       │   │   └── response
│       │   │       └── PriceTypeResponse.java
│       │   ├── exceptions
│       │   │   ├── DuplicatePriceTypeNameException.java
│       │   │   ├── PriceTypeInUseException.java
│       │   │   └── PriceTypeNotFoundException.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   ├── ProductPriceTypeExistenceQuery.java
│       │   │   │   └── ProductPriceTypeQuery.java
│       │   │   └── out
│       │   │       └── PriceTypeUsageGateway.java
│       │   ├── queries
│       │   │   ├── services
│       │   │   │   └── RepositoryProductPriceTypeQuery.java
│       │   │   └── use_case
│       │   │       ├── GetPriceTypeUseCase.java
│       │   │       └── ListPriceTypesUseCase.java
│       │   └── update
│       │       ├── request
│       │       │   └── UpdatePriceTypeRequest.java
│       │       └── use_case
│       │           └── UpdatePriceTypeUseCase.java
│       ├── domain
│       │   ├── entity
│       │   │   └── PriceType.java
│       │   └── repository
│       │       └── PriceTypeRepository.java
│       └── infrastructure
│           ├── config
│           │   ├── PriceTypeConfig.java
│           │   └── ProductPriceTypeQueryConfig.java
│           ├── integration
│           │   └── ProductPriceTypeUsageAdapter.java
│           ├── persistence
│           │   ├── adapters
│           │   │   └── JpaPriceTypeRepository.java
│           │   ├── entities
│           │   │   └── PriceTypeEntity.java
│           │   ├── queries
│           │   │   └── JpaProductPriceTypeExistenceQuery.java
│           │   └── repositories
│           │       └── SpringDataPriceTypeRepository.java
│           └── web
│               └── PriceTypeController.java
└── test
    └── price_types
        ├── application
        │   ├── create
        │   │   └── use_case
        │   │       └── CreatePriceTypeUseCaseUnitTest.java
        │   ├── delete
        │   │   └── use_case
        │   │       └── DeletePriceTypeUseCaseUnitTest.java
        │   ├── exceptions
        │   │   └── DuplicatePriceTypeNameExceptionTest.java
        │   ├── queries
        │   │   └── use_case
        │   │       ├── GetPriceTypeUseCaseUnitTest.java
        │   │       └── ListPriceTypesUseCaseTest.java
        │   └── update
        │       └── use_case
        │           └── UpdatePriceTypeUseCaseUnitTest.java
        ├── domain
        │   ├── entity
        │   │   └── PriceTypeUnitTest.java
        │   └── valueobjects
        │       └── ProductPriceUnitTest.java
        └── infrastructure
            ├── persistence
            │   └── adapters
            │       └── JpaPriceTypeRepositoryTest.java
            └── web
                └── PriceTypeControllerTest.java
```

### products

```text
├── main
│   └── products
│       ├── application
│       │   ├── create
│       │   │   ├── request
│       │   │   │   └── CreateProductRequest.java
│       │   │   └── use_case
│       │   │       └── CreateProductUseCase.java
│       │   ├── delete
│       │   │   └── use_case
│       │   │       └── DeleteProductUseCase.java
│       │   ├── dto
│       │   │   ├── request
│       │   │   │   └── ProductPriceRequest.java
│       │   │   └── response
│       │   │       ├── ProductDetailResponse.java
│       │   │       ├── ProductListResponse.java
│       │   │       └── ProductPriceDetailResponse.java
│       │   ├── exceptions
│       │   │   ├── CompositeProductCannotBeComponentException.java
│       │   │   ├── DuplicateProductCodeException.java
│       │   │   ├── DuplicateProductComponentException.java
│       │   │   ├── ProductHasAssociatedRecordsException.java
│       │   │   ├── ProductHasStockException.java
│       │   │   └── ProductNotFoundException.java
│       │   ├── mappers
│       │   │   └── ProductMapper.java
│       │   ├── photos
│       │   │   └── use_case
│       │   │       ├── RemoveProductPhotoUseCase.java
│       │   │       └── UploadProductPhotoUseCase.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   ├── CategoryProductUsageApi.java
│       │   │   │   ├── PriceTypeProductUsageApi.java
│       │   │   │   ├── PurchaseProductCatalogQuery.java
│       │   │   │   ├── SalesProductCatalogEntry.java
│       │   │   │   ├── SalesProductCatalogQuery.java
│       │   │   │   ├── StockCatalogEntry.java
│       │   │   │   └── StockCatalogQuery.java
│       │   │   └── out
│       │   │       ├── CustomerProductContextGateway.java
│       │   │       ├── ProductCategoryGateway.java
│       │   │       ├── ProductFileStorageGateway.java
│       │   │       ├── ProductPriceTypeExistenceGateway.java
│       │   │       ├── ProductPriceTypeGateway.java
│       │   │       ├── ProductReferenceGateway.java
│       │   │       ├── ProductStockFilterGateway.java
│       │   │       └── ProductWarehouseStockGateway.java
│       │   ├── queries
│       │   │   ├── services
│       │   │   │   ├── RepositoryPurchaseProductCatalogQuery.java
│       │   │   │   └── RepositoryStockCatalogQuery.java
│       │   │   └── use_case
│       │   │       ├── GetProductUseCase.java
│       │   │       └── ListProductsUseCase.java
│       │   ├── stock
│       │   │   ├── response
│       │   │   │   └── ProductStockByWarehouseResponse.java
│       │   │   └── use_case
│       │   │       └── GetProductStockUseCase.java
│       │   ├── transactions
│       │   │   ├── response
│       │   │   │   └── ProductWithStockResponse.java
│       │   │   └── use_case
│       │   │       └── GetProductsForTransactionUseCase.java
│       │   └── update
│       │       ├── request
│       │       │   └── UpdateProductRequest.java
│       │       └── use_case
│       │           └── UpdateProductUseCase.java
│       ├── domain
│       │   ├── entity
│       │   │   ├── Product.java
│       │   │   └── ProductComponent.java
│       │   ├── exceptions
│       │   │   └── ProductReferencedException.java
│       │   ├── repository
│       │   │   └── ProductRepository.java
│       │   └── valueobjects
│       │       └── ProductPrice.java
│       ├── infrastructure
│       │   ├── config
│       │   │   ├── ProductConfig.java
│       │   │   ├── PurchaseProductCatalogConfig.java
│       │   │   └── StockCatalogConfig.java
│       │   ├── integration
│       │   │   ├── LocalCustomerProductContextGatewayAdapter.java
│       │   │   ├── LocalProductCategoryGatewayAdapter.java
│       │   │   ├── LocalProductFileStorageGatewayAdapter.java
│       │   │   ├── LocalProductPriceTypeExistenceGatewayAdapter.java
│       │   │   ├── LocalProductPriceTypeGatewayAdapter.java
│       │   │   ├── LocalProductReferenceGatewayAdapter.java
│       │   │   ├── LocalProductStockFilterGatewayAdapter.java
│       │   │   ├── LocalProductWarehouseStockGatewayAdapter.java
│       │   │   └── LocalSalesProductCatalogQuery.java
│       │   ├── persistence
│       │   │   ├── adapters
│       │   │   │   └── JpaProductRepository.java
│       │   │   ├── entities
│       │   │   │   ├── ProductCodeSequenceEntity.java
│       │   │   │   ├── ProductComponentEntity.java
│       │   │   │   ├── ProductEntity.java
│       │   │   │   └── ProductPriceEntity.java
│       │   │   ├── queries
│       │   │   │   └── JpaProductUsageQueries.java
│       │   │   └── repositories
│       │   │       ├── SpringDataProductCodeSequenceRepository.java
│       │   │       ├── SpringDataProductRepository.java
│       │   │       └── SpringDataProductUsageRepository.java
│       │   └── web
│       │       └── ProductController.java
│       └── reporting
│           ├── application
│           │   ├── dto
│           │   │   ├── query
│           │   │   │   └── model
│           │   │   │       └── CommercialCatalogQuery.java
│           │   │   └── response
│           │   │       └── CommercialCatalogResponse.java
│           │   ├── generate_commercial_catalog_pdf
│           │   │   ├── services
│           │   │   │   └── GenerateCommercialCatalogPdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GenerateCommercialCatalogPdfUseCase.java
│           │   └── ports
│           │       └── out
│           │           ├── CommercialCatalogPdfPort.java
│           │           ├── CommercialCatalogRepositoryPort.java
│           │           └── MediaGateway.java
│           └── infrastructure
│               ├── config
│               │   └── CommercialCatalogReportingConfig.java
│               ├── integration
│               │   └── FileStorageMediaAdapter.java
│               ├── pdf
│               │   └── CommercialCatalogPdfGenerator.java
│               ├── persistence
│               │   ├── adapters
│               │   │   └── CommercialCatalogRepositoryAdapter.java
│               │   ├── projections
│               │   │   └── CommercialCatalogProjection.java
│               │   └── repositories
│               │       └── CommercialCatalogJpaRepository.java
│               └── web
│                   └── CommercialCatalogController.java
└── test
    └── products
        ├── application
        │   ├── architecture
        │   │   ├── ProductCatalogBoundaryTest.java
        │   │   └── ProductStockBoundaryTest.java
        │   ├── create
        │   │   └── use_case
        │   │       └── CreateProductUseCaseTest.java
        │   ├── delete
        │   │   └── use_case
        │   │       └── DeleteProductUseCaseTest.java
        │   ├── exceptions
        │   │   ├── CompositeProductCannotBeComponentExceptionTest.java
        │   │   ├── DuplicateProductCodeExceptionTest.java
        │   │   ├── DuplicateProductComponentExceptionTest.java
        │   │   └── ProductNotFoundExceptionTest.java
        │   ├── mappers
        │   │   └── ProductMapperTest.java
        │   ├── photos
        │   │   └── use_case
        │   │       ├── RemoveProductPhotoUseCaseTest.java
        │   │       └── UploadProductPhotoUseCaseTest.java
        │   ├── ports
        │   │   └── out
        │   │       └── ProductReferenceGatewayTest.java
        │   ├── queries
        │   │   └── use_case
        │   │       ├── GetProductUseCaseTest.java
        │   │       └── ListProductsUseCaseTest.java
        │   ├── stock
        │   │   └── use_case
        │   │       └── GetProductStockUseCaseTest.java
        │   ├── transactions
        │   │   └── use_case
        │   │       └── GetProductsForTransactionUseCaseTest.java
        │   └── update
        │       └── use_case
        │           └── UpdateProductUseCaseTest.java
        ├── domain
        │   ├── entity
        │   │   ├── ProductComponentUnitTest.java
        │   │   └── ProductUnitTest.java
        │   └── valueobjects
        │       └── ProductPriceUnitTest.java
        ├── infrastructure
        │   ├── integration
        │   │   ├── LocalProductFileStorageGatewayAdapterTest.java
        │   │   ├── ProductCatalogGatewayIntegrationTest.java
        │   │   └── ProductStockGatewayIntegrationTest.java
        │   ├── integration_tests
        │   │   └── ProductPersistenceQueriesMySqlIT.java
        │   ├── persistence
        │   │   ├── adapters
        │   │   │   └── JpaProductRepositoryTest.java
        │   │   ├── queries
        │   │   │   └── JpaProductUsageQueriesTest.java
        │   │   └── repositories
        │   │       ├── ProductPriceMappingTest.java
        │   │       ├── ProductPricePersistenceBoundaryTest.java
        │   │       ├── ProductReferenceQueryBoundaryTest.java
        │   │       ├── ProductStockFilterTest.java
        │   │       └── ProductStockQueryBoundaryTest.java
        │   └── web
        │       └── ProductControllerTest.java
        └── reporting
            ├── application
            │   ├── architecture
            │   │   └── CommercialCatalogBoundaryTest.java
            │   ├── dto
            │   │   └── response
            │   │       └── CommercialCatalogResponseTest.java
            │   └── generate_commercial_catalog_pdf
            │       └── services
            │           └── GenerateCommercialCatalogPdfUseCaseImplTest.java
            └── infrastructure
                ├── config
                │   └── CommercialCatalogReportingConfigTest.java
                ├── integration
                │   └── FileStorageMediaAdapterTest.java
                ├── pdf
                │   └── CommercialCatalogPdfContentTest.java
                └── web
                    └── CommercialCatalogControllerTest.java
```

### purchases

```text
├── main
│   └── purchases
│       ├── application
│       │   ├── cancel
│       │   │   ├── request
│       │   │   │   └── CancelPurchaseRequest.java
│       │   │   └── use_case
│       │   │       └── CancelPurchaseUseCase.java
│       │   ├── confirm
│       │   │   └── use_case
│       │   │       └── ConfirmPurchaseUseCase.java
│       │   ├── create
│       │   │   ├── request
│       │   │   │   └── CreatePurchaseRequest.java
│       │   │   └── use_case
│       │   │       └── CreatePurchaseUseCase.java
│       │   ├── delete
│       │   │   └── use_case
│       │   │       └── DeletePurchaseUseCase.java
│       │   ├── dto
│       │   │   └── response
│       │   │       ├── PurchaseDetailResponse.java
│       │   │       ├── PurchaseListResponse.java
│       │   │       └── PurchaseProductResponse.java
│       │   ├── get
│       │   │   └── use_case
│       │   │       └── GetPurchaseByIdUseCase.java
│       │   ├── list
│       │   │   └── use_case
│       │   │       └── ListPurchasesUseCase.java
│       │   ├── mappers
│       │   │   └── PurchaseMapper.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   ├── PurchaseDocumentFilter.java
│       │   │   │   ├── PurchaseDocumentQuery.java
│       │   │   │   ├── PurchaseDocumentRow.java
│       │   │   │   ├── PurchaseDocumentSnapshot.java
│       │   │   │   └── SupplierPurchaseUsageQuery.java
│       │   │   └── out
│       │   │       ├── InventoryGateway.java
│       │   │       ├── PurchaseActorGateway.java
│       │   │       ├── PurchaseProductCatalogGateway.java
│       │   │       ├── PurchaseProductGateway.java
│       │   │       ├── PurchaseSupplierGateway.java
│       │   │       └── PurchaseWarehouseGateway.java
│       │   ├── products
│       │   │   └── use_case
│       │   │       └── GetProductsForPurchaseUseCase.java
│       │   ├── queries
│       │   │   └── services
│       │   │       ├── PurchaseDocumentQueryService.java
│       │   │       └── RepositorySupplierPurchaseUsageQuery.java
│       │   ├── services
│       │   │   └── PurchaseDetailResponseAssembler.java
│       │   └── update
│       │       ├── request
│       │       │   └── UpdatePurchaseRequest.java
│       │       └── use_case
│       │           └── UpdatePurchaseUseCase.java
│       ├── domain
│       │   ├── entity
│       │   │   ├── Purchase.java
│       │   │   ├── PurchaseLine.java
│       │   │   └── PurchaseLineAllocation.java
│       │   ├── exceptions
│       │   │   └── PurchaseNotFoundException.java
│       │   ├── repository
│       │   │   └── PurchaseRepository.java
│       │   └── valueobjects
│       │       ├── PurchaseId.java
│       │       ├── PurchaseLineAllocationId.java
│       │       ├── PurchaseLineId.java
│       │       ├── PurchaseNumber.java
│       │       └── PurchaseStatus.java
│       ├── infrastructure
│       │   ├── config
│       │   │   ├── PurchaseConfig.java
│       │   │   ├── PurchaseDocumentQueryConfig.java
│       │   │   └── SupplierPurchaseUsageConfig.java
│       │   ├── integration
│       │   │   ├── LocalPurchaseActorGatewayAdapter.java
│       │   │   ├── LocalPurchaseProductCatalogGateway.java
│       │   │   ├── LocalPurchaseProductGatewayAdapter.java
│       │   │   ├── LocalPurchaseSupplierGatewayAdapter.java
│       │   │   ├── LocalPurchaseWarehouseGatewayAdapter.java
│       │   │   └── WarehouseInventoryAdapter.java
│       │   ├── persistence
│       │   │   ├── adapters
│       │   │   │   └── PurchaseRepositoryAdapter.java
│       │   │   ├── entities
│       │   │   │   ├── PurchaseEntity.java
│       │   │   │   ├── PurchaseLineAllocationEntity.java
│       │   │   │   ├── PurchaseLineEntity.java
│       │   │   │   └── PurchaseNumberSequenceEntity.java
│       │   │   ├── mappers
│       │   │   │   └── PurchaseJpaMapper.java
│       │   │   └── repositories
│       │   │       ├── PurchaseJpaRepository.java
│       │   │       └── PurchaseNumberSequenceJpaRepository.java
│       │   └── web
│       │       └── PurchaseController.java
│       └── reporting
│           ├── application
│           │   ├── dto
│           │   │   └── model
│           │   │       ├── PurchaseReportData.java
│           │   │       ├── PurchaseReportFilter.java
│           │   │       └── PurchaseReportRow.java
│           │   ├── generate_purchase_pdf
│           │   │   ├── services
│           │   │   │   └── GeneratePurchasePdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GeneratePurchasePdfUseCase.java
│           │   ├── generate_purchases_report_pdf
│           │   │   ├── services
│           │   │   │   └── GeneratePurchasesReportPdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GeneratePurchasesReportPdfUseCase.java
│           │   └── ports
│           │       └── out
│           │           ├── PurchasePdfPort.java
│           │           ├── PurchaseReportQueryGateway.java
│           │           └── PurchasesReportPdfPort.java
│           └── infrastructure
│               ├── config
│               │   └── PurchasesReportConfig.java
│               ├── integration
│               │   └── PurchaseDocumentQueryAdapter.java
│               ├── pdf
│               │   ├── PurchasePdfGenerator.java
│               │   └── PurchasesReportPdfGenerator.java
│               └── web
│                   └── PurchasesReportController.java
└── test
    └── purchases
        ├── application
        │   ├── architecture
        │   │   ├── PurchaseIdentityCatalogBoundaryTest.java
        │   │   ├── PurchaseProductCatalogContractTest.java
        │   │   └── PurchaseWarehouseReadBoundaryTest.java
        │   ├── cancel
        │   │   └── use_case
        │   │       └── CancelPurchaseUseCaseUnitTest.java
        │   ├── confirm
        │   │   └── use_case
        │   │       └── ConfirmPurchaseUseCaseUnitTest.java
        │   ├── create
        │   │   └── use_case
        │   │       └── CreatePurchaseUseCaseUnitTest.java
        │   ├── products
        │   │   └── use_case
        │   │       └── GetProductsForPurchaseUseCaseTest.java
        │   ├── queries
        │   │   └── services
        │   │       └── RepositorySupplierPurchaseUsageQueryTest.java
        │   └── services
        │       └── PurchaseDetailResponseAssemblerTest.java
        ├── domain
        │   ├── entity
        │   │   ├── PurchaseLineAllocationUnitTest.java
        │   │   ├── PurchaseLineUnitTest.java
        │   │   ├── PurchaseRepositoryIntegrationTest.java
        │   │   ├── PurchaseUnitTest.java
        │   │   └── UpdatePurchaseCatalogTest.java
        │   └── valueobjects
        │       ├── PurchaseIdUnitTest.java
        │       ├── PurchaseNumberUnitTest.java
        │       └── PurchaseStatusUnitTest.java
        ├── infrastructure
        │   ├── integration
        │   │   └── LocalPurchaseSupplierGatewayAdapterTest.java
        │   └── web
        │       └── PurchaseControllerTest.java
        └── reporting
            ├── application
            │   ├── architecture
            │   │   └── PurchaseReportsBoundaryTest.java
            │   ├── generate_purchase_pdf
            │   │   └── services
            │   │       └── GeneratePurchasePdfUseCaseImplTest.java
            │   └── generate_purchases_report_pdf
            │       └── services
            │           └── GeneratePurchasesReportPdfUseCaseImplTest.java
            └── infrastructure
                ├── config
                │   └── PurchasesReportConfigTest.java
                ├── integration
                │   └── PurchaseDocumentQueryAdapterTest.java
                ├── pdf
                │   └── PurchasePdfContentTest.java
                └── web
                    └── PurchasesReportControllerTest.java
```

### quotes

```text
├── main
│   └── quotes
│       ├── application
│       │   ├── cancel
│       │   │   ├── request
│       │   │   │   └── CancelQuoteRequest.java
│       │   │   └── use_case
│       │   │       └── CancelQuoteUseCase.java
│       │   ├── convert_to_invoice
│       │   │   ├── request
│       │   │   │   └── ConvertQuoteToInvoiceRequest.java
│       │   │   ├── response
│       │   │   │   └── QuoteConvertedToInvoiceResponse.java
│       │   │   └── use_case
│       │   │       └── ConvertQuoteToInvoiceUseCase.java
│       │   ├── convert_to_order
│       │   │   ├── response
│       │   │   │   └── QuoteConvertedToOrderResponse.java
│       │   │   └── use_case
│       │   │       └── ConvertQuoteToOrderUseCase.java
│       │   ├── create
│       │   │   ├── request
│       │   │   │   └── CreateQuoteRequest.java
│       │   │   └── use_case
│       │   │       └── CreateQuoteUseCase.java
│       │   ├── delete
│       │   │   └── use_case
│       │   │       └── DeleteQuoteUseCase.java
│       │   ├── dto
│       │   │   └── response
│       │   │       ├── QuoteAllocationDetailResponse.java
│       │   │       ├── QuoteDetailResponse.java
│       │   │       ├── QuoteLineDetailResponse.java
│       │   │       ├── QuoteListResponse.java
│       │   │       ├── QuoteProductResponse.java
│       │   │       ├── QuoteWarehouseResponse.java
│       │   │       └── WarehouseStockInfoResponse.java
│       │   ├── get
│       │   │   └── use_case
│       │   │       └── GetQuoteByIdUseCase.java
│       │   ├── list
│       │   │   └── use_case
│       │   │       └── GetQuotesUseCase.java
│       │   ├── mappers
│       │   │   └── QuoteMapper.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   ├── QuoteDocumentFilter.java
│       │   │   │   ├── QuoteDocumentQuery.java
│       │   │   │   ├── QuoteDocumentRow.java
│       │   │   │   └── QuoteDocumentSnapshot.java
│       │   │   └── out
│       │   │       ├── ActorIdentityGateway.java
│       │   │       ├── InvoiceCreationGateway.java
│       │   │       ├── OrderCreationGateway.java
│       │   │       ├── QuoteConversionNotificationGateway.java
│       │   │       ├── QuoteCustomerGateway.java
│       │   │       ├── QuoteProductCatalogGateway.java
│       │   │       ├── QuoteRejectionNotificationGateway.java
│       │   │       └── QuoteWarehouseGateway.java
│       │   ├── products
│       │   │   └── use_case
│       │   │       └── GetProductsForQuoteUseCase.java
│       │   ├── queries
│       │   │   └── services
│       │   │       └── QuoteDocumentQueryService.java
│       │   ├── update
│       │   │   ├── request
│       │   │   │   └── UpdateQuoteRequest.java
│       │   │   └── use_case
│       │   │       └── UpdateQuoteUseCase.java
│       │   └── warehouses
│       │       └── use_case
│       │           └── GetWarehousesForQuoteUseCase.java
│       ├── domain
│       │   ├── entity
│       │   │   ├── Quote.java
│       │   │   ├── QuoteAllocation.java
│       │   │   └── QuoteLine.java
│       │   ├── exceptions
│       │   │   ├── InvalidQuoteAllocationException.java
│       │   │   ├── InvalidQuoteLineException.java
│       │   │   ├── QuoteNotFoundException.java
│       │   │   └── QuoteStateException.java
│       │   ├── repository
│       │   │   └── QuoteRepository.java
│       │   └── valueobjects
│       │       ├── QuoteAllocationId.java
│       │       ├── QuoteLineId.java
│       │       ├── QuoteNumber.java
│       │       └── QuoteStatus.java
│       ├── infrastructure
│       │   ├── config
│       │   │   ├── QuoteConfig.java
│       │   │   └── QuoteDocumentQueryConfig.java
│       │   ├── integration
│       │   │   ├── LocalInvoiceCreationGateway.java
│       │   │   ├── LocalOrderCreationGateway.java
│       │   │   ├── LocalQuoteCustomerAdapter.java
│       │   │   ├── LocalQuoteProductCatalogAdapter.java
│       │   │   ├── LocalQuoteRejectionNotificationGateway.java
│       │   │   ├── LocalQuoteWarehouseAdapter.java
│       │   │   ├── QuoteActorIdentityAdapter.java
│       │   │   └── QuoteConversionNotificationAdapter.java
│       │   ├── persistence
│       │   │   ├── adapters
│       │   │   │   └── JpaQuoteRepositoryAdapter.java
│       │   │   ├── entities
│       │   │   │   ├── QuoteAllocationJpaEntity.java
│       │   │   │   ├── QuoteJpaEntity.java
│       │   │   │   ├── QuoteLineCategorySnapshotJpaEntity.java
│       │   │   │   ├── QuoteLineJpaEntity.java
│       │   │   │   └── QuoteNumberSequenceJpaEntity.java
│       │   │   ├── mappers
│       │   │   │   └── QuoteJpaMapper.java
│       │   │   └── repositories
│       │   │       ├── QuoteJpaRepository.java
│       │   │       └── QuoteNumberSequenceJpaRepository.java
│       │   └── web
│       │       └── QuoteController.java
│       └── reporting
│           ├── application
│           │   ├── dto
│           │   │   └── model
│           │   │       ├── QuoteReportData.java
│           │   │       ├── QuoteReportFilter.java
│           │   │       └── QuoteReportRow.java
│           │   ├── generate_quote_pdf
│           │   │   ├── services
│           │   │   │   └── GenerateQuotePdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GenerateQuotePdfUseCase.java
│           │   ├── generate_quotes_report_pdf
│           │   │   ├── services
│           │   │   │   └── GenerateQuotesReportPdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GenerateQuotesReportPdfUseCase.java
│           │   └── ports
│           │       └── out
│           │           ├── QuotePdfPort.java
│           │           ├── QuoteReportQueryGateway.java
│           │           └── QuotesReportPdfPort.java
│           └── infrastructure
│               ├── config
│               │   └── QuotesReportConfig.java
│               ├── integration
│               │   └── QuoteDocumentQueryAdapter.java
│               ├── pdf
│               │   ├── QuotePdfGenerator.java
│               │   └── QuotesReportPdfGenerator.java
│               └── web
│                   └── QuotesReportController.java
└── test
    └── quotes
        ├── application
        │   ├── architecture
        │   │   ├── QuoteCancellationBoundaryTest.java
        │   │   ├── QuoteInvoiceConversionBoundaryTest.java
        │   │   ├── QuoteModuleBoundaryTest.java
        │   │   └── QuoteOrderConversionBoundaryTest.java
        │   ├── cancel
        │   │   └── use_case
        │   │       └── CancelQuoteUseCaseTest.java
        │   ├── convert_to_invoice
        │   │   └── use_case
        │   │       └── ConvertQuoteToInvoiceUseCaseTest.java
        │   ├── convert_to_order
        │   │   └── use_case
        │   │       └── ConvertQuoteToOrderUseCaseTest.java
        │   ├── create
        │   │   └── use_case
        │   │       └── CreateQuoteUseCaseTest.java
        │   ├── delete
        │   │   └── use_case
        │   │       └── DeleteQuoteUseCaseTest.java
        │   ├── get
        │   │   └── use_case
        │   │       └── GetQuoteByIdUseCaseTest.java
        │   ├── list
        │   │   └── use_case
        │   │       └── GetQuotesUseCaseTest.java
        │   ├── mappers
        │   │   └── QuoteMapperTest.java
        │   ├── products
        │   │   └── use_case
        │   │       └── GetProductsForQuoteUseCaseTest.java
        │   ├── update
        │   │   └── use_case
        │   │       └── UpdateQuoteUseCaseTest.java
        │   └── warehouses
        │       └── use_case
        │           └── GetWarehousesForQuoteUseCaseTest.java
        ├── domain
        │   ├── entity
        │   │   ├── QuoteAllocationTest.java
        │   │   ├── QuoteCategorySnapshotMappingTest.java
        │   │   ├── QuoteLineTest.java
        │   │   └── QuoteTest.java
        │   ├── exceptions
        │   │   ├── InvalidQuoteAllocationExceptionTest.java
        │   │   ├── InvalidQuoteLineExceptionTest.java
        │   │   ├── QuoteNotFoundExceptionTest.java
        │   │   └── QuoteStateExceptionTest.java
        │   └── valueobjects
        │       ├── QuoteAllocationIdTest.java
        │       ├── QuoteLineIdTest.java
        │       ├── QuoteNumberTest.java
        │       └── QuoteStatusTest.java
        ├── infrastructure
        │   ├── integration
        │   │   └── LocalOrderCreationGatewayTest.java
        │   ├── integration_tests
        │   │   └── SalesConversionPersistenceMySqlIT.java
        │   ├── persistence
        │   │   └── adapters
        │   │       └── JpaQuoteRepositoryAdapterTest.java
        │   └── web
        │       ├── QuoteControllerTest.java
        │       └── QuoteWarehouseReassignmentRemovalTest.java
        └── reporting
            ├── application
            │   ├── architecture
            │   │   └── QuoteReportsBoundaryTest.java
            │   ├── generate_quote_pdf
            │   │   └── services
            │   │       └── GenerateQuotePdfUseCaseImplTest.java
            │   └── generate_quotes_report_pdf
            │       └── services
            │           └── GenerateQuotesReportPdfUseCaseImplTest.java
            └── infrastructure
                ├── config
                │   └── QuotesReportConfigTest.java
                ├── integration
                │   └── QuoteDocumentQueryAdapterTest.java
                ├── pdf
                │   └── QuotePdfContentTest.java
                └── web
                    └── QuotesReportControllerTest.java
```

### receivables

```text
├── main
│   └── receivables
│       ├── application
│       │   ├── customer_debt
│       │   │   └── use_case
│       │   │       ├── GetCustomerDebtUseCase.java
│       │   │       └── SearchCustomerDebtUseCase.java
│       │   ├── customer_invoices
│       │   │   └── use_case
│       │   │       └── GetCustomerInvoicesUseCase.java
│       │   ├── dto
│       │   │   └── response
│       │   │       ├── CustomerDebtListResponse.java
│       │   │       ├── CustomerDebtSummaryResponse.java
│       │   │       ├── CustomerInvoiceDebtResponse.java
│       │   │       ├── PendingPaymentResponse.java
│       │   │       └── ReceivablesSummaryResponse.java
│       │   ├── payment_history
│       │   │   └── use_case
│       │   │       └── GetPaymentHistoryUseCase.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   ├── AccountsReceivableReportQuery.java
│       │   │   │   └── AccountsReceivableReportSnapshot.java
│       │   │   └── out
│       │   │       ├── model
│       │   │       │   └── AccountsReceivableRow.java
│       │   │       ├── ReceivablesCustomerProfileGateway.java
│       │   │       └── ReceivablesQueryGateway.java
│       │   ├── services
│       │   │   └── AccountsReceivableReportQueryService.java
│       │   └── summary
│       │       └── use_case
│       │           └── GetReceivablesSummaryUseCase.java
│       ├── infrastructure
│       │   ├── config
│       │   │   ├── AccountsReceivableReportQueryConfig.java
│       │   │   └── ReceivablesConfig.java
│       │   ├── integration
│       │   │   └── LocalReceivablesCustomerProfileGatewayAdapter.java
│       │   ├── persistence
│       │   │   ├── adapters
│       │   │   │   └── ReceivablesRepositoryAdapter.java
│       │   │   └── repositories
│       │   │       └── ReceivablesJpaRepository.java
│       │   └── web
│       │       └── ReceivablesController.java
│       └── reporting
│           ├── application
│           │   ├── dto
│           │   │   └── model
│           │   │       ├── AccountsReceivableReportData.java
│           │   │       └── CollectionsReportData.java
│           │   ├── generate_accounts_receivable_report_pdf
│           │   │   ├── services
│           │   │   │   └── GenerateAccountsReceivableReportPdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GenerateAccountsReceivableReportPdfUseCase.java
│           │   ├── generate_collections_report_pdf
│           │   │   ├── services
│           │   │   │   └── GenerateCollectionsReportPdfUseCaseImpl.java
│           │   │   └── use_case
│           │   │       └── GenerateCollectionsReportPdfUseCase.java
│           │   └── ports
│           │       └── out
│           │           ├── AccountsReceivableQueryGateway.java
│           │           ├── AccountsReceivableReportPdfPort.java
│           │           └── CollectionsReportPdfPort.java
│           └── infrastructure
│               ├── config
│               │   └── ReceivablesReportingConfig.java
│               ├── integration
│               │   └── ReceivablesReportQueryAdapter.java
│               ├── pdf
│               │   ├── AccountsReceivableReportPdfGenerator.java
│               │   └── CollectionsReportPdfGenerator.java
│               └── web
│                   └── ReceivablesReportsController.java
└── test
    └── receivables
        ├── application
        │   ├── architecture
        │   │   └── ReceivablesBoundaryTest.java
        │   ├── customer_debt
        │   │   └── use_case
        │   │       ├── GetCustomerDebtUseCaseTest.java
        │   │       └── SearchCustomerDebtUseCaseTest.java
        │   ├── customer_invoices
        │   │   └── use_case
        │   │       └── GetCustomerInvoicesUseCaseTest.java
        │   ├── dto
        │   │   └── response
        │   │       └── ReceivablesSummaryResponseTest.java
        │   ├── payment_history
        │   │   └── use_case
        │   │       └── GetPaymentHistoryUseCaseTest.java
        │   └── summary
        │       └── use_case
        │           └── GetReceivablesSummaryUseCaseTest.java
        ├── infrastructure
        │   ├── config
        │   │   └── ReceivablesConfigTest.java
        │   ├── integration
        │   │   └── LocalReceivablesCustomerProfileGatewayAdapterTest.java
        │   ├── persistence
        │   │   └── adapters
        │   │       └── ReceivablesRepositoryAdapterTest.java
        │   └── web
        │       └── ReceivablesControllerTest.java
        └── reporting
            ├── application
            │   ├── architecture
            │   │   └── AccountsReceivableReportsBoundaryTest.java
            │   ├── dto
            │   │   └── model
            │   │       ├── AccountsReceivableReportDataTest.java
            │   │       └── CollectionsReportDataTest.java
            │   ├── generate_accounts_receivable_report_pdf
            │   │   └── services
            │   │       └── GenerateAccountsReceivableReportPdfUseCaseImplTest.java
            │   └── generate_collections_report_pdf
            │       └── services
            │           └── GenerateCollectionsReportPdfUseCaseImplTest.java
            └── infrastructure
                ├── config
                │   └── ReceivablesReportingConfigTest.java
                ├── integration
                │   └── ReceivablesReportQueryAdapterTest.java
                ├── pdf
                │   └── AccountsReceivablePdfContentTest.java
                └── web
                    └── ReceivablesReportsControllerTest.java
```

### returns

```text
├── main
│   └── returns
│       ├── application
│       │   ├── create
│       │   │   ├── request
│       │   │   │   └── CreateReturnRequest.java
│       │   │   └── use_case
│       │   │       └── CreateReturnUseCase.java
│       │   ├── delete
│       │   │   └── use_case
│       │   │       └── DeleteReturnUseCase.java
│       │   ├── dto
│       │   │   └── response
│       │   │       ├── ReturnListResponse.java
│       │   │       └── ReturnResponse.java
│       │   ├── get
│       │   │   └── use_case
│       │   │       └── GetReturnByIdUseCase.java
│       │   ├── list
│       │   │   └── use_case
│       │   │       └── GetAllReturnsUseCase.java
│       │   ├── mappers
│       │   │   └── ReturnMapper.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   ├── InvoiceReturnDetailsQuery.java
│       │   │   │   └── ProductReferenceQuery.java
│       │   │   └── out
│       │   │       ├── ReturnBalanceGateway.java
│       │   │       ├── ReturnCashGateway.java
│       │   │       ├── ReturnInventoryGateway.java
│       │   │       ├── ReturnInvoiceAdjustmentGateway.java
│       │   │       ├── ReturnInvoiceQueryGateway.java
│       │   │       ├── ReturnPaymentSettlementGateway.java
│       │   │       └── ReturnProjectionQueryGateway.java
│       │   ├── queries
│       │   │   └── services
│       │   │       └── InvoiceReturnDetailsQueryService.java
│       │   └── services
│       │       └── ReturnResponseAssembler.java
│       ├── domain
│       │   ├── entity
│       │   │   ├── CancelledAllocation.java
│       │   │   ├── Return.java
│       │   │   ├── ReturnLine.java
│       │   │   └── ReturnLineAllocation.java
│       │   ├── repository
│       │   │   └── ReturnRepository.java
│       │   └── valueobjects
│       │       ├── ReturnId.java
│       │       ├── ReturnNumber.java
│       │       ├── ReturnStatus.java
│       │       └── ReturnType.java
│       └── infrastructure
│           ├── config
│           │   └── ReturnConfig.java
│           ├── integration
│           │   ├── InvoiceReturnAdjustmentAdapter.java
│           │   ├── LocalReturnBalanceGateway.java
│           │   ├── LocalReturnCashGateway.java
│           │   ├── LocalReturnInventoryGatewayAdapter.java
│           │   ├── LocalReturnInvoiceQueryGateway.java
│           │   ├── LocalReturnPaymentSettlementGateway.java
│           │   └── LocalReturnProjectionQueryGateway.java
│           ├── persistence
│           │   ├── adapters
│           │   │   └── JpaReturnRepository.java
│           │   ├── entities
│           │   │   ├── ReturnCancelledAllocationEntity.java
│           │   │   ├── ReturnEntity.java
│           │   │   ├── ReturnLineAllocationEntity.java
│           │   │   ├── ReturnLineEntity.java
│           │   │   └── ReturnNumberSequenceEntity.java
│           │   ├── queries
│           │   │   └── JpaReturnsProductReferenceQuery.java
│           │   └── repositories
│           │       └── SpringDataReturnsProductReferenceRepository.java
│           └── web
│               └── ReturnController.java
└── test
    └── returns
        ├── application
        │   ├── architecture
        │   │   ├── ReturnFinancialBoundaryTest.java
        │   │   └── ReturnInventoryBoundaryTest.java
        │   ├── create
        │   │   └── use_case
        │   │       └── CreateReturnUseCaseTest.java
        │   ├── delete
        │   │   └── use_case
        │   │       └── DeleteReturnUseCaseTest.java
        │   ├── get
        │   │   └── use_case
        │   │       └── GetReturnByIdUseCaseTest.java
        │   ├── list
        │   │   └── use_case
        │   │       └── GetAllReturnsUseCaseTest.java
        │   ├── mappers
        │   │   └── ReturnMapperTest.java
        │   ├── services
        │   │   └── ReturnResponseAssemblerTest.java
        │   └── usecases
        │       └── services
        │           └── ReturnFinancialFlowsTest.java
        ├── domain
        │   ├── architecture
        │   │   └── ReturnInvoiceReferenceBoundaryTest.java
        │   ├── entity
        │   │   ├── CancelledAllocationUnitTest.java
        │   │   ├── ReturnLineAllocationUnitTest.java
        │   │   ├── ReturnLineUnitTest.java
        │   │   └── ReturnUnitTest.java
        │   └── valueobjects
        │       ├── ReturnIdUnitTest.java
        │       └── ReturnNumberUnitTest.java
        └── infrastructure
            ├── config
            │   └── ReturnConfigTest.java
            ├── integration
            │   ├── InvoiceReturnAdjustmentAdapterTest.java
            │   ├── LocalReturnInventoryGatewayAdapterTest.java
            │   └── ReturnFinancialWiringTest.java
            ├── integration_tests
            │   └── ReturnsFinancialRollbackMySqlIT.java
            ├── persistence
            │   └── adapters
            │       └── JpaReturnRepositoryTest.java
            └── web
                └── ReturnControllerTest.java
```

### shared

```text
├── main
│   └── shared
│       ├── dto
│       │   └── PageResponse.java
│       ├── infrastructure
│       │   ├── pagination
│       │   │   └── PageResponseMapper.java
│       │   └── pdf
│       │       ├── PdfBranding.java
│       │       └── PdfStatusColors.java
│       └── storage
│           ├── domain
│           │   ├── exceptions
│           │   │   ├── FileNotFoundException.java
│           │   │   └── StorageException.java
│           │   └── services
│           │       └── FileStorageService.java
│           └── infrastructure
│               ├── adapters
│               │   ├── LocalFileStorageService.java
│               │   └── S3FileStorageService.java
│               ├── config
│               │   └── StorageConfig.java
│               └── web
│                   └── FileController.java
└── test
    └── shared
        ├── application
        │   └── architecture
        │       └── ResponsibilityStructureTest.java
        ├── dto
        │   └── PageResponseBoundaryTest.java
        ├── infrastructure
        │   ├── bootstrap
        │   │   ├── CashlyApplicationTests.java
        │   │   └── TestCashlyApplication.java
        │   ├── config
        │   │   ├── DatabaseCleaner.java
        │   │   ├── H2DialectWithoutEngineSupport.java
        │   │   ├── TestJpaConfig.java
        │   │   └── TestcontainersConfiguration.java
        │   ├── integration_tests
        │   │   └── BackendStartupMySqlIT.java
        │   ├── pagination
        │   │   └── PageResponseMapperTest.java
        │   └── pdf
        │       └── PdfStatusColorsTest.java
        └── storage
            └── domain
                ├── entity
                │   └── StorageServiceTest.java
                └── exceptions
                    └── StorageExceptionTest.java
```

### suppliers

```text
├── main
│   └── suppliers
│       ├── application
│       │   ├── delete
│       │   │   └── use_case
│       │   │       └── DeleteSupplierUseCase.java
│       │   ├── dto
│       │   │   ├── request
│       │   │   │   ├── AddressRequest.java
│       │   │   │   ├── EmailRequest.java
│       │   │   │   └── PhoneRequest.java
│       │   │   └── response
│       │   │       ├── AddressResponse.java
│       │   │       ├── EmailResponse.java
│       │   │       ├── PhoneResponse.java
│       │   │       ├── SupplierCreateResponse.java
│       │   │       ├── SupplierDetailResponse.java
│       │   │       └── SupplierListResponse.java
│       │   ├── exceptions
│       │   │   ├── SupplierHasAssociatedRecordsException.java
│       │   │   └── SupplierNotFoundException.java
│       │   ├── mappers
│       │   │   └── SupplierMapper.java
│       │   ├── ports
│       │   │   ├── in
│       │   │   │   └── PurchaseSupplierQuery.java
│       │   │   └── out
│       │   │       └── PurchaseUsageGateway.java
│       │   ├── queries
│       │   │   └── use_case
│       │   │       ├── GetSupplierUseCase.java
│       │   │       └── ListSuppliersUseCase.java
│       │   ├── register
│       │   │   ├── request
│       │   │   │   └── CreateSupplierRequest.java
│       │   │   └── use_case
│       │   │       └── RegisterSupplierUseCase.java
│       │   └── update
│       │       ├── request
│       │       │   └── UpdateSupplierRequest.java
│       │       └── use_case
│       │           └── UpdateSupplierUseCase.java
│       ├── domain
│       │   ├── entity
│       │   │   ├── Supplier.java
│       │   │   ├── SupplierAddress.java
│       │   │   ├── SupplierEmail.java
│       │   │   └── SupplierPhone.java
│       │   ├── repository
│       │   │   └── SupplierRepository.java
│       │   ├── services
│       │   │   └── SupplierCodeGenerator.java
│       │   └── valueobjects
│       │       ├── AddressId.java
│       │       ├── EmailAddress.java
│       │       ├── EmailId.java
│       │       ├── LegalName.java
│       │       ├── PhoneId.java
│       │       ├── PhoneNumber.java
│       │       ├── Rif.java
│       │       ├── SupplierAlias.java
│       │       ├── SupplierCode.java
│       │       ├── SupplierId.java
│       │       └── SupplierType.java
│       └── infrastructure
│           ├── config
│           │   ├── PurchaseSupplierQueryConfig.java
│           │   └── SupplierConfig.java
│           ├── integration
│           │   ├── LocalPurchaseUsageGatewayAdapter.java
│           │   └── SupplierPurchaseDirectoryAdapter.java
│           ├── persistence
│           │   ├── adapters
│           │   │   └── JpaSupplierRepository.java
│           │   ├── entities
│           │   │   ├── SupplierAddressEntity.java
│           │   │   ├── SupplierEmailEntity.java
│           │   │   ├── SupplierEntity.java
│           │   │   └── SupplierPhoneEntity.java
│           │   └── repositories
│           │       └── SpringDataSupplierRepository.java
│           ├── services
│           │   └── SequentialSupplierCodeGenerator.java
│           └── web
│               └── SupplierController.java
└── test
    └── suppliers
        ├── domain
        │   ├── entity
        │   │   ├── SupplierPurchaseUsageTest.java
        │   │   └── SupplierUnitTest.java
        │   └── valueobjects
        │       ├── EmailAddressUnitTest.java
        │       ├── LegalNameUnitTest.java
        │       ├── PhoneNumberUnitTest.java
        │       ├── RifUnitTest.java
        │       ├── SupplierAliasUnitTest.java
        │       ├── SupplierCodeUnitTest.java
        │       └── SupplierIdUnitTest.java
        └── infrastructure
            ├── integration
            │   ├── LocalPurchaseUsageGatewayAdapterTest.java
            │   └── SupplierPurchaseDirectoryAdapterTest.java
            ├── persistence
            │   └── adapters
            │       └── JpaSupplierRepositoryTest.java
            └── web
                └── SupplierControllerTest.java
```

### warehouses

```text
├── main
│   └── warehouses
│       ├── infrastructure
│       │   └── config
│       │       └── WarehouseConfig.java
│       ├── management
│       │   ├── application
│       │   │   ├── create
│       │   │   │   ├── request
│       │   │   │   │   └── CreateWarehouseRequest.java
│       │   │   │   ├── services
│       │   │   │   │   └── DuplicateWarehouseCodeException.java
│       │   │   │   └── use_case
│       │   │   │       └── CreateWarehouseUseCase.java
│       │   │   ├── delete
│       │   │   │   ├── services
│       │   │   │   │   └── WarehouseHasActiveStockException.java
│       │   │   │   └── use_case
│       │   │   │       └── DeleteWarehouseUseCase.java
│       │   │   ├── dto
│       │   │   │   └── response
│       │   │   │       ├── WarehouseDetailResponse.java
│       │   │   │       └── WarehouseListResponse.java
│       │   │   ├── exceptions
│       │   │   │   ├── WarehouseNotActiveException.java
│       │   │   │   └── WarehouseNotFoundException.java
│       │   │   ├── ports
│       │   │   │   └── in
│       │   │   │       ├── WarehouseDirectoryEntry.java
│       │   │   │       └── WarehouseDirectoryQuery.java
│       │   │   ├── queries
│       │   │   │   ├── services
│       │   │   │   │   └── RepositoryWarehouseDirectoryQuery.java
│       │   │   │   └── use_case
│       │   │   │       ├── GetWarehouseUseCase.java
│       │   │   │       └── ListWarehousesUseCase.java
│       │   │   └── update
│       │   │       ├── request
│       │   │       │   └── UpdateWarehouseRequest.java
│       │   │       └── use_case
│       │   │           └── UpdateWarehouseUseCase.java
│       │   ├── domain
│       │   │   ├── entity
│       │   │   │   └── Warehouse.java
│       │   │   └── repository
│       │   │       └── WarehouseRepository.java
│       │   └── infrastructure
│       │       ├── config
│       │       │   └── WarehouseDirectoryConfig.java
│       │       ├── persistence
│       │       │   ├── adapters
│       │       │   │   └── JpaWarehouseRepository.java
│       │       │   ├── entities
│       │       │   │   └── WarehouseEntity.java
│       │       │   └── repositories
│       │       │       └── SpringDataWarehouseRepository.java
│       │       └── web
│       │           └── WarehouseController.java
│       ├── reporting
│       │   ├── application
│       │   │   ├── dto
│       │   │   │   ├── query
│       │   │   │   │   └── model
│       │   │   │   │       ├── CatalogPdfQuery.java
│       │   │   │   │       ├── CatalogReportQuery.java
│       │   │   │   │       ├── MovementsPdfQuery.java
│       │   │   │   │       └── StockPdfQuery.java
│       │   │   │   └── response
│       │   │   │       ├── CatalogReportResponse.java
│       │   │   │       ├── MovementReportResponse.java
│       │   │   │       └── StockReportResponse.java
│       │   │   ├── generate_catalog_pdf
│       │   │   │   ├── services
│       │   │   │   │   └── GenerateCatalogPdfUseCaseImpl.java
│       │   │   │   └── use_case
│       │   │   │       └── GenerateCatalogPdfUseCase.java
│       │   │   ├── generate_movements_excel
│       │   │   │   ├── services
│       │   │   │   │   └── GenerateMovementsExcelUseCaseImpl.java
│       │   │   │   └── use_case
│       │   │   │       └── GenerateMovementsExcelUseCase.java
│       │   │   ├── generate_movements_pdf
│       │   │   │   ├── services
│       │   │   │   │   └── GenerateMovementsPdfUseCaseImpl.java
│       │   │   │   └── use_case
│       │   │   │       └── GenerateMovementsPdfUseCase.java
│       │   │   ├── generate_stock_excel
│       │   │   │   ├── services
│       │   │   │   │   └── GenerateStockExcelUseCaseImpl.java
│       │   │   │   └── use_case
│       │   │   │       └── GenerateStockExcelUseCase.java
│       │   │   ├── generate_stock_pdf
│       │   │   │   ├── services
│       │   │   │   │   └── GenerateStockPdfUseCaseImpl.java
│       │   │   │   └── use_case
│       │   │   │       └── GenerateStockPdfUseCase.java
│       │   │   ├── get_catalog_report
│       │   │   │   ├── services
│       │   │   │   │   └── GetCatalogReportUseCaseImpl.java
│       │   │   │   └── use_case
│       │   │   │       └── GetCatalogReportUseCase.java
│       │   │   └── ports
│       │   │       └── out
│       │   │           ├── CatalogPdfPort.java
│       │   │           ├── CatalogReportRepositoryPort.java
│       │   │           ├── MovementsExcelPort.java
│       │   │           ├── MovementsPdfPort.java
│       │   │           ├── MovementsReportRepositoryPort.java
│       │   │           ├── StockExcelPort.java
│       │   │           ├── StockPdfPort.java
│       │   │           └── StockReportRepositoryPort.java
│       │   └── infrastructure
│       │       ├── config
│       │       │   ├── MovementsReportConfig.java
│       │       │   ├── StockReportConfig.java
│       │       │   └── WarehouseCatalogReportingConfig.java
│       │       ├── excel
│       │       │   ├── MovementsExcelGenerator.java
│       │       │   └── StockExcelGenerator.java
│       │       ├── pdf
│       │       │   ├── CatalogPdfGenerator.java
│       │       │   ├── MovementsPdfGenerator.java
│       │       │   └── StockPdfGenerator.java
│       │       ├── persistence
│       │       │   ├── adapters
│       │       │   │   ├── CatalogReportRepositoryAdapter.java
│       │       │   │   ├── MovementsReportRepositoryAdapter.java
│       │       │   │   └── StockReportRepositoryAdapter.java
│       │       │   ├── projections
│       │       │   │   ├── CatalogReportProjection.java
│       │       │   │   ├── MovementsReportProjection.java
│       │       │   │   └── StockReportProjection.java
│       │       │   └── repositories
│       │       │       ├── CatalogReportJpaRepository.java
│       │       │       ├── MovementsReportJpaRepository.java
│       │       │       └── StockReportJpaRepository.java
│       │       └── web
│       │           ├── CatalogReportController.java
│       │           ├── MovementsReportController.java
│       │           └── StockReportController.java
│       └── stock
│           ├── application
│           │   ├── adjustment
│           │   │   ├── request
│           │   │   │   └── StockAdjustmentRequest.java
│           │   │   └── use_case
│           │   │       └── StockAdjustmentUseCase.java
│           │   ├── deduction
│           │   │   └── services
│           │   │       └── StockDeductionService.java
│           │   ├── dto
│           │   │   └── response
│           │   │       ├── StockBatchItem.java
│           │   │       └── WarehouseStockResponse.java
│           │   ├── entry
│           │   │   ├── request
│           │   │   │   └── StockEntryRequest.java
│           │   │   └── use_case
│           │   │       └── StockEntryUseCase.java
│           │   ├── exceptions
│           │   │   ├── InsufficientStockException.java
│           │   │   └── ProductNotInWarehouseException.java
│           │   ├── exit
│           │   │   ├── request
│           │   │   │   └── StockExitRequest.java
│           │   │   └── use_case
│           │   │       └── StockExitUseCase.java
│           │   ├── invoices
│           │   │   └── services
│           │   │       └── InvoiceInventoryService.java
│           │   ├── movements
│           │   │   ├── response
│           │   │   │   └── StockMovementResponse.java
│           │   │   └── use_case
│           │   │       └── GetStockMovementsUseCase.java
│           │   ├── ports
│           │   │   ├── in
│           │   │   │   ├── AvailableStockProductsQuery.java
│           │   │   │   ├── InvoiceInventoryApi.java
│           │   │   │   ├── InvoiceStockCommand.java
│           │   │   │   ├── ProductReferenceQuery.java
│           │   │   │   ├── ProductStockQuery.java
│           │   │   │   ├── PurchaseInventoryApi.java
│           │   │   │   ├── PurchaseStockCommand.java
│           │   │   │   ├── PurchaseWarehouseQuery.java
│           │   │   │   ├── ReturnInventoryApi.java
│           │   │   │   ├── ReturnStockCommand.java
│           │   │   │   ├── StockAvailabilityQuery.java
│           │   │   │   └── StockDeductionApi.java
│           │   │   └── out
│           │   │       ├── CustomerWarehouseAccessGateway.java
│           │   │       └── StockCatalogGateway.java
│           │   ├── purchases
│           │   │   └── services
│           │   │       └── PurchaseInventoryService.java
│           │   ├── queries
│           │   │   ├── services
│           │   │   │   ├── RepositoryProductStockQuery.java
│           │   │   │   ├── RepositoryPurchaseWarehouseQuery.java
│           │   │   │   └── StockAvailabilityQueryService.java
│           │   │   └── use_case
│           │   │       └── GetWarehouseStockUseCase.java
│           │   ├── returns
│           │   │   └── services
│           │   │       └── ReturnInventoryService.java
│           │   └── transfer
│           │       ├── request
│           │       │   └── TransferStockRequest.java
│           │       └── use_case
│           │           └── TransferStockUseCase.java
│           ├── domain
│           │   ├── entity
│           │   │   ├── StockMovement.java
│           │   │   └── WarehouseStock.java
│           │   ├── repository
│           │   │   ├── StockMovementRepository.java
│           │   │   └── WarehouseStockRepository.java
│           │   └── valueobjects
│           │       ├── MovementType.java
│           │       └── ReferenceType.java
│           └── infrastructure
│               ├── config
│               │   ├── ProductStockQueryConfig.java
│               │   ├── PurchaseInventoryConfig.java
│               │   ├── PurchaseWarehouseQueryConfig.java
│               │   ├── ReturnInventoryConfig.java
│               │   ├── StockAvailabilityQueryConfig.java
│               │   └── StockDeductionConfig.java
│               ├── integration
│               │   ├── CustomerWarehouseAccessAdapter.java
│               │   └── LocalStockCatalogGatewayAdapter.java
│               ├── persistence
│               │   ├── adapters
│               │   │   ├── JpaStockMovementRepository.java
│               │   │   └── JpaWarehouseStockRepository.java
│               │   ├── entities
│               │   │   ├── StockMovementEntity.java
│               │   │   └── WarehouseStockEntity.java
│               │   ├── queries
│               │   │   ├── JpaAvailableStockProductsQuery.java
│               │   │   └── JpaWarehousesProductReferenceQuery.java
│               │   └── repositories
│               │       ├── SpringDataAvailableStockProductsRepository.java
│               │       ├── SpringDataStockMovementRepository.java
│               │       ├── SpringDataWarehouseStockRepository.java
│               │       └── SpringDataWarehousesProductReferenceRepository.java
│               └── web
│                   ├── StockMovementController.java
│                   └── WarehouseStockController.java
└── test
    └── warehouses
        ├── management
        │   ├── application
        │   │   ├── create
        │   │   │   ├── services
        │   │   │   │   └── DuplicateWarehouseCodeExceptionTest.java
        │   │   │   └── use_case
        │   │   │       └── CreateWarehouseUseCaseTest.java
        │   │   ├── delete
        │   │   │   └── use_case
        │   │   │       └── DeleteWarehouseUseCaseTest.java
        │   │   ├── exceptions
        │   │   │   ├── WarehouseNotActiveExceptionTest.java
        │   │   │   └── WarehouseNotFoundExceptionTest.java
        │   │   ├── queries
        │   │   │   ├── services
        │   │   │   │   └── RepositoryWarehouseDirectoryQueryTest.java
        │   │   │   └── use_case
        │   │   │       ├── GetWarehouseUseCaseTest.java
        │   │   │       └── ListWarehousesUseCaseTest.java
        │   │   └── update
        │   │       └── use_case
        │   │           └── UpdateWarehouseUseCaseTest.java
        │   ├── domain
        │   │   └── entity
        │   │       └── WarehouseUnitTest.java
        │   └── infrastructure
        │       ├── persistence
        │       │   └── adapters
        │       │       └── JpaWarehouseRepositoryTest.java
        │       └── web
        │           └── WarehouseControllerTest.java
        ├── reporting
        │   ├── application
        │   │   ├── dto
        │   │   │   ├── query
        │   │   │   │   └── model
        │   │   │   │       ├── CatalogPdfQueryTest.java
        │   │   │   │       ├── CatalogReportQueryTest.java
        │   │   │   │       ├── MovementsPdfQueryTest.java
        │   │   │   │       └── StockPdfQueryTest.java
        │   │   │   └── response
        │   │   │       ├── CatalogReportResponseTest.java
        │   │   │       ├── MovementReportResponseTest.java
        │   │   │       └── StockReportResponseTest.java
        │   │   ├── generate_catalog_pdf
        │   │   │   └── services
        │   │   │       └── GenerateCatalogPdfUseCaseImplTest.java
        │   │   ├── generate_movements_excel
        │   │   │   └── services
        │   │   │       └── GenerateMovementsExcelUseCaseImplTest.java
        │   │   ├── generate_movements_pdf
        │   │   │   └── services
        │   │   │       └── GenerateMovementsPdfUseCaseImplTest.java
        │   │   ├── generate_stock_excel
        │   │   │   └── services
        │   │   │       └── GenerateStockExcelUseCaseImplTest.java
        │   │   ├── generate_stock_pdf
        │   │   │   └── services
        │   │   │       └── GenerateStockPdfUseCaseImplTest.java
        │   │   ├── get_catalog_report
        │   │   │   └── services
        │   │   │       └── GetCatalogReportUseCaseImplTest.java
        │   │   └── ports
        │   │       └── out
        │   │           ├── CatalogPdfPortTest.java
        │   │           ├── MovementsPdfPortTest.java
        │   │           └── StockPdfPortTest.java
        │   └── infrastructure
        │       ├── config
        │       │   ├── MovementsReportConfigTest.java
        │       │   ├── StockReportConfigTest.java
        │       │   └── WarehouseCatalogReportingConfigTest.java
        │       ├── persistence
        │       │   ├── adapters
        │       │   │   ├── CatalogReportRepositoryAdapterTest.java
        │       │   │   ├── MovementsReportRepositoryAdapterTest.java
        │       │   │   └── StockReportRepositoryAdapterTest.java
        │       │   └── projections
        │       │       ├── CatalogReportProjectionTest.java
        │       │       ├── MovementsReportProjectionTest.java
        │       │       └── StockReportProjectionTest.java
        │       └── web
        │           ├── CatalogReportControllerTest.java
        │           ├── MovementsReportControllerTest.java
        │           └── StockReportControllerTest.java
        └── stock
            ├── application
            │   ├── adjustment
            │   │   └── use_case
            │   │       └── StockAdjustmentUseCaseTest.java
            │   ├── architecture
            │   │   └── ManualStockCatalogBoundaryTest.java
            │   ├── entry
            │   │   └── use_case
            │   │       └── StockEntryUseCaseTest.java
            │   ├── exceptions
            │   │   ├── InsufficientStockExceptionTest.java
            │   │   └── ProductNotInWarehouseExceptionTest.java
            │   ├── exit
            │   │   └── use_case
            │   │       └── StockExitUseCaseTest.java
            │   ├── invoices
            │   │   └── services
            │   │       └── InvoiceInventoryServiceTest.java
            │   ├── movements
            │   │   └── use_case
            │   │       └── GetStockMovementsUseCaseTest.java
            │   ├── ports
            │   │   └── in
            │   │       └── InvoiceStockCommandTest.java
            │   ├── queries
            │   │   ├── services
            │   │   │   └── StockAvailabilityQueryServiceTest.java
            │   │   └── use_case
            │   │       └── GetWarehouseStockUseCaseTest.java
            │   ├── returns
            │   │   └── services
            │   │       └── ReturnInventoryServiceTest.java
            │   └── transfer
            │       └── use_case
            │           └── TransferStockUseCaseTest.java
            ├── domain
            │   ├── entity
            │   │   ├── StockMovementUnitTest.java
            │   │   └── WarehouseStockTest.java
            │   └── valueobjects
            │       ├── MovementTypeUnitTest.java
            │       └── ReferenceTypeTest.java
            └── infrastructure
                ├── concurrency
                │   ├── InvoiceInventoryConcurrencyIT.java
                │   ├── ManualStockConcurrencyIT.java
                │   ├── StockDeductionConcurrencyIT.java
                │   └── WarehouseStockConcurrencyIT.java
                ├── integration
                │   ├── LocalStockCatalogGatewayAdapterTest.java
                │   └── StockAvailabilityConsumerAdaptersTest.java
                ├── persistence
                │   └── adapters
                │       ├── JpaStockMovementRepositoryTest.java
                │       └── JpaWarehouseStockRepositoryTest.java
                └── web
                    ├── StockMovementControllerTest.java
                    └── WarehouseStockControllerTest.java
```
