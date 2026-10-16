# Complete before backend trees

This is an exhaustive source/test/resource inventory. Proposed package moves are not implemented. Overview is a reserved extension point and introduces no new source in this structural change.

## auth

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── auth
                            ├── authentication
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── request
                            │   │   │   │   └── LoginRequest.java
                            │   │   │   └── response
                            │   │   │       ├── AuthenticatedEntityResponse.java
                            │   │   │       └── AuthenticationResponse.java
                            │   │   ├── exceptions
                            │   │   │   └── InvalidCredentialsException.java
                            │   │   ├── mappers
                            │   │   │   └── AuthenticationMapper.java
                            │   │   └── usecases
                            │   │       └── LoginUserUseCase.java
                            │   ├── domain
                            │   │   ├── entities
                            │   │   │   ├── Authenticable.java
                            │   │   │   └── AuthenticableType.java
                            │   │   └── services
                            │   │       ├── AuthenticableResolver.java
                            │   │       └── AuthenticationDomainService.java
                            │   └── infrastructure
                            │       ├── adapters
                            │       │   ├── in
                            │       │   │   ├── controllers
                            │       │   │   │   └── AuthController.java
                            │       │   │   └── security
                            │       │   │       ├── JwtAuthenticationEntryPoint.java
                            │       │   │       └── JwtAuthenticationFilter.java
                            │       │   └── out
                            │       │       ├── services
                            │       │       │   └── ChainedAuthenticableResolver.java
                            │       │       └── UserAuthenticableAdapter.java
                            │       └── config
                            │           ├── AuthenticationConfig.java
                            │           └── SecurityConfig.java
                            ├── token
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   └── request
                            │   │   │       ├── LogoutRequest.java
                            │   │   │       └── RefreshTokenRequest.java
                            │   │   ├── exceptions
                            │   │   │   ├── InvalidTokenException.java
                            │   │   │   └── TokenExpiredException.java
                            │   │   ├── ports
                            │   │   │   └── out
                            │   │   │       └── AuthenticableSubjectGateway.java
                            │   │   └── usecases
                            │   │       ├── LogoutUserUseCase.java
                            │   │       └── RefreshTokenUseCase.java
                            │   ├── domain
                            │   │   ├── entities
                            │   │   │   └── RefreshToken.java
                            │   │   ├── repositories
                            │   │   │   └── RefreshTokenRepository.java
                            │   │   ├── services
                            │   │   │   └── TokenService.java
                            │   │   └── valueobjects
                            │   │       └── RefreshTokenId.java
                            │   └── infrastructure
                            │       ├── adapters
                            │       │   └── out
                            │       │       ├── persistence
                            │       │       │   ├── adapters
                            │       │       │   │   └── JpaRefreshTokenRepository.java
                            │       │       │   ├── entities
                            │       │       │   │   └── RefreshTokenEntity.java
                            │       │       │   └── repositories
                            │       │       │       └── SpringDataRefreshTokenRepository.java
                            │       │       └── LocalAuthenticableSubjectGateway.java
                            │       └── config
                            │           └── JwtTokenService.java
                            └── user
                                ├── application
                                │   ├── dto
                                │   │   ├── request
                                │   │   │   ├── RegisterUserRequest.java
                                │   │   │   ├── UpdateUserEmailRequest.java
                                │   │   │   ├── UpdateUserModulePermissionsRequest.java
                                │   │   │   ├── UpdateUserPasswordRequest.java
                                │   │   │   └── UpdateUserRequest.java
                                │   │   └── response
                                │   │       └── UserResponse.java
                                │   ├── exceptions
                                │   │   ├── DuplicateEmailException.java
                                │   │   ├── UserHasAssociatedRecordsException.java
                                │   │   └── UserNotFoundException.java
                                │   ├── mappers
                                │   │   └── UserMapper.java
                                │   ├── ports
                                │   │   └── in
                                │   │       ├── ActorIdentityQuery.java
                                │   │       └── NotificationRecipientsQuery.java
                                │   ├── queries
                                │   │   └── RepositoryNotificationRecipientsQuery.java
                                │   ├── services
                                │   │   └── RepositoryActorIdentityQuery.java
                                │   └── usecases
                                │       ├── DeleteUserUseCase.java
                                │       ├── GetAllUsersUseCase.java
                                │       ├── GetUserByIdUseCase.java
                                │       ├── RegisterUserUseCase.java
                                │       ├── UpdateUserEmailUseCase.java
                                │       ├── UpdateUserModulePermissionsUseCase.java
                                │       ├── UpdateUserPasswordUseCase.java
                                │       └── UpdateUserUseCase.java
                                ├── domain
                                │   ├── entities
                                │   │   └── User.java
                                │   ├── repositories
                                │   │   └── UserRepository.java
                                │   ├── services
                                │   │   └── PasswordEncoder.java
                                │   └── valueobjects
                                │       ├── BusinessModule.java
                                │       ├── Email.java
                                │       ├── Password.java
                                │       ├── UserId.java
                                │       ├── UserName.java
                                │       └── UserRole.java
                                └── infrastructure
                                    ├── adapters
                                    │   ├── in
                                    │   │   └── controllers
                                    │   │       └── UserController.java
                                    │   └── out
                                    │       └── persistence
                                    │           ├── adapters
                                    │           │   └── JpaUserRepository.java
                                    │           ├── entities
                                    │           │   ├── UserEntity.java
                                    │           │   └── UserModulePermissionEntity.java
                                    │           └── repositories
                                    │               └── SpringDataUserRepository.java
                                    └── config
                                        ├── ActorIdentityQueryConfig.java
                                        ├── BcryptPasswordEncoder.java
                                        └── UserQueryConfig.java
```

## bootstrap

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── CashlyApplication.java
```

## cash_register

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── cash_register
                            ├── application
                            │   ├── dto
                            │   │   ├── CashMovementRequest.java
                            │   │   ├── CashMovementResponse.java
                            │   │   ├── CashRegisterDetailResponse.java
                            │   │   ├── CashRegisterExportRow.java
                            │   │   ├── CashRegisterMapper.java
                            │   │   ├── CashRegisterMetrics.java
                            │   │   ├── CashRegisterReportFilter.java
                            │   │   ├── CashRegisterResponse.java
                            │   │   ├── CloseRegisterRequest.java
                            │   │   ├── CustomerSummary.java
                            │   │   ├── DailyTotals.java
                            │   │   ├── ExpenseSummaryItem.java
                            │   │   ├── OpenRegisterRequest.java
                            │   │   ├── ReportFormat.java
                            │   │   ├── ReportType.java
                            │   │   └── RevenueSummaryItem.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── CashRegisterOperations.java
                            │   │   │   ├── CreditNoteCashApi.java
                            │   │   │   ├── DebitNoteCashApi.java
                            │   │   │   ├── QueryPaymentMethodUsage.java
                            │   │   │   └── ReturnCashRecording.java
                            │   │   └── out
                            │   │       ├── CashRegisterActorGateway.java
                            │   │       ├── CashRegisterExcelPort.java
                            │   │       ├── CashRegisterPaymentMethodGateway.java
                            │   │       ├── CashRegisterQueryGateway.java
                            │   │       ├── CashRegisterRangeReportPort.java
                            │   │       └── CashRegisterReportPort.java
                            │   ├── services
                            │   │   ├── CashMovementService.java
                            │   │   ├── CashRegisterOperationsService.java
                            │   │   └── ReturnCashRecordingService.java
                            │   └── usecases
                            │       ├── CloseRegisterUseCase.java
                            │       ├── GenerateCashRegisterExcelUseCase.java
                            │       ├── GenerateCashRegisterExcelUseCaseImpl.java
                            │       ├── GenerateCashRegisterRangeReportUseCase.java
                            │       ├── GenerateCashRegisterReportUseCase.java
                            │       ├── GetActiveRegisterUseCase.java
                            │       ├── GetRegisterHistoryUseCase.java
                            │       ├── OpenRegisterUseCase.java
                            │       └── RecordCashMovementUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   ├── CashMovement.java
                            │   │   ├── CashRegister.java
                            │   │   └── CashRegisterDetail.java
                            │   ├── repositories
                            │   │   └── CashRegisterRepository.java
                            │   └── valueobjects
                            │       ├── CashMovementDirection.java
                            │       ├── CashMovementType.java
                            │       ├── RegisterId.java
                            │       └── RegisterStatus.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       ├── CashRegisterController.java
                                │   │       └── CashRegisterExportController.java
                                │   └── out
                                │       ├── excel
                                │       │   ├── CashRegisterExcelExportService.java
                                │       │   └── CashRegisterExcelGenerator.java
                                │       ├── integration
                                │       │   ├── LocalCashRegisterActorGateway.java
                                │       │   ├── LocalCashRegisterPaymentMethodGateway.java
                                │       │   ├── LocalCreditNoteCashApi.java
                                │       │   └── LocalDebitNoteCashApi.java
                                │       ├── persistence
                                │       │   ├── adapters
                                │       │   │   └── CashRegisterRepositoryAdapter.java
                                │       │   ├── entities
                                │       │   │   ├── CashMovementJpaEntity.java
                                │       │   │   ├── CashRegisterDetailJpaEntity.java
                                │       │   │   └── CashRegisterJpaEntity.java
                                │       │   ├── queries
                                │       │   │   └── JpaPaymentMethodUsageQuery.java
                                │       │   └── repositories
                                │       │       ├── CashMovementJpaRepository.java
                                │       │       └── CashRegisterJpaRepository.java
                                │       └── report
                                │           ├── CashRegisterRangeReportGenerator.java
                                │           └── CashRegisterSessionReportGenerator.java
                                └── config
                                    ├── CashRegisterConfig.java
                                    ├── CashRegisterOperationsConfig.java
                                    └── ReturnCashRecordingConfig.java
```

## categories

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── categories
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── CreateCategoryRequest.java
                            │   │   │   └── UpdateCategoryRequest.java
                            │   │   └── response
                            │   │       ├── CategoryDetailResponse.java
                            │   │       ├── CategoryListResponse.java
                            │   │       └── CategorySummary.java
                            │   ├── exceptions
                            │   │   ├── CategoryHasActiveProductsException.java
                            │   │   ├── CategoryNotFoundException.java
                            │   │   └── DuplicateCategoryNameException.java
                            │   ├── mappers
                            │   │   └── CategoryMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   └── ProductCategoryQuery.java
                            │   │   └── out
                            │   │       └── CategoryUsageGateway.java
                            │   ├── queries
                            │   │   └── RepositoryProductCategoryQuery.java
                            │   └── usecases
                            │       ├── CreateCategoryUseCase.java
                            │       ├── DeleteCategoryUseCase.java
                            │       ├── GetCategoryUseCase.java
                            │       ├── ListCategoriesUseCase.java
                            │       └── UpdateCategoryUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   └── Category.java
                            │   └── repositories
                            │       └── CategoryRepository.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── CategoryController.java
                                │   └── out
                                │       ├── integration
                                │       │   └── ProductCategoryUsageAdapter.java
                                │       └── persistence
                                │           ├── adapters
                                │           │   └── JpaCategoryRepository.java
                                │           ├── entities
                                │           │   └── CategoryEntity.java
                                │           └── repositories
                                │               └── SpringDataCategoryRepository.java
                                └── config
                                    ├── CategoryConfig.java
                                    └── ProductCategoryQueryConfig.java
```

## credit_notes

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── credit_notes
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   └── CreateCreditNoteRequest.java
                            │   │   └── response
                            │   │       └── CreditNoteResponse.java
                            │   ├── mappers
                            │   │   └── CreditNoteMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── CreditNoteLedgerQuery.java
                            │   │   │   └── InvoiceCreditNoteDetailsQuery.java
                            │   │   └── out
                            │   │       ├── CreditNoteEffectsGateway.java
                            │   │       ├── CreditNoteFinancialPositionGateway.java
                            │   │       └── CreditNoteInvoiceGateway.java
                            │   ├── services
                            │   │   ├── InvoiceCreditNoteDetailsQueryService.java
                            │   │   └── RepositoryCreditNoteLedgerQuery.java
                            │   └── usecases
                            │       ├── CreateCreditNoteUseCase.java
                            │       ├── DeleteCreditNoteUseCase.java
                            │       ├── GetCreditNoteByIdUseCase.java
                            │       └── GetCreditNotesUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   └── CreditNote.java
                            │   ├── repositories
                            │   │   └── CreditNoteRepository.java
                            │   └── valueobjects
                            │       ├── CreditNoteId.java
                            │       └── CreditNoteNumber.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── CreditNoteController.java
                                │   └── out
                                │       ├── integration
                                │       │   ├── LocalCreditNoteEffectsGateway.java
                                │       │   ├── LocalCreditNoteFinancialPositionGateway.java
                                │       │   └── LocalCreditNoteInvoiceGateway.java
                                │       └── persistence
                                │           ├── adapters
                                │           │   └── CreditNoteRepositoryAdapter.java
                                │           ├── entities
                                │           │   ├── CreditNoteEntity.java
                                │           │   └── CreditNoteNumberSequenceEntity.java
                                │           ├── mappers
                                │           │   └── CreditNoteJpaMapper.java
                                │           └── repositories
                                │               ├── CreditNoteJpaRepository.java
                                │               └── CreditNoteNumberSequenceJpaRepository.java
                                └── config
                                    └── CreditNoteLedgerQueryConfig.java
```

## customers

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── customers
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── AddressRequest.java
                            │   │   │   ├── CreateAdvanceRequest.java
                            │   │   │   ├── CreateCustomerRequest.java
                            │   │   │   ├── CreateRefundRequest.java
                            │   │   │   ├── EmailRequest.java
                            │   │   │   ├── PhoneRequest.java
                            │   │   │   └── UpdateCustomerRequest.java
                            │   │   └── response
                            │   │       ├── AddressResponse.java
                            │   │       ├── BalanceTransactionResponse.java
                            │   │       ├── CreditStatusResponse.java
                            │   │       ├── CustomerBalanceMutationResponse.java
                            │   │       ├── CustomerBalanceResponse.java
                            │   │       ├── CustomerCreateResponse.java
                            │   │       ├── CustomerDetailResponse.java
                            │   │       ├── CustomerListResponse.java
                            │   │       ├── CustomerWarehouseAccessResponse.java
                            │   │       ├── EmailResponse.java
                            │   │       ├── OverdueInvoiceResponse.java
                            │   │       └── PhoneResponse.java
                            │   ├── exceptions
                            │   │   ├── CreditLimitExceededException.java
                            │   │   ├── CustomerHasAssociatedRecordsException.java
                            │   │   ├── CustomerNotFoundException.java
                            │   │   ├── CustomerWarehouseAccessAlreadyExistsException.java
                            │   │   ├── CustomerWarehouseAccessNotFoundException.java
                            │   │   └── DuplicateCustomerRifException.java
                            │   ├── mappers
                            │   │   ├── BalanceMapper.java
                            │   │   └── CustomerMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── CreditNoteBalanceApi.java
                            │   │   │   ├── CustomerCreditContextEntry.java
                            │   │   │   ├── CustomerCreditContextQuery.java
                            │   │   │   ├── CustomerCreditEligibilityQuery.java
                            │   │   │   ├── CustomerCreditLimits.java
                            │   │   │   ├── CustomerCreditTermsQuery.java
                            │   │   │   ├── CustomerProductPricingQuery.java
                            │   │   │   ├── CustomerProfileQuery.java
                            │   │   │   ├── CustomerProfileSnapshot.java
                            │   │   │   ├── CustomerWarehouseAccessQuery.java
                            │   │   │   ├── InvoiceBalanceConsumption.java
                            │   │   │   ├── InvoiceCustomerQuery.java
                            │   │   │   └── ReturnBalanceMutation.java
                            │   │   └── out
                            │   │       ├── model
                            │   │       │   └── WarehouseSnapshot.java
                            │   │       ├── CreditValidationGateway.java
                            │   │       ├── CustomerActorGateway.java
                            │   │       ├── CustomerCashGateway.java
                            │   │       └── WarehouseDirectoryGateway.java
                            │   ├── queries
                            │   │   ├── RepositoryCustomerCreditTermsQuery.java
                            │   │   ├── RepositoryCustomerProfileQuery.java
                            │   │   ├── RepositoryCustomerWarehouseAccessQuery.java
                            │   │   └── RepositoryInvoiceCustomerQuery.java
                            │   ├── services
                            │   │   ├── CustomerCreditEligibilityService.java
                            │   │   ├── InvoiceBalanceConsumptionService.java
                            │   │   ├── RepositoryCustomerCreditContextQuery.java
                            │   │   ├── RepositoryCustomerProductPricingQuery.java
                            │   │   └── ReturnBalanceMutationService.java
                            │   └── usecases
                            │       ├── credit
                            │       │   ├── GetCustomerCreditStatusUseCase.java
                            │       │   └── GetOverdueInvoicesUseCase.java
                            │       ├── AddToCustomerBalanceUseCase.java
                            │       ├── ConsumeCustomerBalanceUseCase.java
                            │       ├── CreateCustomerAdvanceUseCase.java
                            │       ├── DeleteCustomerUseCase.java
                            │       ├── GetCustomerBalanceUseCase.java
                            │       ├── GetCustomerUseCase.java
                            │       ├── GrantWarehouseAccessUseCase.java
                            │       ├── ListCustomerWarehousesUseCase.java
                            │       ├── ListCustomersUseCase.java
                            │       ├── RefundCustomerBalanceUseCase.java
                            │       ├── RegisterCustomerUseCase.java
                            │       ├── RevokeWarehouseAccessUseCase.java
                            │       └── UpdateCustomerUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   ├── BalanceTransaction.java
                            │   │   ├── Customer.java
                            │   │   ├── CustomerAddress.java
                            │   │   ├── CustomerBalance.java
                            │   │   ├── CustomerEmail.java
                            │   │   ├── CustomerPhone.java
                            │   │   └── CustomerWarehouseAccess.java
                            │   ├── repositories
                            │   │   ├── CustomerBalanceRepository.java
                            │   │   ├── CustomerRepository.java
                            │   │   └── CustomerWarehouseAccessRepository.java
                            │   ├── services
                            │   │   └── CustomerCodeGenerator.java
                            │   └── valueobjects
                            │       ├── AddressId.java
                            │       ├── BalanceType.java
                            │       ├── CreditDays.java
                            │       ├── CreditLimit.java
                            │       ├── CustomerAlias.java
                            │       ├── CustomerCode.java
                            │       ├── CustomerId.java
                            │       ├── CustomerType.java
                            │       ├── EmailAddress.java
                            │       ├── EmailId.java
                            │       ├── LegalName.java
                            │       ├── PaymentType.java
                            │       ├── PhoneId.java
                            │       ├── PhoneNumber.java
                            │       └── Rif.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       ├── CustomerBalanceController.java
                                │   │       ├── CustomerController.java
                                │   │       ├── CustomerCreditController.java
                                │   │       └── CustomerWarehouseAccessController.java
                                │   └── out
                                │       ├── integration
                                │       │   ├── LocalCreditNoteBalanceApi.java
                                │       │   ├── LocalCreditValidationGateway.java
                                │       │   ├── LocalCustomerActorGateway.java
                                │       │   ├── LocalCustomerCashGateway.java
                                │       │   └── LocalWarehouseDirectoryGateway.java
                                │       ├── persistence
                                │       │   ├── adapters
                                │       │   │   ├── CustomerBalanceRepositoryAdapter.java
                                │       │   │   ├── JpaCustomerRepository.java
                                │       │   │   └── JpaCustomerWarehouseAccessRepository.java
                                │       │   ├── entities
                                │       │   │   ├── BalanceTransactionEntity.java
                                │       │   │   ├── BalanceTxSequenceEntity.java
                                │       │   │   ├── CustomerAddressEntity.java
                                │       │   │   ├── CustomerBalanceEntity.java
                                │       │   │   ├── CustomerEmailEntity.java
                                │       │   │   ├── CustomerEntity.java
                                │       │   │   ├── CustomerPhoneEntity.java
                                │       │   │   └── CustomerWarehouseAccessEntity.java
                                │       │   ├── mappers
                                │       │   │   ├── BalanceTransactionJpaMapper.java
                                │       │   │   └── CustomerBalanceJpaMapper.java
                                │       │   └── repositories
                                │       │       ├── CustomerBalanceJpaRepository.java
                                │       │       ├── SpringDataCustomerRepository.java
                                │       │       └── SpringDataCustomerWarehouseAccessRepository.java
                                │       └── services
                                │           └── SequentialCustomerCodeGenerator.java
                                └── config
                                    ├── CustomerConfig.java
                                    ├── CustomerCreditQueryConfig.java
                                    ├── CustomerCreditTermsConfig.java
                                    ├── CustomerProductPricingConfig.java
                                    ├── CustomerProfileQueryConfig.java
                                    ├── CustomerWarehouseAccessQueryConfig.java
                                    ├── InvoiceBalanceConsumptionConfig.java
                                    ├── InvoiceCustomerQueryConfig.java
                                    └── ReturnBalanceMutationConfig.java
```

## dashboard

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── dashboard
                            ├── application
                            │   ├── dto
                            │   │   └── response
                            │   │       ├── DashboardSummaryResponse.java
                            │   │       ├── InvoiceDueResponse.java
                            │   │       ├── MonthlySalesResponse.java
                            │   │       └── TopItemResponse.java
                            │   ├── ports
                            │   │   └── out
                            │   │       └── DashboardQueryGateway.java
                            │   └── usecases
                            │       └── GetDashboardUseCase.java
                            └── infrastructure
                                └── adapters
                                    ├── in
                                    │   └── controllers
                                    │       └── DashboardController.java
                                    └── out
                                        └── persistence
                                            └── queries
                                                └── JdbcDashboardQueryAdapter.java
```

## debit_notes

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── debit_notes
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   └── CreateDebitNoteRequest.java
                            │   │   └── response
                            │   │       └── DebitNoteResponse.java
                            │   ├── mappers
                            │   │   └── DebitNoteMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── DebitNoteLedgerQuery.java
                            │   │   │   └── InvoiceDebitNoteDetailsQuery.java
                            │   │   └── out
                            │   │       ├── DebitNoteCashGateway.java
                            │   │       └── DebitNoteInvoiceGateway.java
                            │   ├── services
                            │   │   ├── InvoiceDebitNoteDetailsQueryService.java
                            │   │   └── RepositoryDebitNoteLedgerQuery.java
                            │   └── usecases
                            │       ├── CreateDebitNoteUseCase.java
                            │       ├── DeleteDebitNoteUseCase.java
                            │       ├── GetDebitNoteByIdUseCase.java
                            │       └── GetDebitNotesUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   └── DebitNote.java
                            │   ├── repositories
                            │   │   └── DebitNoteRepository.java
                            │   └── valueobjects
                            │       ├── DebitNoteId.java
                            │       └── DebitNoteNumber.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── DebitNoteController.java
                                │   └── out
                                │       ├── integration
                                │       │   ├── LocalDebitNoteCashGateway.java
                                │       │   └── LocalDebitNoteInvoiceGateway.java
                                │       └── persistence
                                │           ├── adapters
                                │           │   └── DebitNoteRepositoryAdapter.java
                                │           ├── entities
                                │           │   ├── DebitNoteEntity.java
                                │           │   └── DebitNoteNumberSequenceEntity.java
                                │           ├── mappers
                                │           │   └── DebitNoteJpaMapper.java
                                │           └── repositories
                                │               ├── DebitNoteJpaRepository.java
                                │               └── DebitNoteNumberSequenceJpaRepository.java
                                └── config
                                    └── DebitNoteLedgerQueryConfig.java
```

## infrastructure

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── infrastructure
                            ├── adapters
                            │   └── in
                            │       └── controllers
                            │           └── PartMultipartFile.java
                            └── config
                                ├── CorsConfig.java
                                ├── GlobalExceptionHandler.java
                                ├── JpaAuditingConfig.java
                                ├── MultipartConfig.java
                                └── OpenApiConfig.java
```

## invoices

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── invoices
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── CancelInvoiceRequest.java
                            │   │   │   ├── ConfirmInvoiceRequest.java
                            │   │   │   ├── CreateInvoiceRequest.java
                            │   │   │   ├── DuplicateInvoiceRequest.java
                            │   │   │   └── UpdateInvoiceRequest.java
                            │   │   └── response
                            │   │       ├── InvoiceDetailResponse.java
                            │   │       └── InvoiceListResponse.java
                            │   ├── mappers
                            │   │   └── InvoiceMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── InvoiceCreditLedgerEntry.java
                            │   │   │   ├── InvoiceCreditLedgerQuery.java
                            │   │   │   ├── InvoiceDocumentFilter.java
                            │   │   │   ├── InvoiceDocumentQuery.java
                            │   │   │   ├── InvoiceDocumentRow.java
                            │   │   │   ├── InvoiceDocumentSnapshot.java
                            │   │   │   ├── InvoiceNoteApi.java
                            │   │   │   ├── InvoiceReturnAdjustments.java
                            │   │   │   ├── InvoiceReturnCommand.java
                            │   │   │   ├── OrderInvoiceCreationApi.java
                            │   │   │   ├── ProductReferenceQuery.java
                            │   │   │   ├── QuoteInvoiceCreationApi.java
                            │   │   │   └── ReturnInvoiceQuery.java
                            │   │   └── out
                            │   │       ├── ActorIdentityGateway.java
                            │   │       ├── CashRegisterGateway.java
                            │   │       ├── CustomerBalanceGateway.java
                            │   │       ├── CustomerCreditGateway.java
                            │   │       ├── CustomerCreditTermsGateway.java
                            │   │       ├── InventoryGateway.java
                            │   │       ├── InvoiceCatalogGateway.java
                            │   │       ├── InvoiceCustomerGateway.java
                            │   │       ├── InvoiceFinancialPositionGateway.java
                            │   │       ├── InvoiceNoteDetailsGateway.java
                            │   │       ├── InvoiceNotificationGateway.java
                            │   │       ├── InvoiceOrderGateway.java
                            │   │       ├── InvoicePaymentSettlementGateway.java
                            │   │       ├── InvoicePaymentTotalsGateway.java
                            │   │       ├── InvoiceQuoteGateway.java
                            │   │       ├── InvoiceReturnDetailsGateway.java
                            │   │       ├── StockAvailabilityGateway.java
                            │   │       └── StockDeductionGateway.java
                            │   ├── services
                            │   │   ├── InvoiceAuthorizationService.java
                            │   │   ├── InvoiceDocumentQueryService.java
                            │   │   ├── OrderInvoiceCreationService.java
                            │   │   ├── QuoteInvoiceCreationService.java
                            │   │   ├── RepositoryInvoiceCreditLedgerQuery.java
                            │   │   ├── RepositoryInvoiceReturnAdjustments.java
                            │   │   └── RepositoryReturnInvoiceQuery.java
                            │   └── usecases
                            │       ├── CancelInvoiceUseCase.java
                            │       ├── ConfirmInvoiceUseCase.java
                            │       ├── CreateInvoiceUseCase.java
                            │       ├── DeleteInvoiceUseCase.java
                            │       ├── DuplicateInvoiceUseCase.java
                            │       ├── GetInvoiceByIdUseCase.java
                            │       ├── GetInvoicesUseCase.java
                            │       └── UpdateInvoiceUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   ├── Invoice.java
                            │   │   ├── InvoiceLine.java
                            │   │   └── InvoiceLineAllocation.java
                            │   ├── exceptions
                            │   │   ├── InvoiceNotFoundException.java
                            │   │   ├── InvoiceNotReturnableException.java
                            │   │   └── ReturnQuantityExceedsException.java
                            │   ├── repositories
                            │   │   └── InvoiceRepository.java
                            │   ├── services
                            │   │   └── InvoiceStatusService.java
                            │   └── valueobjects
                            │       ├── InvoiceId.java
                            │       ├── InvoiceLineId.java
                            │       ├── InvoiceNumber.java
                            │       ├── InvoiceReturnAdjustment.java
                            │       ├── InvoiceReturnStatus.java
                            │       ├── InvoiceStatus.java
                            │       └── PaymentType.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── InvoiceController.java
                                │   └── out
                                │       ├── integration
                                │       │   ├── InvoiceCustomerCreditAdapter.java
                                │       │   ├── InvoiceStockAvailabilityAdapter.java
                                │       │   ├── LocalActorIdentityGateway.java
                                │       │   ├── LocalCustomerBalanceGateway.java
                                │       │   ├── LocalCustomerCreditTermsGateway.java
                                │       │   ├── LocalInventoryGatewayAdapter.java
                                │       │   ├── LocalInvoiceCashRegisterGateway.java
                                │       │   ├── LocalInvoiceCatalogGateway.java
                                │       │   ├── LocalInvoiceCustomerGateway.java
                                │       │   ├── LocalInvoiceFinancialPositionGateway.java
                                │       │   ├── LocalInvoiceNoteApi.java
                                │       │   ├── LocalInvoiceNoteDetailsAdapter.java
                                │       │   ├── LocalInvoiceNotificationGateway.java
                                │       │   ├── LocalInvoiceOrderGateway.java
                                │       │   ├── LocalInvoicePaymentSettlementGateway.java
                                │       │   ├── LocalInvoicePaymentTotalsAdapter.java
                                │       │   ├── LocalInvoiceQuoteGateway.java
                                │       │   ├── LocalInvoiceReturnDetailsAdapter.java
                                │       │   └── LocalStockDeductionGateway.java
                                │       ├── persistence
                                │       │   ├── adapters
                                │       │   │   └── InvoiceRepositoryAdapter.java
                                │       │   ├── entities
                                │       │   │   ├── InvoiceEntity.java
                                │       │   │   ├── InvoiceLineAllocationEntity.java
                                │       │   │   ├── InvoiceLineEntity.java
                                │       │   │   └── InvoiceNumberSequenceEntity.java
                                │       │   ├── mappers
                                │       │   │   └── InvoiceJpaMapper.java
                                │       │   ├── queries
                                │       │   │   └── JpaInvoicesProductReferenceQuery.java
                                │       │   └── repositories
                                │       │       ├── InvoiceJpaRepository.java
                                │       │       ├── InvoiceNumberSequenceJpaRepository.java
                                │       │       └── SpringDataInvoicesProductReferenceRepository.java
                                │       └── services
                                │           └── InvoiceStatusServiceImpl.java
                                └── config
                                    ├── InvoiceCreditQueryConfig.java
                                    ├── InvoiceDocumentQueryConfig.java
                                    ├── InvoiceReturnAdjustmentConfig.java
                                    ├── OrderInvoiceCreationConfig.java
                                    ├── QuoteInvoiceCreationConfig.java
                                    └── ReturnInvoiceQueryConfig.java
```

## notifications

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── notifications
                            ├── application
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── QuoteConversionNotifications.java
                            │   │   │   └── QuoteRejectionNotifications.java
                            │   │   └── out
                            │   │       ├── NotificationDeliveryGateway.java
                            │   │       └── NotificationRecipientGateway.java
                            │   └── services
                            │       ├── NotificationService.java
                            │       ├── QuoteConversionNotificationService.java
                            │       └── QuoteRejectionNotificationService.java
                            ├── domain
                            │   ├── entities
                            │   │   └── Notification.java
                            │   ├── repositories
                            │   │   └── NotificationRepository.java
                            │   └── valueobjects
                            │       ├── NotificationId.java
                            │       └── NotificationType.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── NotificationController.java
                                │   └── out
                                │       ├── integration
                                │       │   └── AuthNotificationRecipientAdapter.java
                                │       ├── messaging
                                │       │   └── WebSocketNotificationDeliveryAdapter.java
                                │       └── persistence
                                │           ├── adapters
                                │           │   └── NotificationRepositoryAdapter.java
                                │           ├── entities
                                │           │   └── NotificationEntity.java
                                │           ├── mappers
                                │           │   └── NotificationJpaMapper.java
                                │           └── repositories
                                │               └── NotificationJpaRepository.java
                                └── config
                                    └── WebSocketConfig.java
```

## orders

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── orders
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── AssignOrderWarehousesRequest.java
                            │   │   │   ├── CancelOrderRequest.java
                            │   │   │   ├── ConvertToInvoiceRequest.java
                            │   │   │   ├── CreateOrderRequest.java
                            │   │   │   └── UpdateOrderRequest.java
                            │   │   └── response
                            │   │       ├── ConvertToInvoiceResponse.java
                            │   │       ├── OrderDetailResponse.java
                            │   │       ├── OrderListResponse.java
                            │   │       └── OrderProductResponse.java
                            │   ├── mappers
                            │   │   └── OrderMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── OrderDocumentFilter.java
                            │   │   │   ├── OrderDocumentQuery.java
                            │   │   │   ├── OrderDocumentRow.java
                            │   │   │   ├── OrderDocumentSnapshot.java
                            │   │   │   ├── ProductReferenceQuery.java
                            │   │   │   └── QuoteOrderCreationApi.java
                            │   │   └── out
                            │   │       ├── CustomerCreditGateway.java
                            │   │       ├── InvoiceCreationGateway.java
                            │   │       ├── OrderActorGateway.java
                            │   │       ├── OrderCustomerGateway.java
                            │   │       ├── OrderInventoryGateway.java
                            │   │       ├── OrderNotificationGateway.java
                            │   │       ├── OrderProductCatalogGateway.java
                            │   │       ├── OrderWarehouseGateway.java
                            │   │       └── StockAvailabilityGateway.java
                            │   ├── services
                            │   │   ├── OrderDocumentQueryService.java
                            │   │   └── QuoteOrderCreationService.java
                            │   └── usecases
                            │       ├── AssignOrderWarehousesUseCase.java
                            │       ├── CancelOrderUseCase.java
                            │       ├── ConfirmOrderUseCase.java
                            │       ├── ConvertToInvoiceUseCase.java
                            │       ├── CreateOrderUseCase.java
                            │       ├── DeleteOrderUseCase.java
                            │       ├── GetOrderByIdUseCase.java
                            │       ├── GetOrdersUseCase.java
                            │       ├── GetProductsForOrderUseCase.java
                            │       └── UpdateOrderUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   ├── Order.java
                            │   │   ├── OrderLine.java
                            │   │   └── OrderLineAllocation.java
                            │   ├── exceptions
                            │   │   ├── OrderCannotBeDeletedException.java
                            │   │   ├── OrderNotFoundException.java
                            │   │   └── OrderNotPendingException.java
                            │   ├── repositories
                            │   │   └── OrderRepository.java
                            │   └── valueobjects
                            │       ├── OrderId.java
                            │       ├── OrderLineId.java
                            │       ├── OrderNumber.java
                            │       └── OrderStatus.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── OrderController.java
                                │   └── out
                                │       ├── integration
                                │       │   ├── LocalInvoiceCreationGateway.java
                                │       │   ├── LocalOrderActorAdapter.java
                                │       │   ├── LocalOrderCustomerAdapter.java
                                │       │   ├── LocalOrderInventoryGateway.java
                                │       │   ├── LocalOrderNotificationAdapter.java
                                │       │   ├── LocalOrderProductCatalogAdapter.java
                                │       │   ├── LocalOrderWarehouseAdapter.java
                                │       │   ├── OrderCustomerCreditAdapter.java
                                │       │   └── OrderStockAvailabilityAdapter.java
                                │       └── persistence
                                │           ├── adapters
                                │           │   └── OrderRepositoryAdapter.java
                                │           ├── entities
                                │           │   ├── OrderEntity.java
                                │           │   ├── OrderLineAllocationEntity.java
                                │           │   ├── OrderLineCategorySnapshotEntity.java
                                │           │   ├── OrderLineEntity.java
                                │           │   └── OrderNumberSequenceEntity.java
                                │           ├── mappers
                                │           │   └── OrderJpaMapper.java
                                │           ├── queries
                                │           │   └── JpaOrdersProductReferenceQuery.java
                                │           └── repositories
                                │               ├── OrderJpaRepository.java
                                │               ├── OrderNumberSequenceJpaRepository.java
                                │               └── SpringDataOrdersProductReferenceRepository.java
                                └── config
                                    ├── OrderConfig.java
                                    ├── OrderDocumentQueryConfig.java
                                    └── QuoteOrderCreationConfig.java
```

## payment_methods

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── payment_methods
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── CreatePaymentMethodRequest.java
                            │   │   │   └── UpdatePaymentMethodRequest.java
                            │   │   └── response
                            │   │       └── PaymentMethodResponse.java
                            │   ├── mappers
                            │   │   └── PaymentMethodMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   └── PaymentMethodDetailsQuery.java
                            │   │   └── out
                            │   │       └── PaymentMethodUsageGateway.java
                            │   ├── queries
                            │   │   └── RepositoryPaymentMethodDetailsQuery.java
                            │   └── usecases
                            │       ├── CreatePaymentMethodUseCase.java
                            │       ├── DeletePaymentMethodUseCase.java
                            │       ├── GetPaymentMethodByIdUseCase.java
                            │       ├── ListPaymentMethodsUseCase.java
                            │       ├── TogglePaymentMethodUseCase.java
                            │       └── UpdatePaymentMethodUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   └── PaymentMethod.java
                            │   ├── repositories
                            │   │   └── PaymentMethodRepository.java
                            │   └── valueobjects
                            │       └── PaymentMethodId.java
                            └── infrastructure
                                └── adapters
                                    ├── in
                                    │   └── controllers
                                    │       └── PaymentMethodController.java
                                    └── out
                                        ├── integration
                                        │   └── LocalPaymentMethodUsageGateway.java
                                        └── persistence
                                            ├── adapters
                                            │   └── PaymentMethodRepositoryAdapter.java
                                            ├── entities
                                            │   └── PaymentMethodEntity.java
                                            ├── mappers
                                            │   └── PaymentMethodJpaMapper.java
                                            └── repositories
                                                └── PaymentMethodJpaRepository.java
```

## payments

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── payments
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── ApprovePaymentRequest.java
                            │   │   │   ├── CreatePaymentRequest.java
                            │   │   │   ├── PayCustomerDebtRequest.java
                            │   │   │   ├── PayInvoiceRequest.java
                            │   │   │   ├── PayPartialDebtRequest.java
                            │   │   │   ├── RejectPaymentRequest.java
                            │   │   │   └── UpdatePaymentRequest.java
                            │   │   └── response
                            │   │       ├── PaymentListResponse.java
                            │   │       ├── PaymentResponse.java
                            │   │       └── PaymentResultResponse.java
                            │   ├── mappers
                            │   │   └── PaymentMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── CashInvoiceSettlement.java
                            │   │   │   ├── CreditStatusSnapshot.java
                            │   │   │   ├── CustomerCreditQuery.java
                            │   │   │   ├── InvoicePaymentTotalQuery.java
                            │   │   │   ├── InvoicePaymentTotalsQuery.java
                            │   │   │   ├── OverdueInvoiceSnapshot.java
                            │   │   │   ├── PaymentDocumentQuery.java
                            │   │   │   ├── PaymentDocumentSnapshot.java
                            │   │   │   ├── QueryPaymentMethodUsage.java
                            │   │   │   └── ReturnPaymentOperations.java
                            │   │   └── out
                            │   │       ├── model
                            │   │       │   ├── CustomerCreditContext.java
                            │   │       │   └── InvoiceCreditEntry.java
                            │   │       ├── CashRegisterGateway.java
                            │   │       ├── CreditNoteLedgerGateway.java
                            │   │       ├── CustomerCreditContextGateway.java
                            │   │       ├── DebitNoteLedgerGateway.java
                            │   │       ├── InvoiceCreditLedgerGateway.java
                            │   │       ├── PaymentActorGateway.java
                            │   │       ├── PaymentCustomerGateway.java
                            │   │       ├── PaymentInvoiceGateway.java
                            │   │       └── PaymentNotificationGateway.java
                            │   ├── services
                            │   │   ├── CashInvoiceSettlementService.java
                            │   │   ├── CustomerCreditQueryService.java
                            │   │   ├── PaymentDocumentQueryService.java
                            │   │   └── ReturnPaymentOperationsService.java
                            │   └── usecases
                            │       ├── ApprovePaymentUseCase.java
                            │       ├── CreatePaymentUseCase.java
                            │       ├── DeletePaymentUseCase.java
                            │       ├── GetAllPaymentsUseCase.java
                            │       ├── GetPaymentByIdUseCase.java
                            │       ├── PayAllCustomerDebtUseCase.java
                            │       ├── PayInvoiceUseCase.java
                            │       ├── PayPartialDebtUseCase.java
                            │       ├── RejectPaymentUseCase.java
                            │       └── UpdatePaymentUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   ├── Payment.java
                            │   │   └── PaymentAllocation.java
                            │   ├── repositories
                            │   │   ├── PaymentAllocationRepository.java
                            │   │   └── PaymentRepository.java
                            │   ├── services
                            │   │   ├── CustomerCreditPolicy.java
                            │   │   └── PaymentAllocationService.java
                            │   └── valueobjects
                            │       ├── InvoiceRemainingBalance.java
                            │       ├── PaymentId.java
                            │       ├── PaymentNumber.java
                            │       ├── PaymentSource.java
                            │       └── PaymentStatus.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── PaymentController.java
                                │   └── out
                                │       ├── integration
                                │       │   ├── LocalCreditNoteLedgerGateway.java
                                │       │   ├── LocalCustomerCreditContextGateway.java
                                │       │   ├── LocalDebitNoteLedgerGateway.java
                                │       │   ├── LocalInvoiceCreditLedgerGateway.java
                                │       │   ├── LocalPaymentActorGateway.java
                                │       │   ├── LocalPaymentCashRegisterGateway.java
                                │       │   ├── LocalPaymentCustomerGateway.java
                                │       │   ├── LocalPaymentInvoiceGateway.java
                                │       │   ├── LocalPaymentNotificationGateway.java
                                │       │   ├── RepositoryInvoicePaymentTotalQuery.java
                                │       │   └── RepositoryInvoicePaymentTotalsQuery.java
                                │       └── persistence
                                │           ├── adapters
                                │           │   ├── PaymentAllocationRepositoryAdapter.java
                                │           │   └── PaymentRepositoryAdapter.java
                                │           ├── entities
                                │           │   ├── PaymentAllocationEntity.java
                                │           │   └── PaymentEntity.java
                                │           ├── mappers
                                │           │   ├── PaymentAllocationJpaMapper.java
                                │           │   └── PaymentJpaMapper.java
                                │           ├── queries
                                │           │   └── JpaPaymentMethodUsageQuery.java
                                │           └── repositories
                                │               ├── PaymentAllocationJpaRepository.java
                                │               └── PaymentJpaRepository.java
                                └── config
                                    ├── CashInvoiceSettlementConfig.java
                                    ├── PaymentConfig.java
                                    ├── PaymentDocumentQueryConfig.java
                                    └── ReturnPaymentOperationsConfig.java
```

## price_types

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── price_types
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── CreatePriceTypeRequest.java
                            │   │   │   └── UpdatePriceTypeRequest.java
                            │   │   └── response
                            │   │       └── PriceTypeResponse.java
                            │   ├── exceptions
                            │   │   ├── DuplicatePriceTypeNameException.java
                            │   │   ├── PriceTypeInUseException.java
                            │   │   └── PriceTypeNotFoundException.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── ProductPriceTypeExistenceQuery.java
                            │   │   │   └── ProductPriceTypeQuery.java
                            │   │   └── out
                            │   │       └── PriceTypeUsageGateway.java
                            │   ├── queries
                            │   │   └── RepositoryProductPriceTypeQuery.java
                            │   └── usecases
                            │       ├── CreatePriceTypeUseCase.java
                            │       ├── DeletePriceTypeUseCase.java
                            │       ├── GetPriceTypeUseCase.java
                            │       ├── ListPriceTypesUseCase.java
                            │       └── UpdatePriceTypeUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   └── PriceType.java
                            │   └── repositories
                            │       └── PriceTypeRepository.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── PriceTypeController.java
                                │   └── out
                                │       ├── integration
                                │       │   └── ProductPriceTypeUsageAdapter.java
                                │       └── persistence
                                │           ├── adapters
                                │           │   └── JpaPriceTypeRepository.java
                                │           ├── entities
                                │           │   └── PriceTypeEntity.java
                                │           ├── queries
                                │           │   └── JpaProductPriceTypeExistenceQuery.java
                                │           └── repositories
                                │               └── SpringDataPriceTypeRepository.java
                                └── config
                                    ├── PriceTypeConfig.java
                                    └── ProductPriceTypeQueryConfig.java
```

## products

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── products
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── CreateProductRequest.java
                            │   │   │   ├── ProductPriceRequest.java
                            │   │   │   └── UpdateProductRequest.java
                            │   │   └── response
                            │   │       ├── ProductDetailResponse.java
                            │   │       ├── ProductListResponse.java
                            │   │       ├── ProductPriceDetailResponse.java
                            │   │       ├── ProductStockByWarehouseResponse.java
                            │   │       └── ProductWithStockResponse.java
                            │   ├── exceptions
                            │   │   ├── CompositeProductCannotBeComponentException.java
                            │   │   ├── DuplicateProductCodeException.java
                            │   │   ├── DuplicateProductComponentException.java
                            │   │   ├── ProductHasAssociatedRecordsException.java
                            │   │   ├── ProductHasStockException.java
                            │   │   └── ProductNotFoundException.java
                            │   ├── mappers
                            │   │   └── ProductMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── CategoryProductUsageApi.java
                            │   │   │   ├── PriceTypeProductUsageApi.java
                            │   │   │   ├── PurchaseProductCatalogQuery.java
                            │   │   │   ├── SalesProductCatalogEntry.java
                            │   │   │   ├── SalesProductCatalogQuery.java
                            │   │   │   ├── StockCatalogEntry.java
                            │   │   │   └── StockCatalogQuery.java
                            │   │   └── out
                            │   │       ├── CustomerProductContextGateway.java
                            │   │       ├── ProductCategoryGateway.java
                            │   │       ├── ProductFileStorageGateway.java
                            │   │       ├── ProductPriceTypeExistenceGateway.java
                            │   │       ├── ProductPriceTypeGateway.java
                            │   │       ├── ProductReferenceGateway.java
                            │   │       ├── ProductStockFilterGateway.java
                            │   │       └── ProductWarehouseStockGateway.java
                            │   ├── queries
                            │   │   ├── RepositoryPurchaseProductCatalogQuery.java
                            │   │   └── RepositoryStockCatalogQuery.java
                            │   └── usecases
                            │       ├── CreateProductUseCase.java
                            │       ├── DeleteProductUseCase.java
                            │       ├── GetProductStockUseCase.java
                            │       ├── GetProductUseCase.java
                            │       ├── GetProductsForTransactionUseCase.java
                            │       ├── ListProductsUseCase.java
                            │       ├── RemoveProductPhotoUseCase.java
                            │       ├── UpdateProductUseCase.java
                            │       └── UploadProductPhotoUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   ├── Product.java
                            │   │   └── ProductComponent.java
                            │   ├── exceptions
                            │   │   └── ProductReferencedException.java
                            │   ├── repositories
                            │   │   └── ProductRepository.java
                            │   └── valueobjects
                            │       └── ProductPrice.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── ProductController.java
                                │   └── out
                                │       ├── integration
                                │       │   ├── LocalCustomerProductContextGatewayAdapter.java
                                │       │   ├── LocalProductCategoryGatewayAdapter.java
                                │       │   ├── LocalProductFileStorageGatewayAdapter.java
                                │       │   ├── LocalProductPriceTypeExistenceGatewayAdapter.java
                                │       │   ├── LocalProductPriceTypeGatewayAdapter.java
                                │       │   ├── LocalProductReferenceGatewayAdapter.java
                                │       │   ├── LocalProductStockFilterGatewayAdapter.java
                                │       │   ├── LocalProductWarehouseStockGatewayAdapter.java
                                │       │   └── LocalSalesProductCatalogQuery.java
                                │       └── persistence
                                │           ├── adapters
                                │           │   └── JpaProductRepository.java
                                │           ├── entities
                                │           │   ├── ProductCodeSequenceEntity.java
                                │           │   ├── ProductComponentEntity.java
                                │           │   ├── ProductEntity.java
                                │           │   └── ProductPriceEntity.java
                                │           ├── queries
                                │           │   └── JpaProductUsageQueries.java
                                │           └── repositories
                                │               ├── SpringDataProductCodeSequenceRepository.java
                                │               ├── SpringDataProductRepository.java
                                │               └── SpringDataProductUsageRepository.java
                                └── config
                                    ├── ProductConfig.java
                                    ├── PurchaseProductCatalogConfig.java
                                    └── StockCatalogConfig.java
```

## purchases

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── purchases
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── CancelPurchaseRequest.java
                            │   │   │   ├── CreatePurchaseRequest.java
                            │   │   │   └── UpdatePurchaseRequest.java
                            │   │   └── response
                            │   │       ├── PurchaseDetailResponse.java
                            │   │       ├── PurchaseListResponse.java
                            │   │       └── PurchaseProductResponse.java
                            │   ├── mappers
                            │   │   └── PurchaseMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── PurchaseDocumentFilter.java
                            │   │   │   ├── PurchaseDocumentQuery.java
                            │   │   │   ├── PurchaseDocumentRow.java
                            │   │   │   ├── PurchaseDocumentSnapshot.java
                            │   │   │   └── SupplierPurchaseUsageQuery.java
                            │   │   └── out
                            │   │       ├── InventoryGateway.java
                            │   │       ├── PurchaseActorGateway.java
                            │   │       ├── PurchaseProductCatalogGateway.java
                            │   │       ├── PurchaseProductGateway.java
                            │   │       ├── PurchaseSupplierGateway.java
                            │   │       └── PurchaseWarehouseGateway.java
                            │   ├── queries
                            │   │   └── RepositorySupplierPurchaseUsageQuery.java
                            │   ├── services
                            │   │   ├── PurchaseDetailResponseAssembler.java
                            │   │   └── PurchaseDocumentQueryService.java
                            │   └── usecases
                            │       ├── CancelPurchaseUseCase.java
                            │       ├── ConfirmPurchaseUseCase.java
                            │       ├── CreatePurchaseUseCase.java
                            │       ├── DeletePurchaseUseCase.java
                            │       ├── GetProductsForPurchaseUseCase.java
                            │       ├── GetPurchaseByIdUseCase.java
                            │       ├── ListPurchasesUseCase.java
                            │       └── UpdatePurchaseUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   ├── Purchase.java
                            │   │   ├── PurchaseLine.java
                            │   │   └── PurchaseLineAllocation.java
                            │   ├── exceptions
                            │   │   └── PurchaseNotFoundException.java
                            │   ├── repositories
                            │   │   └── PurchaseRepository.java
                            │   └── valueobjects
                            │       ├── PurchaseId.java
                            │       ├── PurchaseLineAllocationId.java
                            │       ├── PurchaseLineId.java
                            │       ├── PurchaseNumber.java
                            │       └── PurchaseStatus.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── PurchaseController.java
                                │   └── out
                                │       ├── integration
                                │       │   ├── LocalPurchaseActorGatewayAdapter.java
                                │       │   ├── LocalPurchaseProductGatewayAdapter.java
                                │       │   ├── LocalPurchaseSupplierGatewayAdapter.java
                                │       │   ├── LocalPurchaseWarehouseGatewayAdapter.java
                                │       │   └── WarehouseInventoryAdapter.java
                                │       ├── persistence
                                │       │   ├── adapters
                                │       │   │   └── PurchaseRepositoryAdapter.java
                                │       │   ├── entities
                                │       │   │   ├── PurchaseEntity.java
                                │       │   │   ├── PurchaseLineAllocationEntity.java
                                │       │   │   ├── PurchaseLineEntity.java
                                │       │   │   └── PurchaseNumberSequenceEntity.java
                                │       │   ├── mappers
                                │       │   │   └── PurchaseJpaMapper.java
                                │       │   └── repositories
                                │       │       ├── PurchaseJpaRepository.java
                                │       │       └── PurchaseNumberSequenceJpaRepository.java
                                │       └── LocalPurchaseProductCatalogGateway.java
                                └── config
                                    ├── PurchaseConfig.java
                                    ├── PurchaseDocumentQueryConfig.java
                                    └── SupplierPurchaseUsageConfig.java
```

## quotes

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── quotes
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── AssignQuoteWarehousesRequest.java
                            │   │   │   ├── CancelQuoteRequest.java
                            │   │   │   ├── ConvertQuoteToInvoiceRequest.java
                            │   │   │   ├── CreateQuoteRequest.java
                            │   │   │   └── UpdateQuoteRequest.java
                            │   │   └── response
                            │   │       ├── QuoteAllocationDetailResponse.java
                            │   │       ├── QuoteConvertedToInvoiceResponse.java
                            │   │       ├── QuoteConvertedToOrderResponse.java
                            │   │       ├── QuoteDetailResponse.java
                            │   │       ├── QuoteLineDetailResponse.java
                            │   │       ├── QuoteListResponse.java
                            │   │       ├── QuoteProductResponse.java
                            │   │       ├── QuoteWarehouseResponse.java
                            │   │       └── WarehouseStockInfoResponse.java
                            │   ├── mappers
                            │   │   └── QuoteMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── QuoteDocumentFilter.java
                            │   │   │   ├── QuoteDocumentQuery.java
                            │   │   │   ├── QuoteDocumentRow.java
                            │   │   │   └── QuoteDocumentSnapshot.java
                            │   │   └── out
                            │   │       ├── ActorIdentityGateway.java
                            │   │       ├── InvoiceCreationGateway.java
                            │   │       ├── OrderCreationGateway.java
                            │   │       ├── QuoteConversionNotificationGateway.java
                            │   │       ├── QuoteCustomerGateway.java
                            │   │       ├── QuoteProductCatalogGateway.java
                            │   │       ├── QuoteRejectionNotificationGateway.java
                            │   │       └── QuoteWarehouseGateway.java
                            │   ├── services
                            │   │   └── QuoteDocumentQueryService.java
                            │   └── usecases
                            │       ├── AssignQuoteWarehousesUseCase.java
                            │       ├── CancelQuoteUseCase.java
                            │       ├── ConvertQuoteToInvoiceUseCase.java
                            │       ├── ConvertQuoteToOrderUseCase.java
                            │       ├── CreateQuoteUseCase.java
                            │       ├── DeleteQuoteUseCase.java
                            │       ├── GetProductsForQuoteUseCase.java
                            │       ├── GetQuoteByIdUseCase.java
                            │       ├── GetQuotesUseCase.java
                            │       ├── GetWarehousesForQuoteUseCase.java
                            │       └── UpdateQuoteUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   ├── Quote.java
                            │   │   ├── QuoteAllocation.java
                            │   │   └── QuoteLine.java
                            │   ├── exceptions
                            │   │   ├── InvalidQuoteAllocationException.java
                            │   │   ├── InvalidQuoteLineException.java
                            │   │   ├── QuoteNotFoundException.java
                            │   │   └── QuoteStateException.java
                            │   ├── repositories
                            │   │   └── QuoteRepository.java
                            │   └── valueobjects
                            │       ├── QuoteAllocationId.java
                            │       ├── QuoteLineId.java
                            │       ├── QuoteNumber.java
                            │       └── QuoteStatus.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── QuoteController.java
                                │   └── out
                                │       ├── integration
                                │       │   ├── LocalInvoiceCreationGateway.java
                                │       │   ├── LocalOrderCreationGateway.java
                                │       │   ├── LocalQuoteCustomerAdapter.java
                                │       │   ├── LocalQuoteProductCatalogAdapter.java
                                │       │   ├── LocalQuoteRejectionNotificationGateway.java
                                │       │   ├── LocalQuoteWarehouseAdapter.java
                                │       │   ├── QuoteActorIdentityAdapter.java
                                │       │   └── QuoteConversionNotificationAdapter.java
                                │       └── persistence
                                │           ├── adapters
                                │           │   └── JpaQuoteRepositoryAdapter.java
                                │           ├── entities
                                │           │   ├── QuoteAllocationJpaEntity.java
                                │           │   ├── QuoteJpaEntity.java
                                │           │   ├── QuoteLineCategorySnapshotJpaEntity.java
                                │           │   ├── QuoteLineJpaEntity.java
                                │           │   └── QuoteNumberSequenceJpaEntity.java
                                │           ├── mappers
                                │           │   └── QuoteJpaMapper.java
                                │           └── repositories
                                │               ├── QuoteJpaRepository.java
                                │               └── QuoteNumberSequenceJpaRepository.java
                                └── config
                                    ├── QuoteConfig.java
                                    └── QuoteDocumentQueryConfig.java
```

## receivables

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── receivables
                            ├── application
                            │   ├── dto
                            │   │   └── response
                            │   │       ├── CustomerDebtListResponse.java
                            │   │       ├── CustomerDebtSummaryResponse.java
                            │   │       ├── CustomerInvoiceDebtResponse.java
                            │   │       ├── PendingPaymentResponse.java
                            │   │       └── ReceivablesSummaryResponse.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── AccountsReceivableReportQuery.java
                            │   │   │   └── AccountsReceivableReportSnapshot.java
                            │   │   └── out
                            │   │       ├── model
                            │   │       │   └── AccountsReceivableRow.java
                            │   │       ├── ReceivablesCustomerProfileGateway.java
                            │   │       └── ReceivablesQueryGateway.java
                            │   ├── services
                            │   │   └── AccountsReceivableReportQueryService.java
                            │   └── usecases
                            │       ├── GetCustomerDebtUseCase.java
                            │       ├── GetCustomerInvoicesUseCase.java
                            │       ├── GetPaymentHistoryUseCase.java
                            │       ├── GetReceivablesSummaryUseCase.java
                            │       └── SearchCustomerDebtUseCase.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── ReceivablesController.java
                                │   └── out
                                │       ├── integration
                                │       │   └── LocalReceivablesCustomerProfileGatewayAdapter.java
                                │       └── persistence
                                │           ├── adapters
                                │           │   └── ReceivablesRepositoryAdapter.java
                                │           └── repositories
                                │               └── ReceivablesJpaRepository.java
                                └── config
                                    ├── AccountsReceivableReportQueryConfig.java
                                    └── ReceivablesConfig.java
```

## reports

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── reports
                            ├── finance
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── AccountsReceivableReportData.java
                            │   │   │   ├── CollectionsReportData.java
                            │   │   │   ├── PaymentHistoryReportData.java
                            │   │   │   └── PaymentReportData.java
                            │   │   ├── ports
                            │   │   │   └── out
                            │   │   │       ├── AccountsReceivableQueryGateway.java
                            │   │   │       ├── AccountsReceivableReportPdfPort.java
                            │   │   │       ├── CollectionsReportPdfPort.java
                            │   │   │       ├── PaymentHistoryReportPdfPort.java
                            │   │   │       ├── PaymentPdfPort.java
                            │   │   │       └── PaymentReportQueryGateway.java
                            │   │   └── usecases
                            │   │       ├── GenerateAccountsReceivableReportPdfUseCase.java
                            │   │       ├── GenerateAccountsReceivableReportPdfUseCaseImpl.java
                            │   │       ├── GenerateCollectionsReportPdfUseCase.java
                            │   │       ├── GenerateCollectionsReportPdfUseCaseImpl.java
                            │   │       ├── GeneratePaymentHistoryReportPdfUseCase.java
                            │   │       ├── GeneratePaymentHistoryReportPdfUseCaseImpl.java
                            │   │       ├── GeneratePaymentPdfUseCase.java
                            │   │       └── GeneratePaymentPdfUseCaseImpl.java
                            │   └── infrastructure
                            │       ├── adapters
                            │       │   ├── in
                            │       │   │   └── controllers
                            │       │   │       └── FinancesReportController.java
                            │       │   └── out
                            │       │       ├── integration
                            │       │       │   ├── PaymentDocumentQueryAdapter.java
                            │       │       │   └── ReceivablesReportQueryAdapter.java
                            │       │       └── pdf
                            │       │           ├── AccountsReceivableReportPdfGenerator.java
                            │       │           ├── CollectionsReportPdfGenerator.java
                            │       │           ├── PaymentHistoryReportPdfGenerator.java
                            │       │           └── PaymentPdfGenerator.java
                            │       └── config
                            │           └── FinancesReportConfig.java
                            ├── infrastructure
                            │   └── adapters
                            │       └── out
                            │           └── pdf
                            │               ├── PdfBranding.java
                            │               └── PdfStatusColors.java
                            ├── inventory
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── query
                            │   │   │   │   ├── CatalogPdfQuery.java
                            │   │   │   │   ├── CatalogReportQuery.java
                            │   │   │   │   └── CommercialCatalogQuery.java
                            │   │   │   └── response
                            │   │   │       ├── CatalogReportResponse.java
                            │   │   │       └── CommercialCatalogResponse.java
                            │   │   ├── ports
                            │   │   │   └── out
                            │   │   │       ├── CatalogPdfPort.java
                            │   │   │       ├── CatalogReportRepositoryPort.java
                            │   │   │       ├── CommercialCatalogPdfPort.java
                            │   │   │       ├── CommercialCatalogRepositoryPort.java
                            │   │   │       └── MediaGateway.java
                            │   │   └── usecases
                            │   │       ├── GenerateCatalogPdfUseCase.java
                            │   │       ├── GenerateCatalogPdfUseCaseImpl.java
                            │   │       ├── GenerateCommercialCatalogPdfUseCase.java
                            │   │       ├── GenerateCommercialCatalogPdfUseCaseImpl.java
                            │   │       ├── GetCatalogReportUseCase.java
                            │   │       └── GetCatalogReportUseCaseImpl.java
                            │   └── infrastructure
                            │       ├── adapters
                            │       │   ├── in
                            │       │   │   └── controllers
                            │       │   │       ├── CatalogReportController.java
                            │       │   │       └── CommercialCatalogController.java
                            │       │   └── out
                            │       │       ├── integration
                            │       │       │   └── FileStorageMediaAdapter.java
                            │       │       ├── pdf
                            │       │       │   ├── CatalogPdfGenerator.java
                            │       │       │   └── CommercialCatalogPdfGenerator.java
                            │       │       └── persistence
                            │       │           ├── adapters
                            │       │           │   ├── CatalogReportRepositoryAdapter.java
                            │       │           │   └── CommercialCatalogRepositoryAdapter.java
                            │       │           ├── projections
                            │       │           │   ├── CatalogReportProjection.java
                            │       │           │   └── CommercialCatalogProjection.java
                            │       │           └── repositories
                            │       │               ├── CatalogReportJpaRepository.java
                            │       │               └── CommercialCatalogJpaRepository.java
                            │       └── config
                            │           └── InventoryReportConfig.java
                            ├── invoices
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── InvoiceReportData.java
                            │   │   │   ├── InvoiceReportFilter.java
                            │   │   │   └── InvoiceReportRow.java
                            │   │   ├── ports
                            │   │   │   └── out
                            │   │   │       ├── InvoicePdfPort.java
                            │   │   │       ├── InvoiceReportQueryGateway.java
                            │   │   │       └── InvoicesReportPdfPort.java
                            │   │   └── usecases
                            │   │       ├── GenerateInvoicePdfUseCase.java
                            │   │       ├── GenerateInvoicePdfUseCaseImpl.java
                            │   │       ├── GenerateInvoicesReportPdfUseCase.java
                            │   │       └── GenerateInvoicesReportPdfUseCaseImpl.java
                            │   └── infrastructure
                            │       ├── adapters
                            │       │   ├── in
                            │       │   │   └── controllers
                            │       │   │       └── InvoicesReportController.java
                            │       │   └── out
                            │       │       ├── integration
                            │       │       │   └── InvoiceDocumentQueryAdapter.java
                            │       │       └── pdf
                            │       │           ├── InvoicePdfGenerator.java
                            │       │           └── InvoicesReportPdfGenerator.java
                            │       └── config
                            │           └── InvoicesReportConfig.java
                            ├── movements
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── query
                            │   │   │   │   └── MovementsPdfQuery.java
                            │   │   │   └── response
                            │   │   │       └── MovementReportResponse.java
                            │   │   ├── ports
                            │   │   │   └── out
                            │   │   │       ├── MovementsExcelPort.java
                            │   │   │       ├── MovementsPdfPort.java
                            │   │   │       └── MovementsReportRepositoryPort.java
                            │   │   └── usecases
                            │   │       ├── GenerateMovementsExcelUseCase.java
                            │   │       ├── GenerateMovementsExcelUseCaseImpl.java
                            │   │       ├── GenerateMovementsPdfUseCase.java
                            │   │       └── GenerateMovementsPdfUseCaseImpl.java
                            │   └── infrastructure
                            │       ├── adapters
                            │       │   ├── in
                            │       │   │   └── controllers
                            │       │   │       └── MovementsReportController.java
                            │       │   └── out
                            │       │       ├── excel
                            │       │       │   └── MovementsExcelGenerator.java
                            │       │       ├── pdf
                            │       │       │   └── MovementsPdfGenerator.java
                            │       │       └── persistence
                            │       │           ├── adapters
                            │       │           │   └── MovementsReportRepositoryAdapter.java
                            │       │           ├── projections
                            │       │           │   └── MovementsReportProjection.java
                            │       │           └── repositories
                            │       │               └── MovementsReportJpaRepository.java
                            │       └── config
                            │           └── MovementsReportConfig.java
                            ├── orders
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── OrderReportData.java
                            │   │   │   ├── OrderReportFilter.java
                            │   │   │   └── OrderReportRow.java
                            │   │   ├── ports
                            │   │   │   └── out
                            │   │   │       ├── OrderPdfPort.java
                            │   │   │       ├── OrderReportQueryGateway.java
                            │   │   │       └── OrdersReportPdfPort.java
                            │   │   └── usecases
                            │   │       ├── GenerateOrderPdfUseCase.java
                            │   │       ├── GenerateOrderPdfUseCaseImpl.java
                            │   │       ├── GenerateOrdersReportPdfUseCase.java
                            │   │       └── GenerateOrdersReportPdfUseCaseImpl.java
                            │   └── infrastructure
                            │       ├── adapters
                            │       │   ├── in
                            │       │   │   └── controllers
                            │       │   │       └── OrdersReportController.java
                            │       │   └── out
                            │       │       ├── integration
                            │       │       │   └── OrderDocumentQueryAdapter.java
                            │       │       └── pdf
                            │       │           ├── OrderPdfGenerator.java
                            │       │           └── OrdersReportPdfGenerator.java
                            │       └── config
                            │           └── OrdersReportConfig.java
                            ├── purchases
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── PurchaseReportData.java
                            │   │   │   ├── PurchaseReportFilter.java
                            │   │   │   └── PurchaseReportRow.java
                            │   │   ├── ports
                            │   │   │   └── out
                            │   │   │       ├── PurchasePdfPort.java
                            │   │   │       ├── PurchaseReportQueryGateway.java
                            │   │   │       └── PurchasesReportPdfPort.java
                            │   │   └── usecases
                            │   │       ├── GeneratePurchasePdfUseCase.java
                            │   │       ├── GeneratePurchasePdfUseCaseImpl.java
                            │   │       ├── GeneratePurchasesReportPdfUseCase.java
                            │   │       └── GeneratePurchasesReportPdfUseCaseImpl.java
                            │   └── infrastructure
                            │       ├── adapters
                            │       │   ├── in
                            │       │   │   └── controllers
                            │       │   │       └── PurchasesReportController.java
                            │       │   └── out
                            │       │       ├── integration
                            │       │       │   └── PurchaseDocumentQueryAdapter.java
                            │       │       └── pdf
                            │       │           ├── PurchasePdfGenerator.java
                            │       │           └── PurchasesReportPdfGenerator.java
                            │       └── config
                            │           └── PurchasesReportConfig.java
                            ├── quotes
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── QuoteReportData.java
                            │   │   │   ├── QuoteReportFilter.java
                            │   │   │   └── QuoteReportRow.java
                            │   │   ├── ports
                            │   │   │   └── out
                            │   │   │       ├── QuotePdfPort.java
                            │   │   │       ├── QuoteReportQueryGateway.java
                            │   │   │       └── QuotesReportPdfPort.java
                            │   │   └── usecases
                            │   │       ├── GenerateQuotePdfUseCase.java
                            │   │       ├── GenerateQuotePdfUseCaseImpl.java
                            │   │       ├── GenerateQuotesReportPdfUseCase.java
                            │   │       └── GenerateQuotesReportPdfUseCaseImpl.java
                            │   └── infrastructure
                            │       ├── adapters
                            │       │   ├── in
                            │       │   │   └── controllers
                            │       │   │       └── QuotesReportController.java
                            │       │   └── out
                            │       │       ├── integration
                            │       │       │   └── QuoteDocumentQueryAdapter.java
                            │       │       └── pdf
                            │       │           ├── QuotePdfGenerator.java
                            │       │           └── QuotesReportPdfGenerator.java
                            │       └── config
                            │           └── QuotesReportConfig.java
                            └── stock
                                ├── application
                                │   ├── dto
                                │   │   ├── query
                                │   │   │   └── StockPdfQuery.java
                                │   │   └── response
                                │   │       └── StockReportResponse.java
                                │   ├── ports
                                │   │   └── out
                                │   │       ├── StockExcelPort.java
                                │   │       ├── StockPdfPort.java
                                │   │       └── StockReportRepositoryPort.java
                                │   └── usecases
                                │       ├── GenerateStockExcelUseCase.java
                                │       ├── GenerateStockExcelUseCaseImpl.java
                                │       ├── GenerateStockPdfUseCase.java
                                │       └── GenerateStockPdfUseCaseImpl.java
                                └── infrastructure
                                    ├── adapters
                                    │   ├── in
                                    │   │   └── controllers
                                    │   │       └── StockReportController.java
                                    │   └── out
                                    │       ├── excel
                                    │       │   └── StockExcelGenerator.java
                                    │       ├── pdf
                                    │       │   └── StockPdfGenerator.java
                                    │       └── persistence
                                    │           ├── adapters
                                    │           │   └── StockReportRepositoryAdapter.java
                                    │           ├── projections
                                    │           │   └── StockReportProjection.java
                                    │           └── repositories
                                    │               └── StockReportJpaRepository.java
                                    └── config
                                        └── StockReportConfig.java
```

## returns

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── returns
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   └── CreateReturnRequest.java
                            │   │   └── response
                            │   │       ├── ReturnListResponse.java
                            │   │       └── ReturnResponse.java
                            │   ├── mappers
                            │   │   └── ReturnMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── InvoiceReturnDetailsQuery.java
                            │   │   │   └── ProductReferenceQuery.java
                            │   │   └── out
                            │   │       ├── ReturnBalanceGateway.java
                            │   │       ├── ReturnCashGateway.java
                            │   │       ├── ReturnInventoryGateway.java
                            │   │       ├── ReturnInvoiceAdjustmentGateway.java
                            │   │       ├── ReturnInvoiceQueryGateway.java
                            │   │       ├── ReturnPaymentSettlementGateway.java
                            │   │       └── ReturnProjectionQueryGateway.java
                            │   ├── services
                            │   │   ├── InvoiceReturnDetailsQueryService.java
                            │   │   └── ReturnResponseAssembler.java
                            │   └── usecases
                            │       ├── CreateReturnUseCase.java
                            │       ├── DeleteReturnUseCase.java
                            │       ├── GetAllReturnsUseCase.java
                            │       └── GetReturnByIdUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   ├── CancelledAllocation.java
                            │   │   ├── Return.java
                            │   │   ├── ReturnId.java
                            │   │   ├── ReturnLine.java
                            │   │   ├── ReturnLineAllocation.java
                            │   │   ├── ReturnNumber.java
                            │   │   ├── ReturnStatus.java
                            │   │   └── ReturnType.java
                            │   └── repositories
                            │       └── ReturnRepository.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── ReturnController.java
                                │   └── out
                                │       ├── integration
                                │       │   ├── InvoiceReturnAdjustmentAdapter.java
                                │       │   ├── LocalReturnBalanceGateway.java
                                │       │   ├── LocalReturnCashGateway.java
                                │       │   ├── LocalReturnInventoryGatewayAdapter.java
                                │       │   ├── LocalReturnInvoiceQueryGateway.java
                                │       │   ├── LocalReturnPaymentSettlementGateway.java
                                │       │   └── LocalReturnProjectionQueryGateway.java
                                │       └── persistence
                                │           ├── adapters
                                │           │   └── JpaReturnRepository.java
                                │           ├── entities
                                │           │   ├── ReturnCancelledAllocationEntity.java
                                │           │   ├── ReturnEntity.java
                                │           │   ├── ReturnLineAllocationEntity.java
                                │           │   ├── ReturnLineEntity.java
                                │           │   └── ReturnNumberSequenceEntity.java
                                │           ├── queries
                                │           │   └── JpaReturnsProductReferenceQuery.java
                                │           └── repositories
                                │               └── SpringDataReturnsProductReferenceRepository.java
                                └── config
                                    └── ReturnConfig.java
```

## shared

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── shared
                            ├── dto
                            │   └── PageResponse.java
                            ├── infrastructure
                            │   └── pagination
                            │       └── PageResponseMapper.java
                            └── storage
                                ├── domain
                                │   ├── FileNotFoundException.java
                                │   ├── FileStorageService.java
                                │   └── StorageException.java
                                └── infrastructure
                                    ├── FileController.java
                                    ├── LocalFileStorageService.java
                                    ├── S3FileStorageService.java
                                    └── StorageConfig.java
```

## suppliers

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── suppliers
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── AddressRequest.java
                            │   │   │   ├── CreateSupplierRequest.java
                            │   │   │   ├── EmailRequest.java
                            │   │   │   ├── PhoneRequest.java
                            │   │   │   └── UpdateSupplierRequest.java
                            │   │   └── response
                            │   │       ├── AddressResponse.java
                            │   │       ├── EmailResponse.java
                            │   │       ├── PhoneResponse.java
                            │   │       ├── SupplierCreateResponse.java
                            │   │       ├── SupplierDetailResponse.java
                            │   │       └── SupplierListResponse.java
                            │   ├── exceptions
                            │   │   ├── SupplierHasAssociatedRecordsException.java
                            │   │   └── SupplierNotFoundException.java
                            │   ├── mappers
                            │   │   └── SupplierMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   └── PurchaseSupplierQuery.java
                            │   │   └── out
                            │   │       └── PurchaseUsageGateway.java
                            │   └── usecases
                            │       ├── DeleteSupplierUseCase.java
                            │       ├── GetSupplierUseCase.java
                            │       ├── ListSuppliersUseCase.java
                            │       ├── RegisterSupplierUseCase.java
                            │       └── UpdateSupplierUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   ├── Supplier.java
                            │   │   ├── SupplierAddress.java
                            │   │   ├── SupplierEmail.java
                            │   │   └── SupplierPhone.java
                            │   ├── repositories
                            │   │   └── SupplierRepository.java
                            │   ├── services
                            │   │   └── SupplierCodeGenerator.java
                            │   └── valueobjects
                            │       ├── AddressId.java
                            │       ├── EmailAddress.java
                            │       ├── EmailId.java
                            │       ├── LegalName.java
                            │       ├── PhoneId.java
                            │       ├── PhoneNumber.java
                            │       ├── Rif.java
                            │       ├── SupplierAlias.java
                            │       ├── SupplierCode.java
                            │       ├── SupplierId.java
                            │       └── SupplierType.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       └── SupplierController.java
                                │   └── out
                                │       ├── integration
                                │       │   ├── LocalPurchaseUsageGatewayAdapter.java
                                │       │   └── SupplierPurchaseDirectoryAdapter.java
                                │       ├── persistence
                                │       │   ├── adapters
                                │       │   │   └── JpaSupplierRepository.java
                                │       │   ├── entities
                                │       │   │   ├── SupplierAddressEntity.java
                                │       │   │   ├── SupplierEmailEntity.java
                                │       │   │   ├── SupplierEntity.java
                                │       │   │   └── SupplierPhoneEntity.java
                                │       │   └── repositories
                                │       │       └── SpringDataSupplierRepository.java
                                │       └── services
                                │           └── SequentialSupplierCodeGenerator.java
                                └── config
                                    ├── PurchaseSupplierQueryConfig.java
                                    └── SupplierConfig.java
```

## warehouses

```text
└── src
    └── main
        └── java
            └── com
                └── cashly
                    └── cashly_api
                        └── warehouses
                            ├── application
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── CreateWarehouseRequest.java
                            │   │   │   ├── StockAdjustmentRequest.java
                            │   │   │   ├── StockBatchItem.java
                            │   │   │   ├── StockEntryRequest.java
                            │   │   │   ├── StockExitRequest.java
                            │   │   │   ├── TransferStockRequest.java
                            │   │   │   └── UpdateWarehouseRequest.java
                            │   │   └── response
                            │   │       ├── StockMovementResponse.java
                            │   │       ├── WarehouseDetailResponse.java
                            │   │       ├── WarehouseListResponse.java
                            │   │       └── WarehouseStockResponse.java
                            │   ├── exceptions
                            │   │   ├── DuplicateWarehouseCodeException.java
                            │   │   ├── InsufficientStockException.java
                            │   │   ├── ProductNotInWarehouseException.java
                            │   │   ├── WarehouseHasActiveStockException.java
                            │   │   ├── WarehouseNotActiveException.java
                            │   │   └── WarehouseNotFoundException.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── AvailableStockProductsQuery.java
                            │   │   │   ├── InvoiceInventoryApi.java
                            │   │   │   ├── InvoiceStockCommand.java
                            │   │   │   ├── ProductReferenceQuery.java
                            │   │   │   ├── ProductStockQuery.java
                            │   │   │   ├── PurchaseInventoryApi.java
                            │   │   │   ├── PurchaseStockCommand.java
                            │   │   │   ├── PurchaseWarehouseQuery.java
                            │   │   │   ├── ReturnInventoryApi.java
                            │   │   │   ├── ReturnStockCommand.java
                            │   │   │   ├── StockAvailabilityQuery.java
                            │   │   │   ├── StockDeductionApi.java
                            │   │   │   ├── WarehouseDirectoryEntry.java
                            │   │   │   └── WarehouseDirectoryQuery.java
                            │   │   └── out
                            │   │       ├── CustomerWarehouseAccessGateway.java
                            │   │       └── StockCatalogGateway.java
                            │   ├── queries
                            │   │   └── RepositoryPurchaseWarehouseQuery.java
                            │   ├── services
                            │   │   ├── InvoiceInventoryService.java
                            │   │   ├── PurchaseInventoryService.java
                            │   │   ├── RepositoryProductStockQuery.java
                            │   │   ├── RepositoryWarehouseDirectoryQuery.java
                            │   │   ├── ReturnInventoryService.java
                            │   │   ├── StockAvailabilityQueryService.java
                            │   │   └── StockDeductionService.java
                            │   └── usecases
                            │       ├── CreateWarehouseUseCase.java
                            │       ├── DeleteWarehouseUseCase.java
                            │       ├── GetStockMovementsUseCase.java
                            │       ├── GetWarehouseStockUseCase.java
                            │       ├── GetWarehouseUseCase.java
                            │       ├── ListWarehousesUseCase.java
                            │       ├── StockAdjustmentUseCase.java
                            │       ├── StockEntryUseCase.java
                            │       ├── StockExitUseCase.java
                            │       ├── TransferStockUseCase.java
                            │       └── UpdateWarehouseUseCase.java
                            ├── domain
                            │   ├── entities
                            │   │   ├── MovementType.java
                            │   │   ├── ReferenceType.java
                            │   │   ├── StockMovement.java
                            │   │   ├── Warehouse.java
                            │   │   └── WarehouseStock.java
                            │   └── repositories
                            │       ├── StockMovementRepository.java
                            │       ├── WarehouseRepository.java
                            │       └── WarehouseStockRepository.java
                            └── infrastructure
                                ├── adapters
                                │   ├── in
                                │   │   └── controllers
                                │   │       ├── StockMovementController.java
                                │   │       ├── WarehouseController.java
                                │   │       └── WarehouseStockController.java
                                │   └── out
                                │       ├── integration
                                │       │   ├── CustomerWarehouseAccessAdapter.java
                                │       │   └── LocalStockCatalogGatewayAdapter.java
                                │       └── persistence
                                │           ├── adapters
                                │           │   ├── JpaStockMovementRepository.java
                                │           │   ├── JpaWarehouseRepository.java
                                │           │   └── JpaWarehouseStockRepository.java
                                │           ├── entities
                                │           │   ├── StockMovementEntity.java
                                │           │   ├── WarehouseEntity.java
                                │           │   └── WarehouseStockEntity.java
                                │           ├── queries
                                │           │   ├── JpaAvailableStockProductsQuery.java
                                │           │   └── JpaWarehousesProductReferenceQuery.java
                                │           └── repositories
                                │               ├── SpringDataAvailableStockProductsRepository.java
                                │               ├── SpringDataStockMovementRepository.java
                                │               ├── SpringDataWarehouseRepository.java
                                │               ├── SpringDataWarehouseStockRepository.java
                                │               └── SpringDataWarehousesProductReferenceRepository.java
                                └── config
                                    ├── ProductStockQueryConfig.java
                                    ├── PurchaseInventoryConfig.java
                                    ├── PurchaseWarehouseQueryConfig.java
                                    ├── ReturnInventoryConfig.java
                                    ├── StockAvailabilityQueryConfig.java
                                    ├── StockDeductionConfig.java
                                    ├── WarehouseConfig.java
                                    └── WarehouseDirectoryConfig.java
```

## All tests including helpers and noncompiled backups

```text
└── src
    └── test
        ├── java
        │   └── com
        │       └── cashly
        │           └── cashly_api
        │               ├── architecture
        │               │   ├── ApplicationPackageConventionTest.java
        │               │   └── LayerDependencyTest.java
        │               ├── auth
        │               │   ├── authentication
        │               │   │   ├── application
        │               │   │   │   ├── dto
        │               │   │   │   │   ├── request
        │               │   │   │   │   │   └── LoginRequestUnitTest.java
        │               │   │   │   │   └── LoginRequestUnitTest.java
        │               │   │   │   ├── exceptions
        │               │   │   │   │   └── InvalidCredentialsExceptionUnitTest.java
        │               │   │   │   └── usecases
        │               │   │   │       └── LoginUserUseCaseTest.java
        │               │   │   ├── domain
        │               │   │   │   └── services
        │               │   │   │       └── AuthenticationDomainServiceUnitTest.java
        │               │   │   └── infrastructure
        │               │   │       └── adapters
        │               │   │           └── in
        │               │   │               ├── controllers
        │               │   │               │   └── AuthControllerUnitTest.java
        │               │   │               └── security
        │               │   │                   ├── JwtAuthenticationEntryPointUnitTest.java
        │               │   │                   └── JwtAuthenticationFilterUnitTest.java
        │               │   ├── token
        │               │   │   ├── application
        │               │   │   │   ├── dto
        │               │   │   │   │   ├── request
        │               │   │   │   │   │   ├── LogoutRequestUnitTest.java
        │               │   │   │   │   │   └── RefreshTokenRequestUnitTest.java
        │               │   │   │   │   ├── LogoutRequestUnitTest.java
        │               │   │   │   │   └── RefreshTokenRequestUnitTest.java
        │               │   │   │   ├── exceptions
        │               │   │   │   │   ├── InvalidTokenExceptionUnitTest.java
        │               │   │   │   │   └── TokenExpiredExceptionUnitTest.java
        │               │   │   │   └── usecases
        │               │   │   │       ├── LogoutUserUseCaseUnitTest.java
        │               │   │   │       ├── RefreshTokenBoundaryTest.java
        │               │   │   │       └── RefreshTokenUseCaseUnitTest.java
        │               │   │   ├── domain
        │               │   │   │   ├── entities
        │               │   │   │   │   └── RefreshTokenUnitTest.java
        │               │   │   │   └── valueobjects
        │               │   │   │       └── RefreshTokenIdUnitTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── adapters
        │               │   │       │   └── out
        │               │   │       │       ├── persistence
        │               │   │       │       │   ├── JpaRefreshTokenRepositoryUnitTest.java
        │               │   │       │       │   └── RefreshTokenEntityUnitTest.java
        │               │   │       │       └── LocalAuthenticableSubjectGatewayTest.java
        │               │   │       └── config
        │               │   │           └── JwtTokenServiceUnitTest.java
        │               │   └── user
        │               │       ├── application
        │               │       │   ├── dto
        │               │       │   │   ├── request
        │               │       │   │   │   └── RegisterUserRequestUnitTest.java
        │               │       │   │   └── response
        │               │       │   │       └── UserResponseUnitTest.java
        │               │       │   ├── exceptions
        │               │       │   │   ├── DuplicateEmailExceptionUnitTest.java
        │               │       │   │   └── UserNotFoundExceptionUnitTest.java
        │               │       │   ├── mappers
        │               │       │   │   └── UserMapperUnitTest.java
        │               │       │   ├── queries
        │               │       │   │   └── RepositoryNotificationRecipientsQueryTest.java
        │               │       │   ├── services
        │               │       │   │   └── RepositoryActorIdentityQueryTest.java
        │               │       │   └── usecases
        │               │       │       ├── GetAllUsersUseCaseTest.java
        │               │       │       ├── GetUserByIdUseCaseUnitTest.java
        │               │       │       ├── RegisterUserUseCaseUnitTest.java
        │               │       │       ├── UpdateUserEmailUseCaseTest.java
        │               │       │       ├── UpdateUserModulePermissionsUseCaseTest.java
        │               │       │       └── UpdateUserPasswordUseCaseTest.java
        │               │       ├── domain
        │               │       │   ├── entities
        │               │       │   │   └── UserUnitTest.java
        │               │       │   └── valueobjects
        │               │       │       ├── BusinessModuleUnitTest.java
        │               │       │       ├── EmailUnitTest.java
        │               │       │       ├── PasswordUnitTest.java
        │               │       │       ├── UserIdUnitTest.java
        │               │       │       ├── UserNameUnitTest.java
        │               │       │       └── UserRoleUnitTest.java
        │               │       └── infrastructure
        │               │           ├── adapters
        │               │           │   └── out
        │               │           │       └── persistence
        │               │           │           └── JpaUserRepositoryUnitTest.java
        │               │           ├── config
        │               │           │   └── BcryptPasswordEncoderUnitTest.java
        │               │           └── persistence
        │               │               └── UserEntityUnitTest.java.bak
        │               ├── cash_register
        │               │   ├── application
        │               │   │   ├── dto
        │               │   │   │   ├── CashRegisterMapperTest.java
        │               │   │   │   ├── CashRegisterMetricsTest.java
        │               │   │   │   ├── CashRegisterReportFilterTest.java
        │               │   │   │   └── DailyTotalsTest.java
        │               │   │   ├── services
        │               │   │   │   └── CashMovementServiceTest.java
        │               │   │   └── usecases
        │               │   │       ├── CashRegisterBoundaryTest.java
        │               │   │       ├── CloseRegisterUseCaseUnitTest.java
        │               │   │       ├── GenerateCashRegisterExcelUseCaseImplTest.java
        │               │   │       ├── GenerateCashRegisterRangeReportUseCaseTest.java
        │               │   │       ├── GenerateCashRegisterReportUseCaseTest.java
        │               │   │       ├── GetActiveRegisterUseCaseTest.java
        │               │   │       ├── GetRegisterHistoryUseCaseUnitTest.java
        │               │   │       ├── OpenRegisterUseCaseUnitTest.java
        │               │   │       └── RecordCashMovementUseCaseTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   ├── CashMovementUnitTest.java
        │               │   │   │   ├── CashRegisterDetailTest.java
        │               │   │   │   └── CashRegisterUnitTest.java
        │               │   │   └── valueobjects
        │               │   │       ├── RegisterIdUnitTest.java
        │               │   │       └── RegisterStatusUnitTest.java
        │               │   └── infrastructure
        │               │       ├── adapters
        │               │       │   ├── in
        │               │       │   │   └── controllers
        │               │       │   │       ├── CashRegisterControllerTest.java
        │               │       │   │       └── CashRegisterExportControllerTest.java
        │               │       │   └── out
        │               │       │       ├── excel
        │               │       │       │   ├── CashRegisterExcelExportServiceUnitTest.java
        │               │       │       │   ├── CashRegisterExcelGeneratorTest.java
        │               │       │       │   └── VerifyCellsTest.java
        │               │       │       ├── integration
        │               │       │       │   ├── LocalCreditNoteCashApiTest.java
        │               │       │       │   └── LocalDebitNoteCashApiTest.java
        │               │       │       ├── persistence
        │               │       │       │   ├── adapters
        │               │       │       │   │   └── CashRegisterRepositoryAdapterTest.java
        │               │       │       │   ├── queries
        │               │       │       │   │   └── JpaPaymentMethodUsageQueryTest.java
        │               │       │       │   └── CashRegisterRepositoryAdapterSessionTotalsTest.java
        │               │       │       └── report
        │               │       │           ├── CashRegisterRangeReportGeneratorTest.java
        │               │       │           └── CashRegisterSessionReportGeneratorTest.java
        │               │       └── config
        │               │           └── CashRegisterConfigTest.java
        │               ├── categories
        │               │   ├── application
        │               │   │   ├── exceptions
        │               │   │   │   └── CategoryNotFoundExceptionTest.java
        │               │   │   ├── mappers
        │               │   │   │   └── CategoryMapperTest.java
        │               │   │   └── usecases
        │               │   │       ├── CreateCategoryUseCaseTest.java
        │               │   │       ├── DeleteCategoryUseCaseTest.java
        │               │   │       ├── GetCategoryUseCaseTest.java
        │               │   │       ├── ListCategoriesUseCaseTest.java
        │               │   │       └── UpdateCategoryUseCaseTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   └── CategoryUnitTest.java
        │               │   │   └── repositories
        │               │   │       └── CatalogUsageBoundaryTest.java
        │               │   └── infrastructure
        │               │       ├── adapters
        │               │       │   ├── in
        │               │       │   │   └── controllers
        │               │       │   │       └── CategoryControllerTest.java
        │               │       │   └── out
        │               │       │       └── persistence
        │               │       │           └── adapters
        │               │       │               └── JpaCategoryRepositoryTest.java
        │               │       └── config
        │               │           └── CategoryConfigTest.java
        │               ├── config
        │               │   ├── DatabaseCleaner.java
        │               │   └── H2DialectWithoutEngineSupport.java
        │               ├── credit_notes
        │               │   ├── application
        │               │   │   ├── mappers
        │               │   │   │   └── CreditNoteMapperTest.java
        │               │   │   ├── services
        │               │   │   │   └── RepositoryCreditNoteLedgerQueryTest.java
        │               │   │   ├── usecases
        │               │   │   │   ├── CreateCreditNoteUseCaseTest.java
        │               │   │   │   ├── DeleteCreditNoteUseCaseTest.java
        │               │   │   │   ├── GetCreditNoteByIdUseCaseTest.java
        │               │   │   │   └── GetCreditNotesUseCaseTest.java
        │               │   │   └── CreditNoteModuleBoundaryTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   └── CreditNoteUnitTest.java
        │               │   │   └── valueobjects
        │               │   │       ├── CreditNoteIdUnitTest.java
        │               │   │       └── CreditNoteNumberUnitTest.java
        │               │   └── infrastructure
        │               │       └── adapters
        │               │           ├── in
        │               │           │   └── controllers
        │               │           │       └── CreditNoteControllerTest.java
        │               │           └── out
        │               │               ├── integration
        │               │               │   ├── LocalCreditNoteEffectsGatewayTest.java
        │               │               │   ├── LocalCreditNoteFinancialPositionGatewayTest.java
        │               │               │   └── LocalCreditNoteInvoiceGatewayTest.java
        │               │               └── persistence
        │               │                   └── adapters
        │               │                       └── CreditNoteRepositoryAdapterTest.java
        │               ├── customers
        │               │   ├── application
        │               │   │   ├── exceptions
        │               │   │   │   ├── CreditLimitExceededExceptionTest.java
        │               │   │   │   ├── CustomerNotFoundExceptionTest.java
        │               │   │   │   └── CustomerWarehouseAccessAlreadyExistsExceptionTest.java
        │               │   │   ├── mappers
        │               │   │   │   ├── BalanceMapperTest.java
        │               │   │   │   └── CustomerMapperTest.java
        │               │   │   ├── services
        │               │   │   │   ├── CustomerCreditContractTest.java
        │               │   │   │   ├── CustomerCreditEligibilityServiceTest.java
        │               │   │   │   ├── InvoiceBalanceConsumptionServiceTest.java
        │               │   │   │   ├── RepositoryCustomerCreditContextQueryTest.java
        │               │   │   │   └── ReturnBalanceMutationServiceTest.java
        │               │   │   └── usecases
        │               │   │       ├── credit
        │               │   │       │   ├── GetCustomerCreditStatusUseCaseTest.java
        │               │   │       │   └── GetOverdueInvoicesUseCaseTest.java
        │               │   │       ├── AddToCustomerBalanceUseCaseTest.java
        │               │   │       ├── ConsumeCustomerBalanceUseCaseTest.java
        │               │   │       ├── CreateCustomerCashBalanceOperationUseCaseTest.java
        │               │   │       ├── CustomerTestHelper.java
        │               │   │       ├── CustomerWarehouseBoundaryTest.java
        │               │   │       ├── DeleteCustomerUseCaseTest.java
        │               │   │       ├── GetCustomerBalanceUseCaseTest.java
        │               │   │       ├── GetCustomerUseCaseTest.java
        │               │   │       ├── GrantWarehouseAccessUseCaseTest.java
        │               │   │       ├── ListCustomerWarehousesUseCaseTest.java
        │               │   │       ├── ListCustomersUseCaseTest.java
        │               │   │       ├── RegisterCustomerUseCaseTest.java
        │               │   │       ├── RevokeWarehouseAccessUseCaseTest.java
        │               │   │       └── UpdateCustomerUseCaseTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   ├── BalanceTransactionTest.java
        │               │   │   │   ├── CustomerAddressUnitTest.java
        │               │   │   │   ├── CustomerBalanceTest.java
        │               │   │   │   ├── CustomerEmailUnitTest.java
        │               │   │   │   ├── CustomerPhoneUnitTest.java
        │               │   │   │   ├── CustomerUnitTest.java
        │               │   │   │   └── CustomerWarehouseAccessUnitTest.java
        │               │   │   ├── repositories
        │               │   │   │   └── CustomerBalanceBoundaryTest.java
        │               │   │   └── valueobjects
        │               │   │       ├── AddressIdUnitTest.java
        │               │   │       ├── CreditDaysUnitTest.java
        │               │   │       ├── CreditLimitUnitTest.java
        │               │   │       ├── CustomerAliasUnitTest.java
        │               │   │       ├── CustomerCodeUnitTest.java
        │               │   │       ├── CustomerIdUnitTest.java
        │               │   │       ├── CustomerTypeUnitTest.java
        │               │   │       ├── EmailAddressUnitTest.java
        │               │   │       ├── EmailIdUnitTest.java
        │               │   │       ├── LegalNameUnitTest.java
        │               │   │       ├── PaymentTypeUnitTest.java
        │               │   │       ├── PhoneIdUnitTest.java
        │               │   │       ├── PhoneNumberUnitTest.java
        │               │   │       └── RifUnitTest.java
        │               │   └── infrastructure
        │               │       └── adapters
        │               │           ├── in
        │               │           │   └── controllers
        │               │           │       ├── CustomerBalanceControllerTest.java
        │               │           │       ├── CustomerControllerTest.java
        │               │           │       ├── CustomerCreditControllerTest.java
        │               │           │       └── CustomerWarehouseAccessControllerTest.java
        │               │           └── out
        │               │               ├── integration
        │               │               │   ├── LocalCreditNoteBalanceApiTest.java
        │               │               │   ├── LocalCreditValidationGatewayTest.java
        │               │               │   ├── LocalCustomerActorGatewayTest.java
        │               │               │   ├── LocalCustomerCashGatewayTest.java
        │               │               │   └── LocalWarehouseDirectoryGatewayTest.java
        │               │               └── persistence
        │               │                   ├── adapters
        │               │                   │   ├── CustomerBalanceRepositoryAdapterTest.java
        │               │                   │   ├── JpaCustomerRepositoryTest.java
        │               │                   │   └── JpaCustomerWarehouseAccessRepositoryTest.java
        │               │                   └── mappers
        │               │                       ├── BalanceTransactionJpaMapperTest.java
        │               │                       └── CustomerBalanceJpaMapperTest.java
        │               ├── dashboard
        │               │   ├── application
        │               │   │   └── usecases
        │               │   │       ├── DashboardArchitectureTest.java
        │               │   │       └── DashboardQueryGatewayTest.java
        │               │   └── infrastructure
        │               │       └── adapters
        │               │           └── out
        │               │               └── persistence
        │               │                   └── queries
        │               │                       └── JdbcDashboardQueryAdapterTest.java
        │               ├── debit_notes
        │               │   ├── application
        │               │   │   ├── mappers
        │               │   │   │   └── DebitNoteMapperTest.java
        │               │   │   ├── services
        │               │   │   │   └── RepositoryDebitNoteLedgerQueryTest.java
        │               │   │   ├── usecases
        │               │   │   │   ├── CreateDebitNoteUseCaseTest.java
        │               │   │   │   ├── DeleteDebitNoteUseCaseTest.java
        │               │   │   │   ├── GetDebitNoteByIdUseCaseTest.java
        │               │   │   │   └── GetDebitNotesUseCaseTest.java
        │               │   │   └── DebitNoteModuleBoundaryTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   └── DebitNoteUnitTest.java
        │               │   │   └── valueobjects
        │               │   │       ├── DebitNoteIdUnitTest.java
        │               │   │       └── DebitNoteNumberUnitTest.java
        │               │   └── infrastructure
        │               │       └── adapters
        │               │           ├── in
        │               │           │   └── controllers
        │               │           │       └── DebitNoteControllerTest.java
        │               │           └── out
        │               │               ├── integration
        │               │               │   └── LocalDebitNoteInvoiceGatewayTest.java
        │               │               └── persistence
        │               │                   └── adapters
        │               │                       └── DebitNoteRepositoryAdapterTest.java
        │               ├── infrastructure
        │               │   └── config
        │               │       └── GlobalExceptionHandlerUnitTest.java
        │               ├── invoices
        │               │   ├── application
        │               │   │   ├── mappers
        │               │   │   │   └── InvoiceMapperTest.java
        │               │   │   ├── ports
        │               │   │   │   └── in
        │               │   │   │       └── InvoiceReturnCommandValidationTest.java
        │               │   │   ├── services
        │               │   │   │   ├── InvoiceAuthorizationBoundaryTest.java
        │               │   │   │   ├── InvoiceAuthorizationServiceTest.java
        │               │   │   │   ├── RepositoryInvoiceCreditLedgerQueryTest.java
        │               │   │   │   └── RepositoryInvoiceReturnAdjustmentsTest.java
        │               │   │   ├── usecases
        │               │   │   │   ├── CancelInvoiceUseCaseUnitTest.java
        │               │   │   │   ├── ConfirmInvoiceUseCaseTest.java
        │               │   │   │   ├── ConfirmInvoiceUseCaseUnitTest.java.bak
        │               │   │   │   ├── CreateInvoiceUseCaseTest.java
        │               │   │   │   ├── CreateInvoiceUseCaseUnitTest.java.bak
        │               │   │   │   ├── CreditConsumerBoundaryTest.java
        │               │   │   │   ├── DeleteInvoiceUseCaseTest.java
        │               │   │   │   ├── DeleteInvoiceUseCaseUnitTest.java.bak
        │               │   │   │   ├── DuplicateInvoiceUseCaseTest.java
        │               │   │   │   ├── DuplicateInvoiceUseCaseUnitTest.java.bak
        │               │   │   │   ├── GetInvoiceByIdUseCaseNotesTest.java
        │               │   │   │   ├── GetInvoiceByIdUseCaseUnitTest.java
        │               │   │   │   ├── GetInvoicesUseCaseTest.java
        │               │   │   │   ├── GetInvoicesUseCaseUnitTest.java.bak
        │               │   │   │   ├── InvoiceEditingBoundaryTest.java
        │               │   │   │   ├── InvoiceInventoryBoundaryTest.java
        │               │   │   │   ├── StockReadBoundaryTest.java
        │               │   │   │   ├── UpdateInvoiceUseCaseTest.java
        │               │   │   │   └── UpdateInvoiceUseCaseUnitTest.java.bak
        │               │   │   ├── InvoiceCreationBoundaryTest.java
        │               │   │   └── InvoiceEditingBoundaryTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   ├── InvoiceLineAllocationUnitTest.java
        │               │   │   │   ├── InvoiceLineUnitTest.java
        │               │   │   │   ├── InvoiceReturnAdjustmentTest.java
        │               │   │   │   ├── InvoiceReturnBoundaryTest.java
        │               │   │   │   └── InvoiceUnitTest.java
        │               │   │   ├── exceptions
        │               │   │   │   └── InvoiceNotFoundExceptionTest.java
        │               │   │   └── valueobjects
        │               │   │       ├── InvoiceIdUnitTest.java
        │               │   │       ├── InvoiceLineIdUnitTest.java
        │               │   │       ├── InvoiceNumberUnitTest.java
        │               │   │       ├── InvoiceStatusUnitTest.java
        │               │   │       └── PaymentTypeUnitTest.java
        │               │   └── infrastructure
        │               │       └── adapters
        │               │           ├── in
        │               │           │   └── controllers
        │               │           │       └── InvoiceControllerTest.java
        │               │           └── out
        │               │               ├── integration
        │               │               │   ├── CustomerCreditTermsContractTest.java
        │               │               │   ├── InvoiceAuthorizationWiringTest.java
        │               │               │   ├── LocalInventoryGatewayAdapterTest.java
        │               │               │   ├── LocalInvoiceFinancialPositionGatewayTest.java
        │               │               │   ├── LocalInvoiceNoteApiTest.java
        │               │               │   ├── LocalInvoiceNoteDetailsAdapterTest.java
        │               │               │   └── LocalInvoiceReturnDetailsAdapterTest.java
        │               │               ├── persistence
        │               │               │   ├── adapters
        │               │               │   │   └── InvoiceRepositoryAdapterTest.java
        │               │               │   └── entities
        │               │               │       └── InvoiceLineEntityUnitTest.java
        │               │               └── services
        │               │                   └── InvoiceStatusServiceImplTest.java
        │               ├── notifications
        │               │   ├── application
        │               │   │   └── services
        │               │   │       └── NotificationServiceTest.java
        │               │   └── infrastructure
        │               │       └── adapters
        │               │           └── out
        │               │               └── integration
        │               │                   └── AuthNotificationRecipientAdapterTest.java
        │               ├── orders
        │               │   ├── application
        │               │   │   ├── mappers
        │               │   │   │   └── OrderMapperTest.java
        │               │   │   └── usecases
        │               │   │       ├── CancelOrderUseCaseUnitTest.java
        │               │   │       ├── ConfirmOrderUseCaseUnitTest.java
        │               │   │       ├── ConvertToInvoiceUseCaseUnitTest.java
        │               │   │       ├── CreateOrderUseCaseUnitTest.java
        │               │   │       ├── DeleteOrderUseCaseUnitTest.java
        │               │   │       ├── GetOrderByIdUseCaseUnitTest.java
        │               │   │       ├── GetOrdersCategoryQueryBoundaryTest.java
        │               │   │       ├── GetOrdersUseCaseTest.java
        │               │   │       ├── GetOrdersUseCaseUnitTest.java.bak
        │               │   │       ├── GetProductsForOrderUseCaseUnitTest.java
        │               │   │       ├── OrderInvoiceConversionBoundaryTest.java
        │               │   │       └── UpdateOrderUseCaseUnitTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   ├── OrderLineUnitTest.java
        │               │   │   │   └── OrderUnitTest.java
        │               │   │   ├── exceptions
        │               │   │   │   ├── OrderCannotBeDeletedExceptionTest.java
        │               │   │   │   ├── OrderNotFoundExceptionTest.java
        │               │   │   │   └── OrderNotPendingExceptionTest.java
        │               │   │   └── valueobjects
        │               │   │       ├── OrderIdUnitTest.java
        │               │   │       ├── OrderNumberUnitTest.java
        │               │   │       └── OrderStatusUnitTest.java
        │               │   └── infrastructure
        │               │       ├── adapters
        │               │       │   ├── in
        │               │       │   │   └── controllers
        │               │       │   │       └── OrderControllerTest.java
        │               │       │   └── out
        │               │       │       ├── integration
        │               │       │       │   └── InvoiceCreationContractTest.java
        │               │       │       └── persistence
        │               │       │           ├── adapters
        │               │       │           │   └── OrderRepositoryAdapterTest.java
        │               │       │           ├── OrderCategorySnapshotMappingTest.java
        │               │       │           └── OrderHistoricalCategoryMySqlIT.java
        │               │       └── config
        │               │           └── OrderConfigTest.java
        │               ├── payment_methods
        │               │   ├── application
        │               │   │   ├── mappers
        │               │   │   │   └── PaymentMethodMapperTest.java
        │               │   │   └── usecases
        │               │   │       ├── CreatePaymentMethodUseCaseTest.java
        │               │   │       ├── DeletePaymentMethodUseCaseTest.java
        │               │   │       ├── GetPaymentMethodByIdUseCaseTest.java
        │               │   │       ├── ListPaymentMethodsUseCaseTest.java
        │               │   │       ├── PaymentMethodBoundaryTest.java
        │               │   │       ├── TogglePaymentMethodUseCaseTest.java
        │               │   │       └── UpdatePaymentMethodUseCaseTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   └── PaymentMethodUnitTest.java
        │               │   │   └── valueobjects
        │               │   │       └── PaymentMethodIdUnitTest.java
        │               │   └── infrastructure
        │               │       └── adapters
        │               │           ├── in
        │               │           │   └── controllers
        │               │           │       └── PaymentMethodControllerTest.java
        │               │           └── out
        │               │               ├── integration
        │               │               │   ├── LocalPaymentMethodUsageGatewayTest.java
        │               │               │   └── PaymentMethodUsageWiringTest.java
        │               │               └── persistence
        │               │                   └── adapters
        │               │                       └── PaymentMethodRepositoryAdapterTest.java
        │               ├── payments
        │               │   ├── application
        │               │   │   ├── dto
        │               │   │   │   └── request
        │               │   │   │       └── CreatePaymentRequestTest.java
        │               │   │   ├── mappers
        │               │   │   │   └── PaymentMapperTest.java
        │               │   │   ├── services
        │               │   │   │   ├── CashInvoiceSettlementServiceTest.java
        │               │   │   │   ├── CustomerCreditQueryBoundaryTest.java
        │               │   │   │   ├── CustomerCreditQueryServiceTest.java
        │               │   │   │   └── ReturnPaymentOperationsServiceTest.java
        │               │   │   ├── usecases
        │               │   │   │   ├── ApprovePaymentUseCaseTest.java
        │               │   │   │   ├── CreatePaymentUseCaseUnitTest.java
        │               │   │   │   ├── DeletePaymentUseCaseTest.java
        │               │   │   │   ├── GetAllPaymentsUseCaseTest.java
        │               │   │   │   ├── GetPaymentByIdUseCaseUnitTest.java
        │               │   │   │   ├── PayAllCustomerDebtUseCaseTest.java
        │               │   │   │   ├── PayInvoiceUseCaseTest.java
        │               │   │   │   ├── PayPartialDebtUseCaseTest.java
        │               │   │   │   ├── RejectPaymentUseCaseTest.java
        │               │   │   │   └── UpdatePaymentUseCaseTest.java
        │               │   │   └── PaymentModuleBoundaryTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   ├── PaymentAllocationTest.java
        │               │   │   │   └── PaymentUnitTest.java
        │               │   │   ├── services
        │               │   │   │   ├── CustomerCreditPolicyTest.java
        │               │   │   │   └── PaymentAllocationServiceTest.java
        │               │   │   └── valueobjects
        │               │   │       ├── InvoiceRemainingBalanceUnitTest.java
        │               │   │       ├── PaymentIdUnitTest.java
        │               │   │       ├── PaymentNumberUnitTest.java
        │               │   │       ├── PaymentSourceTest.java
        │               │   │       └── PaymentStatusTest.java
        │               │   └── infrastructure
        │               │       ├── adapters
        │               │       │   ├── in
        │               │       │   │   └── controllers
        │               │       │   │       └── PaymentControllerTest.java
        │               │       │   └── out
        │               │       │       ├── integration
        │               │       │       │   ├── CreditLedgerWiringTest.java
        │               │       │       │   ├── LocalCreditLedgerGatewaysTest.java
        │               │       │       │   ├── LocalPaymentCashRegisterGatewayTest.java
        │               │       │       │   ├── LocalPaymentInvoiceGatewayTest.java
        │               │       │       │   └── RepositoryInvoicePaymentTotalQueryTest.java
        │               │       │       └── persistence
        │               │       │           ├── adapters
        │               │       │           │   ├── PaymentAllocationRepositoryAdapterTest.java
        │               │       │           │   └── PaymentRepositoryAdapterTest.java
        │               │       │           └── queries
        │               │       │               └── JpaPaymentMethodUsageQueryTest.java
        │               │       └── config
        │               │           └── PaymentConfigTest.java
        │               ├── price_types
        │               │   ├── application
        │               │   │   ├── exceptions
        │               │   │   │   └── DuplicatePriceTypeNameExceptionTest.java
        │               │   │   └── usecases
        │               │   │       ├── CreatePriceTypeUseCaseUnitTest.java
        │               │   │       ├── DeletePriceTypeUseCaseUnitTest.java
        │               │   │       ├── GetPriceTypeUseCaseUnitTest.java
        │               │   │       ├── ListPriceTypesUseCaseTest.java
        │               │   │       ├── ListPriceTypesUseCaseUnitTest.java.bak
        │               │   │       └── UpdatePriceTypeUseCaseUnitTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   └── PriceTypeUnitTest.java
        │               │   │   └── valueobjects
        │               │   │       └── ProductPriceUnitTest.java
        │               │   └── infrastructure
        │               │       └── adapters
        │               │           ├── in
        │               │           │   └── controllers
        │               │           │       └── PriceTypeControllerTest.java
        │               │           └── out
        │               │               └── persistence
        │               │                   └── adapters
        │               │                       └── JpaPriceTypeRepositoryTest.java
        │               ├── products
        │               │   ├── application
        │               │   │   ├── exceptions
        │               │   │   │   ├── CompositeProductCannotBeComponentExceptionTest.java
        │               │   │   │   ├── DuplicateProductCodeExceptionTest.java
        │               │   │   │   ├── DuplicateProductComponentExceptionTest.java
        │               │   │   │   └── ProductNotFoundExceptionTest.java
        │               │   │   ├── mappers
        │               │   │   │   └── ProductMapperTest.java
        │               │   │   └── usecases
        │               │   │       ├── CreateProductUseCaseTest.java
        │               │   │       ├── DeleteProductUseCaseTest.java
        │               │   │       ├── GetProductStockUseCaseTest.java
        │               │   │       ├── GetProductUseCaseTest.java
        │               │   │       ├── GetProductsForTransactionUseCaseTest.java
        │               │   │       ├── ListProductsUseCaseTest.java
        │               │   │       ├── ProductCatalogBoundaryTest.java
        │               │   │       ├── ProductStockBoundaryTest.java
        │               │   │       ├── RemoveProductPhotoUseCaseTest.java
        │               │   │       ├── UpdateProductUseCaseTest.java
        │               │   │       └── UploadProductPhotoUseCaseTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   ├── ProductComponentUnitTest.java
        │               │   │   │   └── ProductUnitTest.java
        │               │   │   └── valueobjects
        │               │   │       └── ProductPriceUnitTest.java
        │               │   └── infrastructure
        │               │       └── adapters
        │               │           ├── in
        │               │           │   └── controllers
        │               │           │       └── ProductControllerTest.java
        │               │           └── out
        │               │               ├── integration
        │               │               │   ├── LocalProductFileStorageGatewayAdapterTest.java
        │               │               │   ├── ProductCatalogGatewayIntegrationTest.java
        │               │               │   └── ProductStockGatewayIntegrationTest.java
        │               │               └── persistence
        │               │                   ├── adapters
        │               │                   │   └── JpaProductRepositoryTest.java
        │               │                   ├── queries
        │               │                   │   └── JpaProductUsageQueriesTest.java
        │               │                   ├── repositories
        │               │                   │   ├── ProductPriceMappingTest.java
        │               │                   │   ├── ProductPricePersistenceBoundaryTest.java
        │               │                   │   ├── ProductReferenceGatewayTest.java
        │               │                   │   ├── ProductReferenceQueryBoundaryTest.java
        │               │                   │   ├── ProductStockFilterTest.java
        │               │                   │   └── ProductStockQueryBoundaryTest.java
        │               │                   └── ProductPersistenceQueriesMySqlIT.java
        │               ├── purchases
        │               │   ├── application
        │               │   │   ├── queries
        │               │   │   │   └── RepositorySupplierPurchaseUsageQueryTest.java
        │               │   │   ├── services
        │               │   │   │   └── PurchaseDetailResponseAssemblerTest.java
        │               │   │   └── usecases
        │               │   │       ├── CancelPurchaseUseCaseUnitTest.java
        │               │   │       ├── ConfirmPurchaseUseCaseUnitTest.java
        │               │   │       ├── CreatePurchaseUseCaseUnitTest.java
        │               │   │       ├── GetProductsForPurchaseUseCaseTest.java
        │               │   │       ├── PurchaseIdentityCatalogBoundaryTest.java
        │               │   │       ├── PurchaseProductCatalogContractTest.java
        │               │   │       ├── PurchaseWarehouseReadBoundaryTest.java
        │               │   │       └── UpdatePurchaseCatalogTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   ├── PurchaseLineAllocationUnitTest.java
        │               │   │   │   ├── PurchaseLineUnitTest.java
        │               │   │   │   └── PurchaseUnitTest.java
        │               │   │   └── valueobjects
        │               │   │       ├── PurchaseIdUnitTest.java
        │               │   │       ├── PurchaseNumberUnitTest.java
        │               │   │       └── PurchaseStatusUnitTest.java
        │               │   └── infrastructure
        │               │       └── adapters
        │               │           ├── in
        │               │           │   └── controllers
        │               │           │       └── PurchaseControllerTest.java
        │               │           └── out
        │               │               ├── integration
        │               │               │   └── LocalPurchaseSupplierGatewayAdapterTest.java
        │               │               └── persistence
        │               │                   └── repositories
        │               │                       └── PurchaseRepositoryIntegrationTest.java
        │               ├── quotes
        │               │   ├── application
        │               │   │   ├── mappers
        │               │   │   │   └── QuoteMapperTest.java
        │               │   │   ├── usecases
        │               │   │   │   ├── AssignQuoteWarehousesUseCaseTest.java
        │               │   │   │   ├── CancelQuoteUseCaseTest.java
        │               │   │   │   ├── ConvertQuoteToInvoiceUseCaseTest.java
        │               │   │   │   ├── ConvertQuoteToOrderUseCaseTest.java
        │               │   │   │   ├── CreateQuoteUseCaseTest.java
        │               │   │   │   ├── DeleteQuoteUseCaseTest.java
        │               │   │   │   ├── GetProductsForQuoteUseCaseTest.java
        │               │   │   │   ├── GetQuoteByIdUseCaseTest.java
        │               │   │   │   ├── GetQuotesUseCaseTest.java
        │               │   │   │   ├── GetWarehousesForQuoteUseCaseTest.java
        │               │   │   │   ├── QuoteCancellationBoundaryTest.java
        │               │   │   │   ├── QuoteInvoiceConversionBoundaryTest.java
        │               │   │   │   ├── QuoteOrderConversionBoundaryTest.java
        │               │   │   │   └── UpdateQuoteUseCaseTest.java
        │               │   │   └── QuoteModuleBoundaryTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   ├── QuoteAllocationTest.java
        │               │   │   │   ├── QuoteLineTest.java
        │               │   │   │   └── QuoteTest.java
        │               │   │   ├── InvalidQuoteAllocationExceptionTest.java
        │               │   │   ├── InvalidQuoteLineExceptionTest.java
        │               │   │   ├── QuoteAllocationIdTest.java
        │               │   │   ├── QuoteLineIdTest.java
        │               │   │   ├── QuoteNotFoundExceptionTest.java
        │               │   │   ├── QuoteNumberTest.java
        │               │   │   ├── QuoteStateExceptionTest.java
        │               │   │   └── QuoteStatusTest.java
        │               │   └── infrastructure
        │               │       └── adapters
        │               │           ├── in
        │               │           │   └── controllers
        │               │           │       └── QuoteControllerTest.java
        │               │           └── out
        │               │               ├── integration
        │               │               │   └── LocalOrderCreationGatewayTest.java
        │               │               └── persistence
        │               │                   ├── adapters
        │               │                   │   └── JpaQuoteRepositoryAdapterTest.java
        │               │                   └── QuoteCategorySnapshotMappingTest.java
        │               ├── receivables
        │               │   ├── application
        │               │   │   ├── dto
        │               │   │   │   └── response
        │               │   │   │       └── ReceivablesSummaryResponseTest.java
        │               │   │   └── usecases
        │               │   │       ├── GetCustomerDebtUseCaseTest.java
        │               │   │       ├── GetCustomerInvoicesUseCaseTest.java
        │               │   │       ├── GetPaymentHistoryUseCaseTest.java
        │               │   │       ├── GetReceivablesSummaryUseCaseTest.java
        │               │   │       ├── ReceivablesBoundaryTest.java
        │               │   │       └── SearchCustomerDebtUseCaseTest.java
        │               │   └── infrastructure
        │               │       ├── adapters
        │               │       │   ├── in
        │               │       │   │   └── controllers
        │               │       │   │       └── ReceivablesControllerTest.java
        │               │       │   └── out
        │               │       │       ├── integration
        │               │       │       │   └── LocalReceivablesCustomerProfileGatewayAdapterTest.java
        │               │       │       └── persistence
        │               │       │           └── adapters
        │               │       │               └── ReceivablesRepositoryAdapterTest.java
        │               │       └── config
        │               │           └── ReceivablesConfigTest.java
        │               ├── reports
        │               │   ├── finance
        │               │   │   ├── application
        │               │   │   │   ├── dto
        │               │   │   │   │   ├── AccountsReceivableReportDataTest.java
        │               │   │   │   │   ├── CollectionsReportDataTest.java
        │               │   │   │   │   └── PaymentHistoryReportDataTest.java
        │               │   │   │   ├── ports
        │               │   │   │   │   └── out
        │               │   │   │   │       └── PaymentPdfPortTest.java
        │               │   │   │   ├── usecases
        │               │   │   │   │   ├── GenerateAccountsReceivableReportPdfUseCaseImplTest.java
        │               │   │   │   │   ├── GenerateCollectionsReportPdfUseCaseImplTest.java
        │               │   │   │   │   ├── GeneratePaymentHistoryReportPdfUseCaseImplTest.java
        │               │   │   │   │   └── GeneratePaymentPdfUseCaseImplTest.java
        │               │   │   │   ├── AccountsReceivableReportsBoundaryTest.java
        │               │   │   │   └── PaymentReportsBoundaryTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── adapters
        │               │   │       │   ├── in
        │               │   │       │   │   └── controllers
        │               │   │       │   │       └── FinancesReportControllerTest.java
        │               │   │       │   └── out
        │               │   │       │       ├── integration
        │               │   │       │       │   ├── PaymentDocumentQueryAdapterTest.java
        │               │   │       │       │   └── ReceivablesReportQueryAdapterTest.java
        │               │   │       │       └── pdf
        │               │   │       │           ├── AccountsReceivablePdfContentTest.java
        │               │   │       │           └── PaymentPdfContentTest.java
        │               │   │       └── config
        │               │   │           └── FinancesReportConfigTest.java
        │               │   ├── infrastructure
        │               │   │   └── adapters
        │               │   │       └── out
        │               │   │           └── pdf
        │               │   │               └── PdfStatusColorsTest.java
        │               │   ├── inventory
        │               │   │   ├── application
        │               │   │   │   ├── dto
        │               │   │   │   │   ├── query
        │               │   │   │   │   │   ├── CatalogPdfQueryTest.java
        │               │   │   │   │   │   └── CatalogReportQueryTest.java
        │               │   │   │   │   └── response
        │               │   │   │   │       ├── CatalogReportResponseTest.java
        │               │   │   │   │       └── CommercialCatalogResponseTest.java
        │               │   │   │   ├── ports
        │               │   │   │   │   └── out
        │               │   │   │   │       └── CatalogPdfPortTest.java
        │               │   │   │   ├── usecases
        │               │   │   │   │   ├── GenerateCatalogPdfUseCaseImplTest.java
        │               │   │   │   │   ├── GenerateCommercialCatalogPdfUseCaseImplTest.java
        │               │   │   │   │   └── GetCatalogReportUseCaseImplTest.java
        │               │   │   │   └── CommercialCatalogBoundaryTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── adapters
        │               │   │       │   ├── in
        │               │   │       │   │   └── controllers
        │               │   │       │   │       ├── CatalogReportControllerTest.java
        │               │   │       │   │       └── CommercialCatalogControllerTest.java
        │               │   │       │   └── out
        │               │   │       │       ├── integration
        │               │   │       │       │   └── FileStorageMediaAdapterTest.java
        │               │   │       │       ├── pdf
        │               │   │       │       │   └── CommercialCatalogPdfContentTest.java
        │               │   │       │       └── persistence
        │               │   │       │           ├── adapters
        │               │   │       │           │   └── CatalogReportRepositoryAdapterTest.java
        │               │   │       │           └── projections
        │               │   │       │               └── CatalogReportProjectionTest.java
        │               │   │       └── config
        │               │   │           └── InventoryReportConfigTest.java
        │               │   ├── invoices
        │               │   │   ├── application
        │               │   │   │   ├── usecases
        │               │   │   │   │   ├── GenerateInvoicePdfUseCaseImplTest.java
        │               │   │   │   │   └── GenerateInvoicesReportPdfUseCaseImplTest.java
        │               │   │   │   └── InvoiceReportsBoundaryTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── adapters
        │               │   │       │   ├── in
        │               │   │       │   │   └── controllers
        │               │   │       │   │       └── InvoicesReportControllerTest.java
        │               │   │       │   └── out
        │               │   │       │       ├── integration
        │               │   │       │       │   └── InvoiceDocumentQueryAdapterTest.java
        │               │   │       │       └── pdf
        │               │   │       │           └── InvoicePdfContentTest.java
        │               │   │       └── config
        │               │   │           └── InvoicesReportConfigTest.java
        │               │   ├── movements
        │               │   │   ├── application
        │               │   │   │   ├── dto
        │               │   │   │   │   ├── query
        │               │   │   │   │   │   └── MovementsPdfQueryTest.java
        │               │   │   │   │   └── response
        │               │   │   │   │       └── MovementReportResponseTest.java
        │               │   │   │   ├── ports
        │               │   │   │   │   └── out
        │               │   │   │   │       └── MovementsPdfPortTest.java
        │               │   │   │   └── usecases
        │               │   │   │       ├── GenerateMovementsExcelUseCaseImplTest.java
        │               │   │   │       └── GenerateMovementsPdfUseCaseImplTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── adapters
        │               │   │       │   ├── in
        │               │   │       │   │   └── controllers
        │               │   │       │   │       └── MovementsReportControllerTest.java
        │               │   │       │   └── out
        │               │   │       │       └── persistence
        │               │   │       │           ├── adapters
        │               │   │       │           │   └── MovementsReportRepositoryAdapterTest.java
        │               │   │       │           └── projections
        │               │   │       │               └── MovementsReportProjectionTest.java
        │               │   │       └── config
        │               │   │           └── MovementsReportConfigTest.java
        │               │   ├── orders
        │               │   │   ├── application
        │               │   │   │   ├── usecases
        │               │   │   │   │   ├── GenerateOrderPdfUseCaseImplTest.java
        │               │   │   │   │   └── GenerateOrdersReportPdfUseCaseImplTest.java
        │               │   │   │   └── OrderReportsBoundaryTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── adapters
        │               │   │       │   ├── in
        │               │   │       │   │   └── controllers
        │               │   │       │   │       └── OrdersReportControllerTest.java
        │               │   │       │   └── out
        │               │   │       │       ├── integration
        │               │   │       │       │   └── OrderDocumentQueryAdapterTest.java
        │               │   │       │       └── pdf
        │               │   │       │           └── OrderPdfContentTest.java
        │               │   │       └── config
        │               │   │           └── OrdersReportConfigTest.java
        │               │   ├── purchases
        │               │   │   ├── application
        │               │   │   │   ├── usecases
        │               │   │   │   │   ├── GeneratePurchasePdfUseCaseImplTest.java
        │               │   │   │   │   └── GeneratePurchasesReportPdfUseCaseImplTest.java
        │               │   │   │   └── PurchaseReportsBoundaryTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── adapters
        │               │   │       │   ├── in
        │               │   │       │   │   └── controllers
        │               │   │       │   │       └── PurchasesReportControllerTest.java
        │               │   │       │   └── out
        │               │   │       │       ├── integration
        │               │   │       │       │   └── PurchaseDocumentQueryAdapterTest.java
        │               │   │       │       └── pdf
        │               │   │       │           └── PurchasePdfContentTest.java
        │               │   │       └── config
        │               │   │           └── PurchasesReportConfigTest.java
        │               │   ├── quotes
        │               │   │   ├── application
        │               │   │   │   ├── usecases
        │               │   │   │   │   ├── GenerateQuotePdfUseCaseImplTest.java
        │               │   │   │   │   └── GenerateQuotesReportPdfUseCaseImplTest.java
        │               │   │   │   └── QuoteReportsBoundaryTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── adapters
        │               │   │       │   ├── in
        │               │   │       │   │   └── controllers
        │               │   │       │   │       └── QuotesReportControllerTest.java
        │               │   │       │   └── out
        │               │   │       │       ├── integration
        │               │   │       │       │   └── QuoteDocumentQueryAdapterTest.java
        │               │   │       │       └── pdf
        │               │   │       │           └── QuotePdfContentTest.java
        │               │   │       └── config
        │               │   │           └── QuotesReportConfigTest.java
        │               │   └── stock
        │               │       ├── application
        │               │       │   ├── dto
        │               │       │   │   ├── query
        │               │       │   │   │   └── StockPdfQueryTest.java
        │               │       │   │   └── response
        │               │       │   │       └── StockReportResponseTest.java
        │               │       │   ├── ports
        │               │       │   │   └── out
        │               │       │   │       └── StockPdfPortTest.java
        │               │       │   └── usecases
        │               │       │       ├── GenerateStockExcelUseCaseImplTest.java
        │               │       │       └── GenerateStockPdfUseCaseImplTest.java
        │               │       └── infrastructure
        │               │           ├── adapters
        │               │           │   ├── in
        │               │           │   │   └── controllers
        │               │           │   │       └── StockReportControllerTest.java
        │               │           │   └── out
        │               │           │       └── persistence
        │               │           │           ├── adapters
        │               │           │           │   └── StockReportRepositoryAdapterTest.java
        │               │           │           └── projections
        │               │           │               └── StockReportProjectionTest.java
        │               │           └── config
        │               │               └── StockReportConfigTest.java
        │               ├── returns
        │               │   ├── application
        │               │   │   ├── mappers
        │               │   │   │   └── ReturnMapperTest.java
        │               │   │   ├── services
        │               │   │   │   └── ReturnResponseAssemblerTest.java
        │               │   │   └── usecases
        │               │   │       ├── CreateReturnUseCaseTest.java
        │               │   │       ├── DeleteReturnUseCaseTest.java
        │               │   │       ├── GetAllReturnsUseCaseTest.java
        │               │   │       ├── GetReturnByIdUseCaseTest.java
        │               │   │       ├── ReturnFinancialBoundaryTest.java
        │               │   │       ├── ReturnFinancialFlowsTest.java
        │               │   │       └── ReturnInventoryBoundaryTest.java
        │               │   ├── domain
        │               │   │   └── entities
        │               │   │       ├── CancelledAllocationUnitTest.java
        │               │   │       ├── ReturnIdUnitTest.java
        │               │   │       ├── ReturnInvoiceReferenceBoundaryTest.java
        │               │   │       ├── ReturnLineAllocationUnitTest.java
        │               │   │       ├── ReturnLineUnitTest.java
        │               │   │       ├── ReturnNumberUnitTest.java
        │               │   │       └── ReturnUnitTest.java
        │               │   └── infrastructure
        │               │       ├── adapters
        │               │       │   ├── in
        │               │       │   │   └── controllers
        │               │       │   │       └── ReturnControllerTest.java
        │               │       │   └── out
        │               │       │       ├── integration
        │               │       │       │   ├── InvoiceReturnAdjustmentAdapterTest.java
        │               │       │       │   ├── LocalReturnInventoryGatewayAdapterTest.java
        │               │       │       │   └── ReturnFinancialWiringTest.java
        │               │       │       └── persistence
        │               │       │           └── adapters
        │               │       │               └── JpaReturnRepositoryTest.java
        │               │       ├── config
        │               │       │   └── ReturnConfigTest.java
        │               │       └── ReturnsFinancialRollbackMySqlIT.java
        │               ├── shared
        │               │   ├── dto
        │               │   │   └── PageResponseBoundaryTest.java
        │               │   ├── infrastructure
        │               │   │   └── pagination
        │               │   │       └── PageResponseMapperTest.java
        │               │   └── storage
        │               │       └── domain
        │               │           ├── StorageExceptionTest.java
        │               │           └── StorageServiceTest.java
        │               ├── suppliers
        │               │   ├── application
        │               │   │   └── usecases
        │               │   │       └── SupplierPurchaseUsageTest.java
        │               │   ├── domain
        │               │   │   ├── entities
        │               │   │   │   └── SupplierUnitTest.java
        │               │   │   └── valueobjects
        │               │   │       ├── EmailAddressUnitTest.java
        │               │   │       ├── LegalNameUnitTest.java
        │               │   │       ├── PhoneNumberUnitTest.java
        │               │   │       ├── RifUnitTest.java
        │               │   │       ├── SupplierAliasUnitTest.java
        │               │   │       ├── SupplierCodeUnitTest.java
        │               │   │       └── SupplierIdUnitTest.java
        │               │   └── infrastructure
        │               │       └── adapters
        │               │           ├── in
        │               │           │   └── controllers
        │               │           │       └── SupplierControllerTest.java
        │               │           └── out
        │               │               ├── integration
        │               │               │   ├── LocalPurchaseUsageGatewayAdapterTest.java
        │               │               │   └── SupplierPurchaseDirectoryAdapterTest.java
        │               │               └── persistence
        │               │                   └── adapters
        │               │                       └── JpaSupplierRepositoryTest.java
        │               ├── warehouses
        │               │   ├── application
        │               │   │   ├── exceptions
        │               │   │   │   ├── DuplicateWarehouseCodeExceptionTest.java
        │               │   │   │   ├── InsufficientStockExceptionTest.java
        │               │   │   │   ├── ProductNotInWarehouseExceptionTest.java
        │               │   │   │   ├── WarehouseNotActiveExceptionTest.java
        │               │   │   │   └── WarehouseNotFoundExceptionTest.java
        │               │   │   ├── ports
        │               │   │   │   └── in
        │               │   │   │       └── InvoiceStockCommandTest.java
        │               │   │   ├── services
        │               │   │   │   ├── InvoiceInventoryServiceTest.java
        │               │   │   │   ├── RepositoryWarehouseDirectoryQueryTest.java
        │               │   │   │   ├── ReturnInventoryServiceTest.java
        │               │   │   │   └── StockAvailabilityQueryServiceTest.java
        │               │   │   └── usecases
        │               │   │       ├── CreateWarehouseUseCaseTest.java
        │               │   │       ├── DeleteWarehouseUseCaseTest.java
        │               │   │       ├── GetStockMovementsUseCaseTest.java
        │               │   │       ├── GetWarehouseStockUseCaseTest.java
        │               │   │       ├── GetWarehouseUseCaseTest.java
        │               │   │       ├── ListWarehousesUseCaseTest.java
        │               │   │       ├── ManualStockCatalogBoundaryTest.java
        │               │   │       ├── StockAdjustmentUseCaseTest.java
        │               │   │       ├── StockEntryUseCaseTest.java
        │               │   │       ├── StockExitUseCaseTest.java
        │               │   │       ├── TransferStockUseCaseTest.java
        │               │   │       └── UpdateWarehouseUseCaseTest.java
        │               │   ├── domain
        │               │   │   └── entities
        │               │   │       ├── MovementTypeUnitTest.java
        │               │   │       ├── ReferenceTypeTest.java
        │               │   │       ├── StockMovementUnitTest.java
        │               │   │       ├── WarehouseStockTest.java
        │               │   │       └── WarehouseUnitTest.java
        │               │   └── infrastructure
        │               │       ├── adapters
        │               │       │   ├── in
        │               │       │   │   └── controllers
        │               │       │   │       ├── StockMovementControllerTest.java
        │               │       │   │       ├── WarehouseControllerTest.java
        │               │       │   │       └── WarehouseStockControllerTest.java
        │               │       │   └── out
        │               │       │       ├── integration
        │               │       │       │   ├── LocalStockCatalogGatewayAdapterTest.java
        │               │       │       │   └── StockAvailabilityConsumerAdaptersTest.java
        │               │       │       └── persistence
        │               │       │           └── adapters
        │               │       │               ├── JpaStockMovementRepositoryTest.java
        │               │       │               ├── JpaWarehouseRepositoryTest.java
        │               │       │               └── JpaWarehouseStockRepositoryTest.java
        │               │       ├── InvoiceInventoryConcurrencyIT.java
        │               │       ├── ManualStockConcurrencyIT.java
        │               │       ├── StockDeductionConcurrencyIT.java
        │               │       └── WarehouseStockConcurrencyIT.java
        │               ├── CashlyApplicationTests.java
        │               ├── TestCashlyApplication.java
        │               ├── TestJpaConfig.java
        │               └── TestcontainersConfiguration.java
        └── resources
            ├── application-test.properties
            └── application-web-test.properties
```

## Production resources unchanged

```text
└── src
    └── main
        └── resources
            ├── db
            │   └── migration
            │       ├── V10__create_order_line_allocations.sql
            │       ├── V11__create_invoices_tables.sql
            │       ├── V12__create_invoice_line_allocations.sql
            │       ├── V13__create_quotes_tables.sql
            │       ├── V14__create_payments_tables.sql
            │       ├── V15__create_returns_tables.sql
            │       ├── V16__create_credit_notes_table.sql
            │       ├── V17__create_debit_notes_table.sql
            │       ├── V18__create_cash_closures_tables.sql
            │       ├── V19__create_suppliers_tables.sql
            │       ├── V1__create_users_table.sql
            │       ├── V20__create_purchases_tables.sql
            │       ├── V21__create_return_cancelled_allocations.sql
            │       ├── V22__create_notifications_table.sql
            │       ├── V23__create_cash_register_movements.sql
            │       ├── V24__create_invoice_source_links.sql
            │       ├── V25__create_sales_line_category_snapshots.sql
            │       ├── V2__create_refresh_tokens_table.sql
            │       ├── V3__create_price_types.sql
            │       ├── V4__create_customers_table.sql
            │       ├── V5__create_categories.sql
            │       ├── V6__create_products.sql
            │       ├── V7__create_warehouses.sql
            │       ├── V8__create_product_prices.sql
            │       └── V9__create_orders_tables.sql
            ├── pdf
            │   └── cashly-logo.png
            ├── application-dev.properties
            ├── application-production.properties
            ├── application.properties
            └── data.sql
```
