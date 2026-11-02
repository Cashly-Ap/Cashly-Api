# Complete after backend trees

This is the complete target inventory of the baseline source/test/resource files. Package moves are implemented; additional verification tests are listed in the integrity evidence. Overview is a reserved extension point and introduces no new source in this structural change.

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
                            │   │   │   └── response
                            │   │   │       ├── AuthenticatedEntityResponse.java
                            │   │   │       └── AuthenticationResponse.java
                            │   │   ├── exceptions
                            │   │   │   └── InvalidCredentialsException.java
                            │   │   ├── login
                            │   │   │   ├── LoginRequest.java
                            │   │   │   └── LoginUserUseCase.java
                            │   │   └── mappers
                            │   │       └── AuthenticationMapper.java
                            │   ├── domain
                            │   │   ├── services
                            │   │   │   ├── AuthenticableResolver.java
                            │   │   │   └── AuthenticationDomainService.java
                            │   │   ├── Authenticable.java
                            │   │   └── AuthenticableType.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   ├── AuthenticationConfig.java
                            │       │   └── SecurityConfig.java
                            │       ├── integration
                            │       │   └── UserAuthenticableAdapter.java
                            │       ├── security
                            │       │   ├── JwtAuthenticationEntryPoint.java
                            │       │   └── JwtAuthenticationFilter.java
                            │       ├── services
                            │       │   └── ChainedAuthenticableResolver.java
                            │       └── web
                            │           └── AuthController.java
                            ├── token
                            │   ├── application
                            │   │   ├── exceptions
                            │   │   │   ├── InvalidTokenException.java
                            │   │   │   └── TokenExpiredException.java
                            │   │   ├── logout
                            │   │   │   ├── LogoutRequest.java
                            │   │   │   └── LogoutUserUseCase.java
                            │   │   ├── ports
                            │   │   │   └── out
                            │   │   │       └── AuthenticableSubjectGateway.java
                            │   │   └── refresh
                            │   │       ├── RefreshTokenRequest.java
                            │   │       └── RefreshTokenUseCase.java
                            │   ├── domain
                            │   │   ├── services
                            │   │   │   └── TokenService.java
                            │   │   ├── valueobjects
                            │   │   │   └── RefreshTokenId.java
                            │   │   ├── RefreshToken.java
                            │   │   └── RefreshTokenRepository.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   └── JwtTokenService.java
                            │       ├── integration
                            │       │   └── LocalAuthenticableSubjectGateway.java
                            │       └── persistence
                            │           ├── adapters
                            │           │   └── JpaRefreshTokenRepository.java
                            │           ├── entities
                            │           │   └── RefreshTokenEntity.java
                            │           └── repositories
                            │               └── SpringDataRefreshTokenRepository.java
                            └── user
                                ├── application
                                │   ├── delete
                                │   │   └── DeleteUserUseCase.java
                                │   ├── dto
                                │   │   └── response
                                │   │       └── UserResponse.java
                                │   ├── email
                                │   │   ├── UpdateUserEmailRequest.java
                                │   │   └── UpdateUserEmailUseCase.java
                                │   ├── exceptions
                                │   │   ├── DuplicateEmailException.java
                                │   │   ├── UserHasAssociatedRecordsException.java
                                │   │   └── UserNotFoundException.java
                                │   ├── mappers
                                │   │   └── UserMapper.java
                                │   ├── password
                                │   │   ├── UpdateUserPasswordRequest.java
                                │   │   └── UpdateUserPasswordUseCase.java
                                │   ├── permissions
                                │   │   ├── UpdateUserModulePermissionsRequest.java
                                │   │   └── UpdateUserModulePermissionsUseCase.java
                                │   ├── ports
                                │   │   └── in
                                │   │       ├── ActorIdentityQuery.java
                                │   │       └── NotificationRecipientsQuery.java
                                │   ├── queries
                                │   │   ├── GetAllUsersUseCase.java
                                │   │   ├── GetUserByIdUseCase.java
                                │   │   ├── RepositoryActorIdentityQuery.java
                                │   │   └── RepositoryNotificationRecipientsQuery.java
                                │   ├── register
                                │   │   ├── RegisterUserRequest.java
                                │   │   └── RegisterUserUseCase.java
                                │   └── update
                                │       ├── UpdateUserRequest.java
                                │       └── UpdateUserUseCase.java
                                ├── domain
                                │   ├── services
                                │   │   └── PasswordEncoder.java
                                │   ├── valueobjects
                                │   │   ├── BusinessModule.java
                                │   │   ├── Email.java
                                │   │   ├── Password.java
                                │   │   ├── UserId.java
                                │   │   ├── UserName.java
                                │   │   └── UserRole.java
                                │   ├── User.java
                                │   └── UserRepository.java
                                └── infrastructure
                                    ├── config
                                    │   ├── ActorIdentityQueryConfig.java
                                    │   ├── BcryptPasswordEncoder.java
                                    │   └── UserQueryConfig.java
                                    ├── persistence
                                    │   ├── adapters
                                    │   │   └── JpaUserRepository.java
                                    │   ├── entities
                                    │   │   ├── UserEntity.java
                                    │   │   └── UserModulePermissionEntity.java
                                    │   └── repositories
                                    │       └── SpringDataUserRepository.java
                                    └── web
                                        └── UserController.java
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
                            │   └── ports
                            │       └── out
                            │           └── CashRegisterQueryGateway.java
                            ├── infrastructure
                            │   ├── config
                            │   │   └── CashRegisterConfig.java
                            │   └── persistence
                            │       └── adapters
                            │           └── CashRegisterRepositoryAdapter.java
                            ├── reporting
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── CashRegisterExportRow.java
                            │   │   │   ├── CashRegisterReportFilter.java
                            │   │   │   ├── CustomerSummary.java
                            │   │   │   ├── ReportFormat.java
                            │   │   │   └── ReportType.java
                            │   │   ├── excel
                            │   │   │   ├── GenerateCashRegisterExcelUseCase.java
                            │   │   │   └── GenerateCashRegisterExcelUseCaseImpl.java
                            │   │   ├── ports
                            │   │   │   └── out
                            │   │   │       ├── CashRegisterExcelPort.java
                            │   │   │       ├── CashRegisterRangeReportPort.java
                            │   │   │       └── CashRegisterReportPort.java
                            │   │   ├── range
                            │   │   │   └── GenerateCashRegisterRangeReportUseCase.java
                            │   │   └── session
                            │   │       └── GenerateCashRegisterReportUseCase.java
                            │   └── infrastructure
                            │       ├── excel
                            │       │   ├── CashRegisterExcelExportService.java
                            │       │   └── CashRegisterExcelGenerator.java
                            │       ├── report
                            │       │   ├── CashRegisterRangeReportGenerator.java
                            │       │   └── CashRegisterSessionReportGenerator.java
                            │       └── web
                            │           └── CashRegisterExportController.java
                            └── sessions
                                ├── application
                                │   ├── close
                                │   │   ├── CloseRegisterRequest.java
                                │   │   └── CloseRegisterUseCase.java
                                │   ├── dto
                                │   │   ├── CashRegisterDetailResponse.java
                                │   │   ├── CashRegisterMapper.java
                                │   │   ├── CashRegisterMetrics.java
                                │   │   ├── CashRegisterResponse.java
                                │   │   ├── DailyTotals.java
                                │   │   ├── ExpenseSummaryItem.java
                                │   │   └── RevenueSummaryItem.java
                                │   ├── movements
                                │   │   ├── CashMovementRequest.java
                                │   │   ├── CashMovementResponse.java
                                │   │   ├── CashMovementService.java
                                │   │   └── RecordCashMovementUseCase.java
                                │   ├── open
                                │   │   ├── OpenRegisterRequest.java
                                │   │   └── OpenRegisterUseCase.java
                                │   ├── ports
                                │   │   ├── in
                                │   │   │   ├── CashRegisterOperations.java
                                │   │   │   ├── CreditNoteCashApi.java
                                │   │   │   ├── DebitNoteCashApi.java
                                │   │   │   ├── QueryPaymentMethodUsage.java
                                │   │   │   └── ReturnCashRecording.java
                                │   │   └── out
                                │   │       ├── CashRegisterActorGateway.java
                                │   │       └── CashRegisterPaymentMethodGateway.java
                                │   ├── queries
                                │   │   ├── GetActiveRegisterUseCase.java
                                │   │   └── GetRegisterHistoryUseCase.java
                                │   └── services
                                │       ├── CashRegisterOperationsService.java
                                │       └── ReturnCashRecordingService.java
                                ├── domain
                                │   ├── valueobjects
                                │   │   ├── CashMovementDirection.java
                                │   │   ├── CashMovementType.java
                                │   │   ├── RegisterId.java
                                │   │   └── RegisterStatus.java
                                │   ├── CashMovement.java
                                │   ├── CashRegister.java
                                │   ├── CashRegisterDetail.java
                                │   └── CashRegisterRepository.java
                                └── infrastructure
                                    ├── config
                                    │   ├── CashRegisterOperationsConfig.java
                                    │   └── ReturnCashRecordingConfig.java
                                    ├── integration
                                    │   ├── LocalCashRegisterActorGateway.java
                                    │   ├── LocalCashRegisterPaymentMethodGateway.java
                                    │   ├── LocalCreditNoteCashApi.java
                                    │   └── LocalDebitNoteCashApi.java
                                    ├── persistence
                                    │   ├── entities
                                    │   │   ├── CashMovementJpaEntity.java
                                    │   │   ├── CashRegisterDetailJpaEntity.java
                                    │   │   └── CashRegisterJpaEntity.java
                                    │   ├── queries
                                    │   │   └── JpaPaymentMethodUsageQuery.java
                                    │   └── repositories
                                    │       ├── CashMovementJpaRepository.java
                                    │       └── CashRegisterJpaRepository.java
                                    └── web
                                        └── CashRegisterController.java
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
                            │   ├── create
                            │   │   ├── CreateCategoryRequest.java
                            │   │   └── CreateCategoryUseCase.java
                            │   ├── delete
                            │   │   └── DeleteCategoryUseCase.java
                            │   ├── dto
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
                            │   │   ├── GetCategoryUseCase.java
                            │   │   ├── ListCategoriesUseCase.java
                            │   │   └── RepositoryProductCategoryQuery.java
                            │   └── update
                            │       ├── UpdateCategoryRequest.java
                            │       └── UpdateCategoryUseCase.java
                            ├── domain
                            │   ├── Category.java
                            │   └── CategoryRepository.java
                            └── infrastructure
                                ├── config
                                │   ├── CategoryConfig.java
                                │   └── ProductCategoryQueryConfig.java
                                ├── integration
                                │   └── ProductCategoryUsageAdapter.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── JpaCategoryRepository.java
                                │   ├── entities
                                │   │   └── CategoryEntity.java
                                │   └── repositories
                                │       └── SpringDataCategoryRepository.java
                                └── web
                                    └── CategoryController.java
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
                            │   ├── create
                            │   │   ├── CreateCreditNoteRequest.java
                            │   │   └── CreateCreditNoteUseCase.java
                            │   ├── delete
                            │   │   └── DeleteCreditNoteUseCase.java
                            │   ├── dto
                            │   │   └── response
                            │   │       └── CreditNoteResponse.java
                            │   ├── get
                            │   │   └── GetCreditNoteByIdUseCase.java
                            │   ├── list
                            │   │   └── GetCreditNotesUseCase.java
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
                            │   └── queries
                            │       ├── InvoiceCreditNoteDetailsQueryService.java
                            │       └── RepositoryCreditNoteLedgerQuery.java
                            ├── domain
                            │   ├── valueobjects
                            │   │   ├── CreditNoteId.java
                            │   │   └── CreditNoteNumber.java
                            │   ├── CreditNote.java
                            │   └── CreditNoteRepository.java
                            └── infrastructure
                                ├── config
                                │   └── CreditNoteLedgerQueryConfig.java
                                ├── integration
                                │   ├── LocalCreditNoteEffectsGateway.java
                                │   ├── LocalCreditNoteFinancialPositionGateway.java
                                │   └── LocalCreditNoteInvoiceGateway.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── CreditNoteRepositoryAdapter.java
                                │   ├── entities
                                │   │   ├── CreditNoteEntity.java
                                │   │   └── CreditNoteNumberSequenceEntity.java
                                │   ├── mappers
                                │   │   └── CreditNoteJpaMapper.java
                                │   └── repositories
                                │       ├── CreditNoteJpaRepository.java
                                │       └── CreditNoteNumberSequenceJpaRepository.java
                                └── web
                                    └── CreditNoteController.java
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
                            ├── balance
                            │   ├── application
                            │   │   ├── add
                            │   │   │   └── AddToCustomerBalanceUseCase.java
                            │   │   ├── advance
                            │   │   │   ├── CreateAdvanceRequest.java
                            │   │   │   └── CreateCustomerAdvanceUseCase.java
                            │   │   ├── consume
                            │   │   │   └── ConsumeCustomerBalanceUseCase.java
                            │   │   ├── dto
                            │   │   │   └── response
                            │   │   │       ├── BalanceTransactionResponse.java
                            │   │   │       ├── CustomerBalanceMutationResponse.java
                            │   │   │       └── CustomerBalanceResponse.java
                            │   │   ├── get
                            │   │   │   └── GetCustomerBalanceUseCase.java
                            │   │   ├── mappers
                            │   │   │   └── BalanceMapper.java
                            │   │   ├── ports
                            │   │   │   ├── in
                            │   │   │   │   ├── CreditNoteBalanceApi.java
                            │   │   │   │   ├── InvoiceBalanceConsumption.java
                            │   │   │   │   └── ReturnBalanceMutation.java
                            │   │   │   └── out
                            │   │   │       ├── CustomerActorGateway.java
                            │   │   │       └── CustomerCashGateway.java
                            │   │   ├── refund
                            │   │   │   ├── CreateRefundRequest.java
                            │   │   │   └── RefundCustomerBalanceUseCase.java
                            │   │   └── services
                            │   │       ├── InvoiceBalanceConsumptionService.java
                            │   │       └── ReturnBalanceMutationService.java
                            │   ├── domain
                            │   │   ├── valueobjects
                            │   │   │   └── BalanceType.java
                            │   │   ├── BalanceTransaction.java
                            │   │   ├── CustomerBalance.java
                            │   │   └── CustomerBalanceRepository.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   ├── InvoiceBalanceConsumptionConfig.java
                            │       │   └── ReturnBalanceMutationConfig.java
                            │       ├── integration
                            │       │   ├── LocalCreditNoteBalanceApi.java
                            │       │   ├── LocalCustomerActorGateway.java
                            │       │   └── LocalCustomerCashGateway.java
                            │       ├── persistence
                            │       │   ├── adapters
                            │       │   │   └── CustomerBalanceRepositoryAdapter.java
                            │       │   ├── entities
                            │       │   │   ├── BalanceTransactionEntity.java
                            │       │   │   ├── BalanceTxSequenceEntity.java
                            │       │   │   └── CustomerBalanceEntity.java
                            │       │   ├── mappers
                            │       │   │   ├── BalanceTransactionJpaMapper.java
                            │       │   │   └── CustomerBalanceJpaMapper.java
                            │       │   └── repositories
                            │       │       └── CustomerBalanceJpaRepository.java
                            │       └── web
                            │           └── CustomerBalanceController.java
                            ├── credit
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   └── response
                            │   │   │       ├── CreditStatusResponse.java
                            │   │   │       └── OverdueInvoiceResponse.java
                            │   │   ├── exceptions
                            │   │   │   └── CreditLimitExceededException.java
                            │   │   ├── overdue
                            │   │   │   └── GetOverdueInvoicesUseCase.java
                            │   │   ├── ports
                            │   │   │   ├── in
                            │   │   │   │   ├── CustomerCreditContextEntry.java
                            │   │   │   │   ├── CustomerCreditContextQuery.java
                            │   │   │   │   ├── CustomerCreditEligibilityQuery.java
                            │   │   │   │   └── CustomerCreditLimits.java
                            │   │   │   └── out
                            │   │   │       └── CreditValidationGateway.java
                            │   │   ├── queries
                            │   │   │   └── RepositoryCustomerCreditContextQuery.java
                            │   │   ├── services
                            │   │   │   └── CustomerCreditEligibilityService.java
                            │   │   └── status
                            │   │       └── GetCustomerCreditStatusUseCase.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   └── CustomerCreditQueryConfig.java
                            │       ├── integration
                            │       │   └── LocalCreditValidationGateway.java
                            │       └── web
                            │           └── CustomerCreditController.java
                            ├── infrastructure
                            │   └── config
                            │       └── CustomerConfig.java
                            ├── profile
                            │   ├── application
                            │   │   ├── delete
                            │   │   │   ├── CustomerHasAssociatedRecordsException.java
                            │   │   │   └── DeleteCustomerUseCase.java
                            │   │   ├── dto
                            │   │   │   ├── request
                            │   │   │   │   ├── AddressRequest.java
                            │   │   │   │   ├── EmailRequest.java
                            │   │   │   │   └── PhoneRequest.java
                            │   │   │   └── response
                            │   │   │       ├── AddressResponse.java
                            │   │   │       ├── CustomerDetailResponse.java
                            │   │   │       ├── CustomerListResponse.java
                            │   │   │       ├── EmailResponse.java
                            │   │   │       └── PhoneResponse.java
                            │   │   ├── exceptions
                            │   │   │   ├── CustomerNotFoundException.java
                            │   │   │   └── DuplicateCustomerRifException.java
                            │   │   ├── get
                            │   │   │   └── GetCustomerUseCase.java
                            │   │   ├── list
                            │   │   │   └── ListCustomersUseCase.java
                            │   │   ├── mappers
                            │   │   │   └── CustomerMapper.java
                            │   │   ├── ports
                            │   │   │   └── in
                            │   │   │       ├── CustomerCreditTermsQuery.java
                            │   │   │       ├── CustomerProductPricingQuery.java
                            │   │   │       ├── CustomerProfileQuery.java
                            │   │   │       ├── CustomerProfileSnapshot.java
                            │   │   │       └── InvoiceCustomerQuery.java
                            │   │   ├── queries
                            │   │   │   ├── RepositoryCustomerCreditTermsQuery.java
                            │   │   │   ├── RepositoryCustomerProductPricingQuery.java
                            │   │   │   ├── RepositoryCustomerProfileQuery.java
                            │   │   │   └── RepositoryInvoiceCustomerQuery.java
                            │   │   ├── register
                            │   │   │   ├── CreateCustomerRequest.java
                            │   │   │   ├── CustomerCreateResponse.java
                            │   │   │   └── RegisterCustomerUseCase.java
                            │   │   └── update
                            │   │       ├── UpdateCustomerRequest.java
                            │   │       └── UpdateCustomerUseCase.java
                            │   ├── domain
                            │   │   ├── valueobjects
                            │   │   │   ├── AddressId.java
                            │   │   │   ├── CreditDays.java
                            │   │   │   ├── CreditLimit.java
                            │   │   │   ├── CustomerAlias.java
                            │   │   │   ├── CustomerCode.java
                            │   │   │   ├── CustomerId.java
                            │   │   │   ├── CustomerType.java
                            │   │   │   ├── EmailAddress.java
                            │   │   │   ├── EmailId.java
                            │   │   │   ├── LegalName.java
                            │   │   │   ├── PaymentType.java
                            │   │   │   ├── PhoneId.java
                            │   │   │   ├── PhoneNumber.java
                            │   │   │   └── Rif.java
                            │   │   ├── Customer.java
                            │   │   ├── CustomerAddress.java
                            │   │   ├── CustomerCodeGenerator.java
                            │   │   ├── CustomerEmail.java
                            │   │   ├── CustomerPhone.java
                            │   │   └── CustomerRepository.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   ├── CustomerCreditTermsConfig.java
                            │       │   ├── CustomerProductPricingConfig.java
                            │       │   ├── CustomerProfileQueryConfig.java
                            │       │   └── InvoiceCustomerQueryConfig.java
                            │       ├── persistence
                            │       │   ├── adapters
                            │       │   │   └── JpaCustomerRepository.java
                            │       │   ├── entities
                            │       │   │   ├── CustomerAddressEntity.java
                            │       │   │   ├── CustomerEmailEntity.java
                            │       │   │   ├── CustomerEntity.java
                            │       │   │   └── CustomerPhoneEntity.java
                            │       │   └── repositories
                            │       │       └── SpringDataCustomerRepository.java
                            │       ├── services
                            │       │   └── SequentialCustomerCodeGenerator.java
                            │       └── web
                            │           └── CustomerController.java
                            └── warehouse_access
                                ├── application
                                │   ├── dto
                                │   │   └── response
                                │   │       └── CustomerWarehouseAccessResponse.java
                                │   ├── exceptions
                                │   │   ├── CustomerWarehouseAccessAlreadyExistsException.java
                                │   │   └── CustomerWarehouseAccessNotFoundException.java
                                │   ├── grant
                                │   │   └── GrantWarehouseAccessUseCase.java
                                │   ├── list
                                │   │   └── ListCustomerWarehousesUseCase.java
                                │   ├── ports
                                │   │   ├── in
                                │   │   │   └── CustomerWarehouseAccessQuery.java
                                │   │   └── out
                                │   │       ├── model
                                │   │       │   └── WarehouseSnapshot.java
                                │   │       └── WarehouseDirectoryGateway.java
                                │   ├── queries
                                │   │   └── RepositoryCustomerWarehouseAccessQuery.java
                                │   └── revoke
                                │       └── RevokeWarehouseAccessUseCase.java
                                ├── domain
                                │   ├── CustomerWarehouseAccess.java
                                │   └── CustomerWarehouseAccessRepository.java
                                └── infrastructure
                                    ├── config
                                    │   └── CustomerWarehouseAccessQueryConfig.java
                                    ├── integration
                                    │   └── LocalWarehouseDirectoryGateway.java
                                    ├── persistence
                                    │   ├── adapters
                                    │   │   └── JpaCustomerWarehouseAccessRepository.java
                                    │   ├── entities
                                    │   │   └── CustomerWarehouseAccessEntity.java
                                    │   └── repositories
                                    │       └── SpringDataCustomerWarehouseAccessRepository.java
                                    └── web
                                        └── CustomerWarehouseAccessController.java
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
                            │   └── summary
                            │       └── GetDashboardUseCase.java
                            └── infrastructure
                                ├── persistence
                                │   └── queries
                                │       └── JdbcDashboardQueryAdapter.java
                                └── web
                                    └── DashboardController.java
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
                            │   ├── create
                            │   │   ├── CreateDebitNoteRequest.java
                            │   │   └── CreateDebitNoteUseCase.java
                            │   ├── delete
                            │   │   └── DeleteDebitNoteUseCase.java
                            │   ├── dto
                            │   │   └── response
                            │   │       └── DebitNoteResponse.java
                            │   ├── get
                            │   │   └── GetDebitNoteByIdUseCase.java
                            │   ├── list
                            │   │   └── GetDebitNotesUseCase.java
                            │   ├── mappers
                            │   │   └── DebitNoteMapper.java
                            │   ├── ports
                            │   │   ├── in
                            │   │   │   ├── DebitNoteLedgerQuery.java
                            │   │   │   └── InvoiceDebitNoteDetailsQuery.java
                            │   │   └── out
                            │   │       ├── DebitNoteCashGateway.java
                            │   │       └── DebitNoteInvoiceGateway.java
                            │   └── queries
                            │       ├── InvoiceDebitNoteDetailsQueryService.java
                            │       └── RepositoryDebitNoteLedgerQuery.java
                            ├── domain
                            │   ├── valueobjects
                            │   │   ├── DebitNoteId.java
                            │   │   └── DebitNoteNumber.java
                            │   ├── DebitNote.java
                            │   └── DebitNoteRepository.java
                            └── infrastructure
                                ├── config
                                │   └── DebitNoteLedgerQueryConfig.java
                                ├── integration
                                │   ├── LocalDebitNoteCashGateway.java
                                │   └── LocalDebitNoteInvoiceGateway.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── DebitNoteRepositoryAdapter.java
                                │   ├── entities
                                │   │   ├── DebitNoteEntity.java
                                │   │   └── DebitNoteNumberSequenceEntity.java
                                │   ├── mappers
                                │   │   └── DebitNoteJpaMapper.java
                                │   └── repositories
                                │       ├── DebitNoteJpaRepository.java
                                │       └── DebitNoteNumberSequenceJpaRepository.java
                                └── web
                                    └── DebitNoteController.java
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
                            ├── config
                            │   ├── CorsConfig.java
                            │   ├── GlobalExceptionHandler.java
                            │   ├── JpaAuditingConfig.java
                            │   ├── MultipartConfig.java
                            │   └── OpenApiConfig.java
                            └── web
                                └── PartMultipartFile.java
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
                            │   ├── cancel
                            │   │   ├── CancelInvoiceRequest.java
                            │   │   └── CancelInvoiceUseCase.java
                            │   ├── confirm
                            │   │   ├── ConfirmInvoiceRequest.java
                            │   │   └── ConfirmInvoiceUseCase.java
                            │   ├── create
                            │   │   ├── from_order
                            │   │   │   └── OrderInvoiceCreationService.java
                            │   │   ├── from_quote
                            │   │   │   └── QuoteInvoiceCreationService.java
                            │   │   ├── CreateInvoiceRequest.java
                            │   │   └── CreateInvoiceUseCase.java
                            │   ├── delete
                            │   │   └── DeleteInvoiceUseCase.java
                            │   ├── dto
                            │   │   └── response
                            │   │       ├── InvoiceDetailResponse.java
                            │   │       └── InvoiceListResponse.java
                            │   ├── duplicate
                            │   │   ├── DuplicateInvoiceRequest.java
                            │   │   └── DuplicateInvoiceUseCase.java
                            │   ├── get
                            │   │   └── GetInvoiceByIdUseCase.java
                            │   ├── list
                            │   │   └── GetInvoicesUseCase.java
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
                            │   ├── queries
                            │   │   ├── InvoiceDocumentQueryService.java
                            │   │   ├── RepositoryInvoiceCreditLedgerQuery.java
                            │   │   ├── RepositoryInvoiceReturnAdjustments.java
                            │   │   └── RepositoryReturnInvoiceQuery.java
                            │   ├── services
                            │   │   └── InvoiceAuthorizationService.java
                            │   └── update
                            │       ├── UpdateInvoiceRequest.java
                            │       └── UpdateInvoiceUseCase.java
                            ├── domain
                            │   ├── exceptions
                            │   │   ├── InvoiceNotFoundException.java
                            │   │   ├── InvoiceNotReturnableException.java
                            │   │   └── ReturnQuantityExceedsException.java
                            │   ├── valueobjects
                            │   │   ├── InvoiceId.java
                            │   │   ├── InvoiceLineId.java
                            │   │   ├── InvoiceNumber.java
                            │   │   ├── InvoiceReturnAdjustment.java
                            │   │   ├── InvoiceReturnStatus.java
                            │   │   ├── InvoiceStatus.java
                            │   │   └── PaymentType.java
                            │   ├── Invoice.java
                            │   ├── InvoiceLine.java
                            │   ├── InvoiceLineAllocation.java
                            │   ├── InvoiceRepository.java
                            │   └── InvoiceStatusService.java
                            └── infrastructure
                                ├── config
                                │   ├── InvoiceCreditQueryConfig.java
                                │   ├── InvoiceDocumentQueryConfig.java
                                │   ├── InvoiceReturnAdjustmentConfig.java
                                │   ├── OrderInvoiceCreationConfig.java
                                │   ├── QuoteInvoiceCreationConfig.java
                                │   └── ReturnInvoiceQueryConfig.java
                                ├── integration
                                │   ├── InvoiceCustomerCreditAdapter.java
                                │   ├── InvoiceStockAvailabilityAdapter.java
                                │   ├── LocalActorIdentityGateway.java
                                │   ├── LocalCustomerBalanceGateway.java
                                │   ├── LocalCustomerCreditTermsGateway.java
                                │   ├── LocalInventoryGatewayAdapter.java
                                │   ├── LocalInvoiceCashRegisterGateway.java
                                │   ├── LocalInvoiceCatalogGateway.java
                                │   ├── LocalInvoiceCustomerGateway.java
                                │   ├── LocalInvoiceFinancialPositionGateway.java
                                │   ├── LocalInvoiceNoteApi.java
                                │   ├── LocalInvoiceNoteDetailsAdapter.java
                                │   ├── LocalInvoiceNotificationGateway.java
                                │   ├── LocalInvoiceOrderGateway.java
                                │   ├── LocalInvoicePaymentSettlementGateway.java
                                │   ├── LocalInvoicePaymentTotalsAdapter.java
                                │   ├── LocalInvoiceQuoteGateway.java
                                │   ├── LocalInvoiceReturnDetailsAdapter.java
                                │   └── LocalStockDeductionGateway.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── InvoiceRepositoryAdapter.java
                                │   ├── entities
                                │   │   ├── InvoiceEntity.java
                                │   │   ├── InvoiceLineAllocationEntity.java
                                │   │   ├── InvoiceLineEntity.java
                                │   │   └── InvoiceNumberSequenceEntity.java
                                │   ├── mappers
                                │   │   └── InvoiceJpaMapper.java
                                │   ├── queries
                                │   │   └── JpaInvoicesProductReferenceQuery.java
                                │   └── repositories
                                │       ├── InvoiceJpaRepository.java
                                │       ├── InvoiceNumberSequenceJpaRepository.java
                                │       └── SpringDataInvoicesProductReferenceRepository.java
                                ├── services
                                │   └── InvoiceStatusServiceImpl.java
                                └── web
                                    └── InvoiceController.java
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
                            │   ├── quotes
                            │   │   ├── QuoteConversionNotificationService.java
                            │   │   └── QuoteRejectionNotificationService.java
                            │   └── services
                            │       └── NotificationService.java
                            ├── domain
                            │   ├── valueobjects
                            │   │   ├── NotificationId.java
                            │   │   └── NotificationType.java
                            │   ├── Notification.java
                            │   └── NotificationRepository.java
                            └── infrastructure
                                ├── config
                                │   └── WebSocketConfig.java
                                ├── integration
                                │   └── AuthNotificationRecipientAdapter.java
                                ├── messaging
                                │   └── WebSocketNotificationDeliveryAdapter.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── NotificationRepositoryAdapter.java
                                │   ├── entities
                                │   │   └── NotificationEntity.java
                                │   ├── mappers
                                │   │   └── NotificationJpaMapper.java
                                │   └── repositories
                                │       └── NotificationJpaRepository.java
                                └── web
                                    └── NotificationController.java
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
                            │   ├── assign_warehouses
                            │   │   ├── AssignOrderWarehousesRequest.java
                            │   │   └── AssignOrderWarehousesUseCase.java
                            │   ├── cancel
                            │   │   ├── CancelOrderRequest.java
                            │   │   └── CancelOrderUseCase.java
                            │   ├── confirm
                            │   │   └── ConfirmOrderUseCase.java
                            │   ├── convert_to_invoice
                            │   │   ├── ConvertToInvoiceRequest.java
                            │   │   ├── ConvertToInvoiceResponse.java
                            │   │   └── ConvertToInvoiceUseCase.java
                            │   ├── create
                            │   │   ├── from_quote
                            │   │   │   └── QuoteOrderCreationService.java
                            │   │   ├── CreateOrderRequest.java
                            │   │   └── CreateOrderUseCase.java
                            │   ├── delete
                            │   │   └── DeleteOrderUseCase.java
                            │   ├── dto
                            │   │   └── response
                            │   │       ├── OrderDetailResponse.java
                            │   │       ├── OrderListResponse.java
                            │   │       └── OrderProductResponse.java
                            │   ├── get
                            │   │   └── GetOrderByIdUseCase.java
                            │   ├── list
                            │   │   └── GetOrdersUseCase.java
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
                            │   ├── products
                            │   │   └── GetProductsForOrderUseCase.java
                            │   ├── queries
                            │   │   └── OrderDocumentQueryService.java
                            │   └── update
                            │       ├── UpdateOrderRequest.java
                            │       └── UpdateOrderUseCase.java
                            ├── domain
                            │   ├── exceptions
                            │   │   ├── OrderCannotBeDeletedException.java
                            │   │   ├── OrderNotFoundException.java
                            │   │   └── OrderNotPendingException.java
                            │   ├── valueobjects
                            │   │   ├── OrderId.java
                            │   │   ├── OrderLineId.java
                            │   │   ├── OrderNumber.java
                            │   │   └── OrderStatus.java
                            │   ├── Order.java
                            │   ├── OrderLine.java
                            │   ├── OrderLineAllocation.java
                            │   └── OrderRepository.java
                            └── infrastructure
                                ├── config
                                │   ├── OrderConfig.java
                                │   ├── OrderDocumentQueryConfig.java
                                │   └── QuoteOrderCreationConfig.java
                                ├── integration
                                │   ├── LocalInvoiceCreationGateway.java
                                │   ├── LocalOrderActorAdapter.java
                                │   ├── LocalOrderCustomerAdapter.java
                                │   ├── LocalOrderInventoryGateway.java
                                │   ├── LocalOrderNotificationAdapter.java
                                │   ├── LocalOrderProductCatalogAdapter.java
                                │   ├── LocalOrderWarehouseAdapter.java
                                │   ├── OrderCustomerCreditAdapter.java
                                │   └── OrderStockAvailabilityAdapter.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── OrderRepositoryAdapter.java
                                │   ├── entities
                                │   │   ├── OrderEntity.java
                                │   │   ├── OrderLineAllocationEntity.java
                                │   │   ├── OrderLineCategorySnapshotEntity.java
                                │   │   ├── OrderLineEntity.java
                                │   │   └── OrderNumberSequenceEntity.java
                                │   ├── mappers
                                │   │   └── OrderJpaMapper.java
                                │   ├── queries
                                │   │   └── JpaOrdersProductReferenceQuery.java
                                │   └── repositories
                                │       ├── OrderJpaRepository.java
                                │       ├── OrderNumberSequenceJpaRepository.java
                                │       └── SpringDataOrdersProductReferenceRepository.java
                                └── web
                                    └── OrderController.java
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
                            │   ├── create
                            │   │   ├── CreatePaymentMethodRequest.java
                            │   │   └── CreatePaymentMethodUseCase.java
                            │   ├── delete
                            │   │   └── DeletePaymentMethodUseCase.java
                            │   ├── dto
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
                            │   │   ├── GetPaymentMethodByIdUseCase.java
                            │   │   ├── ListPaymentMethodsUseCase.java
                            │   │   └── RepositoryPaymentMethodDetailsQuery.java
                            │   ├── toggle
                            │   │   └── TogglePaymentMethodUseCase.java
                            │   └── update
                            │       ├── UpdatePaymentMethodRequest.java
                            │       └── UpdatePaymentMethodUseCase.java
                            ├── domain
                            │   ├── valueobjects
                            │   │   └── PaymentMethodId.java
                            │   ├── PaymentMethod.java
                            │   └── PaymentMethodRepository.java
                            └── infrastructure
                                ├── integration
                                │   └── LocalPaymentMethodUsageGateway.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── PaymentMethodRepositoryAdapter.java
                                │   ├── entities
                                │   │   └── PaymentMethodEntity.java
                                │   ├── mappers
                                │   │   └── PaymentMethodJpaMapper.java
                                │   └── repositories
                                │       └── PaymentMethodJpaRepository.java
                                └── web
                                    └── PaymentMethodController.java
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
                            ├── collection
                            │   ├── application
                            │   │   ├── approve
                            │   │   │   ├── ApprovePaymentRequest.java
                            │   │   │   └── ApprovePaymentUseCase.java
                            │   │   ├── create
                            │   │   │   ├── CreatePaymentRequest.java
                            │   │   │   └── CreatePaymentUseCase.java
                            │   │   ├── delete
                            │   │   │   └── DeletePaymentUseCase.java
                            │   │   ├── dto
                            │   │   │   └── response
                            │   │   │       ├── PaymentListResponse.java
                            │   │   │       ├── PaymentResponse.java
                            │   │   │       └── PaymentResultResponse.java
                            │   │   ├── mappers
                            │   │   │   └── PaymentMapper.java
                            │   │   ├── pay_customer_debt
                            │   │   │   ├── PayAllCustomerDebtUseCase.java
                            │   │   │   └── PayCustomerDebtRequest.java
                            │   │   ├── pay_invoice
                            │   │   │   ├── PayInvoiceRequest.java
                            │   │   │   └── PayInvoiceUseCase.java
                            │   │   ├── pay_partial_debt
                            │   │   │   ├── PayPartialDebtRequest.java
                            │   │   │   └── PayPartialDebtUseCase.java
                            │   │   ├── ports
                            │   │   │   ├── in
                            │   │   │   │   ├── CashInvoiceSettlement.java
                            │   │   │   │   ├── InvoicePaymentTotalQuery.java
                            │   │   │   │   ├── InvoicePaymentTotalsQuery.java
                            │   │   │   │   ├── PaymentDocumentQuery.java
                            │   │   │   │   ├── PaymentDocumentSnapshot.java
                            │   │   │   │   ├── QueryPaymentMethodUsage.java
                            │   │   │   │   └── ReturnPaymentOperations.java
                            │   │   │   └── out
                            │   │   │       ├── CashRegisterGateway.java
                            │   │   │       ├── PaymentActorGateway.java
                            │   │   │       ├── PaymentCustomerGateway.java
                            │   │   │       ├── PaymentInvoiceGateway.java
                            │   │   │       └── PaymentNotificationGateway.java
                            │   │   ├── queries
                            │   │   │   ├── GetAllPaymentsUseCase.java
                            │   │   │   └── GetPaymentByIdUseCase.java
                            │   │   ├── reject
                            │   │   │   ├── RejectPaymentRequest.java
                            │   │   │   └── RejectPaymentUseCase.java
                            │   │   ├── services
                            │   │   │   ├── CashInvoiceSettlementService.java
                            │   │   │   ├── PaymentDocumentQueryService.java
                            │   │   │   └── ReturnPaymentOperationsService.java
                            │   │   └── update
                            │   │       ├── UpdatePaymentRequest.java
                            │   │       └── UpdatePaymentUseCase.java
                            │   ├── domain
                            │   │   ├── valueobjects
                            │   │   │   ├── InvoiceRemainingBalance.java
                            │   │   │   ├── PaymentId.java
                            │   │   │   ├── PaymentNumber.java
                            │   │   │   ├── PaymentSource.java
                            │   │   │   └── PaymentStatus.java
                            │   │   ├── Payment.java
                            │   │   ├── PaymentAllocation.java
                            │   │   ├── PaymentAllocationRepository.java
                            │   │   ├── PaymentAllocationService.java
                            │   │   └── PaymentRepository.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   ├── CashInvoiceSettlementConfig.java
                            │       │   ├── PaymentDocumentQueryConfig.java
                            │       │   └── ReturnPaymentOperationsConfig.java
                            │       ├── integration
                            │       │   ├── LocalPaymentActorGateway.java
                            │       │   ├── LocalPaymentCashRegisterGateway.java
                            │       │   ├── LocalPaymentCustomerGateway.java
                            │       │   ├── LocalPaymentInvoiceGateway.java
                            │       │   ├── LocalPaymentNotificationGateway.java
                            │       │   ├── RepositoryInvoicePaymentTotalQuery.java
                            │       │   └── RepositoryInvoicePaymentTotalsQuery.java
                            │       ├── persistence
                            │       │   ├── adapters
                            │       │   │   ├── PaymentAllocationRepositoryAdapter.java
                            │       │   │   └── PaymentRepositoryAdapter.java
                            │       │   ├── entities
                            │       │   │   ├── PaymentAllocationEntity.java
                            │       │   │   └── PaymentEntity.java
                            │       │   ├── mappers
                            │       │   │   ├── PaymentAllocationJpaMapper.java
                            │       │   │   └── PaymentJpaMapper.java
                            │       │   ├── queries
                            │       │   │   └── JpaPaymentMethodUsageQuery.java
                            │       │   └── repositories
                            │       │       ├── PaymentAllocationJpaRepository.java
                            │       │       └── PaymentJpaRepository.java
                            │       └── web
                            │           └── PaymentController.java
                            ├── credit
                            │   ├── application
                            │   │   ├── ports
                            │   │   │   ├── in
                            │   │   │   │   ├── CreditStatusSnapshot.java
                            │   │   │   │   ├── CustomerCreditQuery.java
                            │   │   │   │   └── OverdueInvoiceSnapshot.java
                            │   │   │   └── out
                            │   │   │       ├── model
                            │   │   │       │   ├── CustomerCreditContext.java
                            │   │   │       │   └── InvoiceCreditEntry.java
                            │   │   │       ├── CreditNoteLedgerGateway.java
                            │   │   │       ├── CustomerCreditContextGateway.java
                            │   │   │       ├── DebitNoteLedgerGateway.java
                            │   │   │       └── InvoiceCreditLedgerGateway.java
                            │   │   └── services
                            │   │       └── CustomerCreditQueryService.java
                            │   ├── domain
                            │   │   └── CustomerCreditPolicy.java
                            │   └── infrastructure
                            │       └── integration
                            │           ├── LocalCreditNoteLedgerGateway.java
                            │           ├── LocalCustomerCreditContextGateway.java
                            │           ├── LocalDebitNoteLedgerGateway.java
                            │           └── LocalInvoiceCreditLedgerGateway.java
                            └── infrastructure
                                └── config
                                    └── PaymentConfig.java
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
                            │   ├── create
                            │   │   ├── CreatePriceTypeRequest.java
                            │   │   └── CreatePriceTypeUseCase.java
                            │   ├── delete
                            │   │   └── DeletePriceTypeUseCase.java
                            │   ├── dto
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
                            │   │   ├── GetPriceTypeUseCase.java
                            │   │   ├── ListPriceTypesUseCase.java
                            │   │   └── RepositoryProductPriceTypeQuery.java
                            │   └── update
                            │       ├── UpdatePriceTypeRequest.java
                            │       └── UpdatePriceTypeUseCase.java
                            ├── domain
                            │   ├── PriceType.java
                            │   └── PriceTypeRepository.java
                            └── infrastructure
                                ├── config
                                │   ├── PriceTypeConfig.java
                                │   └── ProductPriceTypeQueryConfig.java
                                ├── integration
                                │   └── ProductPriceTypeUsageAdapter.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── JpaPriceTypeRepository.java
                                │   ├── entities
                                │   │   └── PriceTypeEntity.java
                                │   ├── queries
                                │   │   └── JpaProductPriceTypeExistenceQuery.java
                                │   └── repositories
                                │       └── SpringDataPriceTypeRepository.java
                                └── web
                                    └── PriceTypeController.java
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
                            │   ├── create
                            │   │   ├── CreateProductRequest.java
                            │   │   └── CreateProductUseCase.java
                            │   ├── delete
                            │   │   └── DeleteProductUseCase.java
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   └── ProductPriceRequest.java
                            │   │   └── response
                            │   │       ├── ProductDetailResponse.java
                            │   │       ├── ProductListResponse.java
                            │   │       └── ProductPriceDetailResponse.java
                            │   ├── exceptions
                            │   │   ├── CompositeProductCannotBeComponentException.java
                            │   │   ├── DuplicateProductCodeException.java
                            │   │   ├── DuplicateProductComponentException.java
                            │   │   ├── ProductHasAssociatedRecordsException.java
                            │   │   ├── ProductHasStockException.java
                            │   │   └── ProductNotFoundException.java
                            │   ├── mappers
                            │   │   └── ProductMapper.java
                            │   ├── photos
                            │   │   ├── RemoveProductPhotoUseCase.java
                            │   │   └── UploadProductPhotoUseCase.java
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
                            │   │   ├── GetProductUseCase.java
                            │   │   ├── ListProductsUseCase.java
                            │   │   ├── RepositoryPurchaseProductCatalogQuery.java
                            │   │   └── RepositoryStockCatalogQuery.java
                            │   ├── stock
                            │   │   ├── GetProductStockUseCase.java
                            │   │   └── ProductStockByWarehouseResponse.java
                            │   ├── transactions
                            │   │   ├── GetProductsForTransactionUseCase.java
                            │   │   └── ProductWithStockResponse.java
                            │   └── update
                            │       ├── UpdateProductRequest.java
                            │       └── UpdateProductUseCase.java
                            ├── domain
                            │   ├── exceptions
                            │   │   └── ProductReferencedException.java
                            │   ├── valueobjects
                            │   │   └── ProductPrice.java
                            │   ├── Product.java
                            │   ├── ProductComponent.java
                            │   └── ProductRepository.java
                            └── infrastructure
                                ├── config
                                │   ├── ProductConfig.java
                                │   ├── PurchaseProductCatalogConfig.java
                                │   └── StockCatalogConfig.java
                                ├── integration
                                │   ├── LocalCustomerProductContextGatewayAdapter.java
                                │   ├── LocalProductCategoryGatewayAdapter.java
                                │   ├── LocalProductFileStorageGatewayAdapter.java
                                │   ├── LocalProductPriceTypeExistenceGatewayAdapter.java
                                │   ├── LocalProductPriceTypeGatewayAdapter.java
                                │   ├── LocalProductReferenceGatewayAdapter.java
                                │   ├── LocalProductStockFilterGatewayAdapter.java
                                │   ├── LocalProductWarehouseStockGatewayAdapter.java
                                │   └── LocalSalesProductCatalogQuery.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── JpaProductRepository.java
                                │   ├── entities
                                │   │   ├── ProductCodeSequenceEntity.java
                                │   │   ├── ProductComponentEntity.java
                                │   │   ├── ProductEntity.java
                                │   │   └── ProductPriceEntity.java
                                │   ├── queries
                                │   │   └── JpaProductUsageQueries.java
                                │   └── repositories
                                │       ├── SpringDataProductCodeSequenceRepository.java
                                │       ├── SpringDataProductRepository.java
                                │       └── SpringDataProductUsageRepository.java
                                └── web
                                    └── ProductController.java
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
                            │   ├── cancel
                            │   │   ├── CancelPurchaseRequest.java
                            │   │   └── CancelPurchaseUseCase.java
                            │   ├── confirm
                            │   │   └── ConfirmPurchaseUseCase.java
                            │   ├── create
                            │   │   ├── CreatePurchaseRequest.java
                            │   │   └── CreatePurchaseUseCase.java
                            │   ├── delete
                            │   │   └── DeletePurchaseUseCase.java
                            │   ├── dto
                            │   │   └── response
                            │   │       ├── PurchaseDetailResponse.java
                            │   │       ├── PurchaseListResponse.java
                            │   │       └── PurchaseProductResponse.java
                            │   ├── get
                            │   │   └── GetPurchaseByIdUseCase.java
                            │   ├── list
                            │   │   └── ListPurchasesUseCase.java
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
                            │   ├── products
                            │   │   └── GetProductsForPurchaseUseCase.java
                            │   ├── queries
                            │   │   ├── PurchaseDocumentQueryService.java
                            │   │   └── RepositorySupplierPurchaseUsageQuery.java
                            │   ├── services
                            │   │   └── PurchaseDetailResponseAssembler.java
                            │   └── update
                            │       ├── UpdatePurchaseRequest.java
                            │       └── UpdatePurchaseUseCase.java
                            ├── domain
                            │   ├── exceptions
                            │   │   └── PurchaseNotFoundException.java
                            │   ├── valueobjects
                            │   │   ├── PurchaseId.java
                            │   │   ├── PurchaseLineAllocationId.java
                            │   │   ├── PurchaseLineId.java
                            │   │   ├── PurchaseNumber.java
                            │   │   └── PurchaseStatus.java
                            │   ├── Purchase.java
                            │   ├── PurchaseLine.java
                            │   ├── PurchaseLineAllocation.java
                            │   └── PurchaseRepository.java
                            └── infrastructure
                                ├── config
                                │   ├── PurchaseConfig.java
                                │   ├── PurchaseDocumentQueryConfig.java
                                │   └── SupplierPurchaseUsageConfig.java
                                ├── integration
                                │   ├── LocalPurchaseActorGatewayAdapter.java
                                │   ├── LocalPurchaseProductCatalogGateway.java
                                │   ├── LocalPurchaseProductGatewayAdapter.java
                                │   ├── LocalPurchaseSupplierGatewayAdapter.java
                                │   ├── LocalPurchaseWarehouseGatewayAdapter.java
                                │   └── WarehouseInventoryAdapter.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── PurchaseRepositoryAdapter.java
                                │   ├── entities
                                │   │   ├── PurchaseEntity.java
                                │   │   ├── PurchaseLineAllocationEntity.java
                                │   │   ├── PurchaseLineEntity.java
                                │   │   └── PurchaseNumberSequenceEntity.java
                                │   ├── mappers
                                │   │   └── PurchaseJpaMapper.java
                                │   └── repositories
                                │       ├── PurchaseJpaRepository.java
                                │       └── PurchaseNumberSequenceJpaRepository.java
                                └── web
                                    └── PurchaseController.java
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
                            │   ├── assign_warehouses
                            │   │   ├── AssignQuoteWarehousesRequest.java
                            │   │   └── AssignQuoteWarehousesUseCase.java
                            │   ├── cancel
                            │   │   ├── CancelQuoteRequest.java
                            │   │   └── CancelQuoteUseCase.java
                            │   ├── convert_to_invoice
                            │   │   ├── ConvertQuoteToInvoiceRequest.java
                            │   │   ├── ConvertQuoteToInvoiceUseCase.java
                            │   │   └── QuoteConvertedToInvoiceResponse.java
                            │   ├── convert_to_order
                            │   │   ├── ConvertQuoteToOrderUseCase.java
                            │   │   └── QuoteConvertedToOrderResponse.java
                            │   ├── create
                            │   │   ├── CreateQuoteRequest.java
                            │   │   └── CreateQuoteUseCase.java
                            │   ├── delete
                            │   │   └── DeleteQuoteUseCase.java
                            │   ├── dto
                            │   │   └── response
                            │   │       ├── QuoteAllocationDetailResponse.java
                            │   │       ├── QuoteDetailResponse.java
                            │   │       ├── QuoteLineDetailResponse.java
                            │   │       ├── QuoteListResponse.java
                            │   │       ├── QuoteProductResponse.java
                            │   │       ├── QuoteWarehouseResponse.java
                            │   │       └── WarehouseStockInfoResponse.java
                            │   ├── get
                            │   │   └── GetQuoteByIdUseCase.java
                            │   ├── list
                            │   │   └── GetQuotesUseCase.java
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
                            │   ├── products
                            │   │   └── GetProductsForQuoteUseCase.java
                            │   ├── queries
                            │   │   └── QuoteDocumentQueryService.java
                            │   ├── update
                            │   │   ├── UpdateQuoteRequest.java
                            │   │   └── UpdateQuoteUseCase.java
                            │   └── warehouses
                            │       └── GetWarehousesForQuoteUseCase.java
                            ├── domain
                            │   ├── exceptions
                            │   │   ├── InvalidQuoteAllocationException.java
                            │   │   ├── InvalidQuoteLineException.java
                            │   │   ├── QuoteNotFoundException.java
                            │   │   └── QuoteStateException.java
                            │   ├── valueobjects
                            │   │   ├── QuoteAllocationId.java
                            │   │   ├── QuoteLineId.java
                            │   │   ├── QuoteNumber.java
                            │   │   └── QuoteStatus.java
                            │   ├── Quote.java
                            │   ├── QuoteAllocation.java
                            │   ├── QuoteLine.java
                            │   └── QuoteRepository.java
                            └── infrastructure
                                ├── config
                                │   ├── QuoteConfig.java
                                │   └── QuoteDocumentQueryConfig.java
                                ├── integration
                                │   ├── LocalInvoiceCreationGateway.java
                                │   ├── LocalOrderCreationGateway.java
                                │   ├── LocalQuoteCustomerAdapter.java
                                │   ├── LocalQuoteProductCatalogAdapter.java
                                │   ├── LocalQuoteRejectionNotificationGateway.java
                                │   ├── LocalQuoteWarehouseAdapter.java
                                │   ├── QuoteActorIdentityAdapter.java
                                │   └── QuoteConversionNotificationAdapter.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── JpaQuoteRepositoryAdapter.java
                                │   ├── entities
                                │   │   ├── QuoteAllocationJpaEntity.java
                                │   │   ├── QuoteJpaEntity.java
                                │   │   ├── QuoteLineCategorySnapshotJpaEntity.java
                                │   │   ├── QuoteLineJpaEntity.java
                                │   │   └── QuoteNumberSequenceJpaEntity.java
                                │   ├── mappers
                                │   │   └── QuoteJpaMapper.java
                                │   └── repositories
                                │       ├── QuoteJpaRepository.java
                                │       └── QuoteNumberSequenceJpaRepository.java
                                └── web
                                    └── QuoteController.java
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
                            │   ├── customer_debt
                            │   │   ├── GetCustomerDebtUseCase.java
                            │   │   └── SearchCustomerDebtUseCase.java
                            │   ├── customer_invoices
                            │   │   └── GetCustomerInvoicesUseCase.java
                            │   ├── dto
                            │   │   └── response
                            │   │       ├── CustomerDebtListResponse.java
                            │   │       ├── CustomerDebtSummaryResponse.java
                            │   │       ├── CustomerInvoiceDebtResponse.java
                            │   │       ├── PendingPaymentResponse.java
                            │   │       └── ReceivablesSummaryResponse.java
                            │   ├── payment_history
                            │   │   └── GetPaymentHistoryUseCase.java
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
                            │   └── summary
                            │       └── GetReceivablesSummaryUseCase.java
                            └── infrastructure
                                ├── config
                                │   ├── AccountsReceivableReportQueryConfig.java
                                │   └── ReceivablesConfig.java
                                ├── integration
                                │   └── LocalReceivablesCustomerProfileGatewayAdapter.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── ReceivablesRepositoryAdapter.java
                                │   └── repositories
                                │       └── ReceivablesJpaRepository.java
                                └── web
                                    └── ReceivablesController.java
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
                            │   │   ├── generate_accounts_receivable_report_pdf
                            │   │   │   ├── GenerateAccountsReceivableReportPdfUseCase.java
                            │   │   │   └── GenerateAccountsReceivableReportPdfUseCaseImpl.java
                            │   │   ├── generate_collections_report_pdf
                            │   │   │   ├── GenerateCollectionsReportPdfUseCase.java
                            │   │   │   └── GenerateCollectionsReportPdfUseCaseImpl.java
                            │   │   ├── generate_payment_history_report_pdf
                            │   │   │   ├── GeneratePaymentHistoryReportPdfUseCase.java
                            │   │   │   └── GeneratePaymentHistoryReportPdfUseCaseImpl.java
                            │   │   ├── generate_payment_pdf
                            │   │   │   ├── GeneratePaymentPdfUseCase.java
                            │   │   │   └── GeneratePaymentPdfUseCaseImpl.java
                            │   │   └── ports
                            │   │       └── out
                            │   │           ├── AccountsReceivableQueryGateway.java
                            │   │           ├── AccountsReceivableReportPdfPort.java
                            │   │           ├── CollectionsReportPdfPort.java
                            │   │           ├── PaymentHistoryReportPdfPort.java
                            │   │           ├── PaymentPdfPort.java
                            │   │           └── PaymentReportQueryGateway.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   └── FinancesReportConfig.java
                            │       ├── integration
                            │       │   ├── PaymentDocumentQueryAdapter.java
                            │       │   └── ReceivablesReportQueryAdapter.java
                            │       ├── pdf
                            │       │   ├── AccountsReceivableReportPdfGenerator.java
                            │       │   ├── CollectionsReportPdfGenerator.java
                            │       │   ├── PaymentHistoryReportPdfGenerator.java
                            │       │   └── PaymentPdfGenerator.java
                            │       └── web
                            │           └── FinancesReportController.java
                            ├── infrastructure
                            │   └── pdf
                            │       ├── PdfBranding.java
                            │       └── PdfStatusColors.java
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
                            │   │   ├── generate_catalog_pdf
                            │   │   │   ├── GenerateCatalogPdfUseCase.java
                            │   │   │   └── GenerateCatalogPdfUseCaseImpl.java
                            │   │   ├── generate_commercial_catalog_pdf
                            │   │   │   ├── GenerateCommercialCatalogPdfUseCase.java
                            │   │   │   └── GenerateCommercialCatalogPdfUseCaseImpl.java
                            │   │   ├── get_catalog_report
                            │   │   │   ├── GetCatalogReportUseCase.java
                            │   │   │   └── GetCatalogReportUseCaseImpl.java
                            │   │   └── ports
                            │   │       └── out
                            │   │           ├── CatalogPdfPort.java
                            │   │           ├── CatalogReportRepositoryPort.java
                            │   │           ├── CommercialCatalogPdfPort.java
                            │   │           ├── CommercialCatalogRepositoryPort.java
                            │   │           └── MediaGateway.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   └── InventoryReportConfig.java
                            │       ├── integration
                            │       │   └── FileStorageMediaAdapter.java
                            │       ├── pdf
                            │       │   ├── CatalogPdfGenerator.java
                            │       │   └── CommercialCatalogPdfGenerator.java
                            │       ├── persistence
                            │       │   ├── adapters
                            │       │   │   ├── CatalogReportRepositoryAdapter.java
                            │       │   │   └── CommercialCatalogRepositoryAdapter.java
                            │       │   ├── projections
                            │       │   │   ├── CatalogReportProjection.java
                            │       │   │   └── CommercialCatalogProjection.java
                            │       │   └── repositories
                            │       │       ├── CatalogReportJpaRepository.java
                            │       │       └── CommercialCatalogJpaRepository.java
                            │       └── web
                            │           ├── CatalogReportController.java
                            │           └── CommercialCatalogController.java
                            ├── invoices
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── InvoiceReportData.java
                            │   │   │   ├── InvoiceReportFilter.java
                            │   │   │   └── InvoiceReportRow.java
                            │   │   ├── generate_invoice_pdf
                            │   │   │   ├── GenerateInvoicePdfUseCase.java
                            │   │   │   └── GenerateInvoicePdfUseCaseImpl.java
                            │   │   ├── generate_invoices_report_pdf
                            │   │   │   ├── GenerateInvoicesReportPdfUseCase.java
                            │   │   │   └── GenerateInvoicesReportPdfUseCaseImpl.java
                            │   │   └── ports
                            │   │       └── out
                            │   │           ├── InvoicePdfPort.java
                            │   │           ├── InvoiceReportQueryGateway.java
                            │   │           └── InvoicesReportPdfPort.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   └── InvoicesReportConfig.java
                            │       ├── integration
                            │       │   └── InvoiceDocumentQueryAdapter.java
                            │       ├── pdf
                            │       │   ├── InvoicePdfGenerator.java
                            │       │   └── InvoicesReportPdfGenerator.java
                            │       └── web
                            │           └── InvoicesReportController.java
                            ├── movements
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── query
                            │   │   │   │   └── MovementsPdfQuery.java
                            │   │   │   └── response
                            │   │   │       └── MovementReportResponse.java
                            │   │   ├── generate_movements_excel
                            │   │   │   ├── GenerateMovementsExcelUseCase.java
                            │   │   │   └── GenerateMovementsExcelUseCaseImpl.java
                            │   │   ├── generate_movements_pdf
                            │   │   │   ├── GenerateMovementsPdfUseCase.java
                            │   │   │   └── GenerateMovementsPdfUseCaseImpl.java
                            │   │   └── ports
                            │   │       └── out
                            │   │           ├── MovementsExcelPort.java
                            │   │           ├── MovementsPdfPort.java
                            │   │           └── MovementsReportRepositoryPort.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   └── MovementsReportConfig.java
                            │       ├── excel
                            │       │   └── MovementsExcelGenerator.java
                            │       ├── pdf
                            │       │   └── MovementsPdfGenerator.java
                            │       ├── persistence
                            │       │   ├── adapters
                            │       │   │   └── MovementsReportRepositoryAdapter.java
                            │       │   ├── projections
                            │       │   │   └── MovementsReportProjection.java
                            │       │   └── repositories
                            │       │       └── MovementsReportJpaRepository.java
                            │       └── web
                            │           └── MovementsReportController.java
                            ├── orders
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── OrderReportData.java
                            │   │   │   ├── OrderReportFilter.java
                            │   │   │   └── OrderReportRow.java
                            │   │   ├── generate_order_pdf
                            │   │   │   ├── GenerateOrderPdfUseCase.java
                            │   │   │   └── GenerateOrderPdfUseCaseImpl.java
                            │   │   ├── generate_orders_report_pdf
                            │   │   │   ├── GenerateOrdersReportPdfUseCase.java
                            │   │   │   └── GenerateOrdersReportPdfUseCaseImpl.java
                            │   │   └── ports
                            │   │       └── out
                            │   │           ├── OrderPdfPort.java
                            │   │           ├── OrderReportQueryGateway.java
                            │   │           └── OrdersReportPdfPort.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   └── OrdersReportConfig.java
                            │       ├── integration
                            │       │   └── OrderDocumentQueryAdapter.java
                            │       ├── pdf
                            │       │   ├── OrderPdfGenerator.java
                            │       │   └── OrdersReportPdfGenerator.java
                            │       └── web
                            │           └── OrdersReportController.java
                            ├── purchases
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── PurchaseReportData.java
                            │   │   │   ├── PurchaseReportFilter.java
                            │   │   │   └── PurchaseReportRow.java
                            │   │   ├── generate_purchase_pdf
                            │   │   │   ├── GeneratePurchasePdfUseCase.java
                            │   │   │   └── GeneratePurchasePdfUseCaseImpl.java
                            │   │   ├── generate_purchases_report_pdf
                            │   │   │   ├── GeneratePurchasesReportPdfUseCase.java
                            │   │   │   └── GeneratePurchasesReportPdfUseCaseImpl.java
                            │   │   └── ports
                            │   │       └── out
                            │   │           ├── PurchasePdfPort.java
                            │   │           ├── PurchaseReportQueryGateway.java
                            │   │           └── PurchasesReportPdfPort.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   └── PurchasesReportConfig.java
                            │       ├── integration
                            │       │   └── PurchaseDocumentQueryAdapter.java
                            │       ├── pdf
                            │       │   ├── PurchasePdfGenerator.java
                            │       │   └── PurchasesReportPdfGenerator.java
                            │       └── web
                            │           └── PurchasesReportController.java
                            ├── quotes
                            │   ├── application
                            │   │   ├── dto
                            │   │   │   ├── QuoteReportData.java
                            │   │   │   ├── QuoteReportFilter.java
                            │   │   │   └── QuoteReportRow.java
                            │   │   ├── generate_quote_pdf
                            │   │   │   ├── GenerateQuotePdfUseCase.java
                            │   │   │   └── GenerateQuotePdfUseCaseImpl.java
                            │   │   ├── generate_quotes_report_pdf
                            │   │   │   ├── GenerateQuotesReportPdfUseCase.java
                            │   │   │   └── GenerateQuotesReportPdfUseCaseImpl.java
                            │   │   └── ports
                            │   │       └── out
                            │   │           ├── QuotePdfPort.java
                            │   │           ├── QuoteReportQueryGateway.java
                            │   │           └── QuotesReportPdfPort.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   └── QuotesReportConfig.java
                            │       ├── integration
                            │       │   └── QuoteDocumentQueryAdapter.java
                            │       ├── pdf
                            │       │   ├── QuotePdfGenerator.java
                            │       │   └── QuotesReportPdfGenerator.java
                            │       └── web
                            │           └── QuotesReportController.java
                            └── stock
                                ├── application
                                │   ├── dto
                                │   │   ├── query
                                │   │   │   └── StockPdfQuery.java
                                │   │   └── response
                                │   │       └── StockReportResponse.java
                                │   ├── generate_stock_excel
                                │   │   ├── GenerateStockExcelUseCase.java
                                │   │   └── GenerateStockExcelUseCaseImpl.java
                                │   ├── generate_stock_pdf
                                │   │   ├── GenerateStockPdfUseCase.java
                                │   │   └── GenerateStockPdfUseCaseImpl.java
                                │   └── ports
                                │       └── out
                                │           ├── StockExcelPort.java
                                │           ├── StockPdfPort.java
                                │           └── StockReportRepositoryPort.java
                                └── infrastructure
                                    ├── config
                                    │   └── StockReportConfig.java
                                    ├── excel
                                    │   └── StockExcelGenerator.java
                                    ├── pdf
                                    │   └── StockPdfGenerator.java
                                    ├── persistence
                                    │   ├── adapters
                                    │   │   └── StockReportRepositoryAdapter.java
                                    │   ├── projections
                                    │   │   └── StockReportProjection.java
                                    │   └── repositories
                                    │       └── StockReportJpaRepository.java
                                    └── web
                                        └── StockReportController.java
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
                            │   ├── create
                            │   │   ├── CreateReturnRequest.java
                            │   │   └── CreateReturnUseCase.java
                            │   ├── delete
                            │   │   └── DeleteReturnUseCase.java
                            │   ├── dto
                            │   │   └── response
                            │   │       ├── ReturnListResponse.java
                            │   │       └── ReturnResponse.java
                            │   ├── get
                            │   │   └── GetReturnByIdUseCase.java
                            │   ├── list
                            │   │   └── GetAllReturnsUseCase.java
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
                            │   ├── queries
                            │   │   └── InvoiceReturnDetailsQueryService.java
                            │   └── services
                            │       └── ReturnResponseAssembler.java
                            ├── domain
                            │   ├── CancelledAllocation.java
                            │   ├── Return.java
                            │   ├── ReturnId.java
                            │   ├── ReturnLine.java
                            │   ├── ReturnLineAllocation.java
                            │   ├── ReturnNumber.java
                            │   ├── ReturnRepository.java
                            │   ├── ReturnStatus.java
                            │   └── ReturnType.java
                            └── infrastructure
                                ├── config
                                │   └── ReturnConfig.java
                                ├── integration
                                │   ├── InvoiceReturnAdjustmentAdapter.java
                                │   ├── LocalReturnBalanceGateway.java
                                │   ├── LocalReturnCashGateway.java
                                │   ├── LocalReturnInventoryGatewayAdapter.java
                                │   ├── LocalReturnInvoiceQueryGateway.java
                                │   ├── LocalReturnPaymentSettlementGateway.java
                                │   └── LocalReturnProjectionQueryGateway.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── JpaReturnRepository.java
                                │   ├── entities
                                │   │   ├── ReturnCancelledAllocationEntity.java
                                │   │   ├── ReturnEntity.java
                                │   │   ├── ReturnLineAllocationEntity.java
                                │   │   ├── ReturnLineEntity.java
                                │   │   └── ReturnNumberSequenceEntity.java
                                │   ├── queries
                                │   │   └── JpaReturnsProductReferenceQuery.java
                                │   └── repositories
                                │       └── SpringDataReturnsProductReferenceRepository.java
                                └── web
                                    └── ReturnController.java
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
                                    ├── adapters
                                    │   ├── LocalFileStorageService.java
                                    │   └── S3FileStorageService.java
                                    ├── config
                                    │   └── StorageConfig.java
                                    └── web
                                        └── FileController.java
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
                            │   ├── delete
                            │   │   └── DeleteSupplierUseCase.java
                            │   ├── dto
                            │   │   ├── request
                            │   │   │   ├── AddressRequest.java
                            │   │   │   ├── EmailRequest.java
                            │   │   │   └── PhoneRequest.java
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
                            │   ├── queries
                            │   │   ├── GetSupplierUseCase.java
                            │   │   └── ListSuppliersUseCase.java
                            │   ├── register
                            │   │   ├── CreateSupplierRequest.java
                            │   │   └── RegisterSupplierUseCase.java
                            │   └── update
                            │       ├── UpdateSupplierRequest.java
                            │       └── UpdateSupplierUseCase.java
                            ├── domain
                            │   ├── services
                            │   │   └── SupplierCodeGenerator.java
                            │   ├── valueobjects
                            │   │   ├── AddressId.java
                            │   │   ├── EmailAddress.java
                            │   │   ├── EmailId.java
                            │   │   ├── LegalName.java
                            │   │   ├── PhoneId.java
                            │   │   ├── PhoneNumber.java
                            │   │   ├── Rif.java
                            │   │   ├── SupplierAlias.java
                            │   │   ├── SupplierCode.java
                            │   │   ├── SupplierId.java
                            │   │   └── SupplierType.java
                            │   ├── Supplier.java
                            │   ├── SupplierAddress.java
                            │   ├── SupplierEmail.java
                            │   ├── SupplierPhone.java
                            │   └── SupplierRepository.java
                            └── infrastructure
                                ├── config
                                │   ├── PurchaseSupplierQueryConfig.java
                                │   └── SupplierConfig.java
                                ├── integration
                                │   ├── LocalPurchaseUsageGatewayAdapter.java
                                │   └── SupplierPurchaseDirectoryAdapter.java
                                ├── persistence
                                │   ├── adapters
                                │   │   └── JpaSupplierRepository.java
                                │   ├── entities
                                │   │   ├── SupplierAddressEntity.java
                                │   │   ├── SupplierEmailEntity.java
                                │   │   ├── SupplierEntity.java
                                │   │   └── SupplierPhoneEntity.java
                                │   └── repositories
                                │       └── SpringDataSupplierRepository.java
                                ├── services
                                │   └── SequentialSupplierCodeGenerator.java
                                └── web
                                    └── SupplierController.java
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
                            ├── infrastructure
                            │   └── config
                            │       └── WarehouseConfig.java
                            ├── management
                            │   ├── application
                            │   │   ├── create
                            │   │   │   ├── CreateWarehouseRequest.java
                            │   │   │   ├── CreateWarehouseUseCase.java
                            │   │   │   └── DuplicateWarehouseCodeException.java
                            │   │   ├── delete
                            │   │   │   ├── DeleteWarehouseUseCase.java
                            │   │   │   └── WarehouseHasActiveStockException.java
                            │   │   ├── dto
                            │   │   │   ├── WarehouseDetailResponse.java
                            │   │   │   └── WarehouseListResponse.java
                            │   │   ├── exceptions
                            │   │   │   ├── WarehouseNotActiveException.java
                            │   │   │   └── WarehouseNotFoundException.java
                            │   │   ├── ports
                            │   │   │   └── in
                            │   │   │       ├── WarehouseDirectoryEntry.java
                            │   │   │       └── WarehouseDirectoryQuery.java
                            │   │   ├── queries
                            │   │   │   ├── GetWarehouseUseCase.java
                            │   │   │   ├── ListWarehousesUseCase.java
                            │   │   │   └── RepositoryWarehouseDirectoryQuery.java
                            │   │   └── update
                            │   │       ├── UpdateWarehouseRequest.java
                            │   │       └── UpdateWarehouseUseCase.java
                            │   ├── domain
                            │   │   ├── Warehouse.java
                            │   │   └── WarehouseRepository.java
                            │   └── infrastructure
                            │       ├── config
                            │       │   └── WarehouseDirectoryConfig.java
                            │       ├── persistence
                            │       │   ├── adapters
                            │       │   │   └── JpaWarehouseRepository.java
                            │       │   ├── entities
                            │       │   │   └── WarehouseEntity.java
                            │       │   └── repositories
                            │       │       └── SpringDataWarehouseRepository.java
                            │       └── web
                            │           └── WarehouseController.java
                            └── stock
                                ├── application
                                │   ├── adjustment
                                │   │   ├── StockAdjustmentRequest.java
                                │   │   └── StockAdjustmentUseCase.java
                                │   ├── deduction
                                │   │   └── StockDeductionService.java
                                │   ├── dto
                                │   │   ├── StockBatchItem.java
                                │   │   └── WarehouseStockResponse.java
                                │   ├── entry
                                │   │   ├── StockEntryRequest.java
                                │   │   └── StockEntryUseCase.java
                                │   ├── exceptions
                                │   │   ├── InsufficientStockException.java
                                │   │   └── ProductNotInWarehouseException.java
                                │   ├── exit
                                │   │   ├── StockExitRequest.java
                                │   │   └── StockExitUseCase.java
                                │   ├── invoices
                                │   │   └── InvoiceInventoryService.java
                                │   ├── movements
                                │   │   ├── GetStockMovementsUseCase.java
                                │   │   └── StockMovementResponse.java
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
                                │   │   │   └── StockDeductionApi.java
                                │   │   └── out
                                │   │       ├── CustomerWarehouseAccessGateway.java
                                │   │       └── StockCatalogGateway.java
                                │   ├── purchases
                                │   │   └── PurchaseInventoryService.java
                                │   ├── queries
                                │   │   ├── GetWarehouseStockUseCase.java
                                │   │   ├── RepositoryProductStockQuery.java
                                │   │   ├── RepositoryPurchaseWarehouseQuery.java
                                │   │   └── StockAvailabilityQueryService.java
                                │   ├── returns
                                │   │   └── ReturnInventoryService.java
                                │   └── transfer
                                │       ├── TransferStockRequest.java
                                │       └── TransferStockUseCase.java
                                ├── domain
                                │   ├── MovementType.java
                                │   ├── ReferenceType.java
                                │   ├── StockMovement.java
                                │   ├── StockMovementRepository.java
                                │   ├── WarehouseStock.java
                                │   └── WarehouseStockRepository.java
                                └── infrastructure
                                    ├── config
                                    │   ├── ProductStockQueryConfig.java
                                    │   ├── PurchaseInventoryConfig.java
                                    │   ├── PurchaseWarehouseQueryConfig.java
                                    │   ├── ReturnInventoryConfig.java
                                    │   ├── StockAvailabilityQueryConfig.java
                                    │   └── StockDeductionConfig.java
                                    ├── integration
                                    │   ├── CustomerWarehouseAccessAdapter.java
                                    │   └── LocalStockCatalogGatewayAdapter.java
                                    ├── persistence
                                    │   ├── adapters
                                    │   │   ├── JpaStockMovementRepository.java
                                    │   │   └── JpaWarehouseStockRepository.java
                                    │   ├── entities
                                    │   │   ├── StockMovementEntity.java
                                    │   │   └── WarehouseStockEntity.java
                                    │   ├── queries
                                    │   │   ├── JpaAvailableStockProductsQuery.java
                                    │   │   └── JpaWarehousesProductReferenceQuery.java
                                    │   └── repositories
                                    │       ├── SpringDataAvailableStockProductsRepository.java
                                    │       ├── SpringDataStockMovementRepository.java
                                    │       ├── SpringDataWarehouseStockRepository.java
                                    │       └── SpringDataWarehousesProductReferenceRepository.java
                                    └── web
                                        ├── StockMovementController.java
                                        └── WarehouseStockController.java
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
        │               │   ├── application
        │               │   │   └── architecture
        │               │   │       └── RefreshTokenBoundaryTest.java
        │               │   ├── authentication
        │               │   │   ├── application
        │               │   │   │   ├── exceptions
        │               │   │   │   │   └── InvalidCredentialsExceptionUnitTest.java
        │               │   │   │   └── login
        │               │   │   │       ├── validation
        │               │   │   │       │   └── LoginRequestUnitTest.java
        │               │   │   │       ├── LoginRequestUnitTest.java
        │               │   │   │       └── LoginUserUseCaseTest.java
        │               │   │   ├── domain
        │               │   │   │   └── services
        │               │   │   │       └── AuthenticationDomainServiceUnitTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── security
        │               │   │       │   ├── JwtAuthenticationEntryPointUnitTest.java
        │               │   │       │   └── JwtAuthenticationFilterUnitTest.java
        │               │   │       └── web
        │               │   │           └── AuthControllerUnitTest.java
        │               │   ├── token
        │               │   │   ├── application
        │               │   │   │   ├── exceptions
        │               │   │   │   │   ├── InvalidTokenExceptionUnitTest.java
        │               │   │   │   │   └── TokenExpiredExceptionUnitTest.java
        │               │   │   │   ├── logout
        │               │   │   │   │   ├── validation
        │               │   │   │   │   │   └── LogoutRequestUnitTest.java
        │               │   │   │   │   ├── LogoutRequestUnitTest.java
        │               │   │   │   │   └── LogoutUserUseCaseUnitTest.java
        │               │   │   │   └── refresh
        │               │   │   │       ├── validation
        │               │   │   │       │   └── RefreshTokenRequestUnitTest.java
        │               │   │   │       ├── RefreshTokenRequestUnitTest.java
        │               │   │   │       └── RefreshTokenUseCaseUnitTest.java
        │               │   │   ├── domain
        │               │   │   │   ├── valueobjects
        │               │   │   │   │   └── RefreshTokenIdUnitTest.java
        │               │   │   │   └── RefreshTokenUnitTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── config
        │               │   │       │   └── JwtTokenServiceUnitTest.java
        │               │   │       ├── integration
        │               │   │       │   └── LocalAuthenticableSubjectGatewayTest.java
        │               │   │       └── persistence
        │               │   │           ├── adapters
        │               │   │           │   └── JpaRefreshTokenRepositoryUnitTest.java
        │               │   │           └── entities
        │               │   │               └── RefreshTokenEntityUnitTest.java
        │               │   └── user
        │               │       ├── application
        │               │       │   ├── dto
        │               │       │   │   └── response
        │               │       │   │       └── UserResponseUnitTest.java
        │               │       │   ├── email
        │               │       │   │   └── UpdateUserEmailUseCaseTest.java
        │               │       │   ├── exceptions
        │               │       │   │   ├── DuplicateEmailExceptionUnitTest.java
        │               │       │   │   └── UserNotFoundExceptionUnitTest.java
        │               │       │   ├── mappers
        │               │       │   │   └── UserMapperUnitTest.java
        │               │       │   ├── password
        │               │       │   │   └── UpdateUserPasswordUseCaseTest.java
        │               │       │   ├── permissions
        │               │       │   │   └── UpdateUserModulePermissionsUseCaseTest.java
        │               │       │   ├── queries
        │               │       │   │   ├── GetAllUsersUseCaseTest.java
        │               │       │   │   ├── GetUserByIdUseCaseUnitTest.java
        │               │       │   │   ├── RepositoryActorIdentityQueryTest.java
        │               │       │   │   └── RepositoryNotificationRecipientsQueryTest.java
        │               │       │   └── register
        │               │       │       ├── RegisterUserRequestUnitTest.java
        │               │       │       └── RegisterUserUseCaseUnitTest.java
        │               │       ├── domain
        │               │       │   ├── valueobjects
        │               │       │   │   ├── BusinessModuleUnitTest.java
        │               │       │   │   ├── EmailUnitTest.java
        │               │       │   │   ├── PasswordUnitTest.java
        │               │       │   │   ├── UserIdUnitTest.java
        │               │       │   │   ├── UserNameUnitTest.java
        │               │       │   │   └── UserRoleUnitTest.java
        │               │       │   └── UserUnitTest.java
        │               │       └── infrastructure
        │               │           ├── config
        │               │           │   └── BcryptPasswordEncoderUnitTest.java
        │               │           └── persistence
        │               │               ├── adapters
        │               │               │   └── JpaUserRepositoryUnitTest.java
        │               │               └── UserEntityUnitTest.java.bak
        │               ├── cash_register
        │               │   ├── application
        │               │   │   └── architecture
        │               │   │       └── CashRegisterBoundaryTest.java
        │               │   ├── infrastructure
        │               │   │   ├── config
        │               │   │   │   └── CashRegisterConfigTest.java
        │               │   │   └── persistence
        │               │   │       └── adapters
        │               │   │           ├── CashRegisterRepositoryAdapterSessionTotalsTest.java
        │               │   │           └── CashRegisterRepositoryAdapterTest.java
        │               │   ├── reporting
        │               │   │   ├── application
        │               │   │   │   ├── dto
        │               │   │   │   │   └── CashRegisterReportFilterTest.java
        │               │   │   │   ├── excel
        │               │   │   │   │   └── GenerateCashRegisterExcelUseCaseImplTest.java
        │               │   │   │   ├── range
        │               │   │   │   │   └── GenerateCashRegisterRangeReportUseCaseTest.java
        │               │   │   │   └── session
        │               │   │   │       └── GenerateCashRegisterReportUseCaseTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── excel
        │               │   │       │   ├── CashRegisterExcelExportServiceUnitTest.java
        │               │   │       │   ├── CashRegisterExcelGeneratorTest.java
        │               │   │       │   └── VerifyCellsTest.java
        │               │   │       ├── report
        │               │   │       │   ├── CashRegisterRangeReportGeneratorTest.java
        │               │   │       │   └── CashRegisterSessionReportGeneratorTest.java
        │               │   │       └── web
        │               │   │           └── CashRegisterExportControllerTest.java
        │               │   └── sessions
        │               │       ├── application
        │               │       │   ├── close
        │               │       │   │   └── CloseRegisterUseCaseUnitTest.java
        │               │       │   ├── dto
        │               │       │   │   ├── CashRegisterMapperTest.java
        │               │       │   │   ├── CashRegisterMetricsTest.java
        │               │       │   │   └── DailyTotalsTest.java
        │               │       │   ├── movements
        │               │       │   │   ├── CashMovementServiceTest.java
        │               │       │   │   └── RecordCashMovementUseCaseTest.java
        │               │       │   ├── open
        │               │       │   │   └── OpenRegisterUseCaseUnitTest.java
        │               │       │   └── queries
        │               │       │       ├── GetActiveRegisterUseCaseTest.java
        │               │       │       └── GetRegisterHistoryUseCaseUnitTest.java
        │               │       ├── domain
        │               │       │   ├── valueobjects
        │               │       │   │   ├── RegisterIdUnitTest.java
        │               │       │   │   └── RegisterStatusUnitTest.java
        │               │       │   ├── CashMovementUnitTest.java
        │               │       │   ├── CashRegisterDetailTest.java
        │               │       │   └── CashRegisterUnitTest.java
        │               │       └── infrastructure
        │               │           ├── integration
        │               │           │   ├── LocalCreditNoteCashApiTest.java
        │               │           │   └── LocalDebitNoteCashApiTest.java
        │               │           ├── persistence
        │               │           │   └── queries
        │               │           │       └── JpaPaymentMethodUsageQueryTest.java
        │               │           └── web
        │               │               └── CashRegisterControllerTest.java
        │               ├── categories
        │               │   ├── application
        │               │   │   ├── create
        │               │   │   │   └── CreateCategoryUseCaseTest.java
        │               │   │   ├── delete
        │               │   │   │   └── DeleteCategoryUseCaseTest.java
        │               │   │   ├── exceptions
        │               │   │   │   └── CategoryNotFoundExceptionTest.java
        │               │   │   ├── mappers
        │               │   │   │   └── CategoryMapperTest.java
        │               │   │   ├── queries
        │               │   │   │   ├── GetCategoryUseCaseTest.java
        │               │   │   │   └── ListCategoriesUseCaseTest.java
        │               │   │   └── update
        │               │   │       └── UpdateCategoryUseCaseTest.java
        │               │   ├── domain
        │               │   │   ├── CatalogUsageBoundaryTest.java
        │               │   │   └── CategoryUnitTest.java
        │               │   └── infrastructure
        │               │       ├── config
        │               │       │   └── CategoryConfigTest.java
        │               │       ├── persistence
        │               │       │   └── adapters
        │               │       │       └── JpaCategoryRepositoryTest.java
        │               │       └── web
        │               │           └── CategoryControllerTest.java
        │               ├── config
        │               │   ├── DatabaseCleaner.java
        │               │   └── H2DialectWithoutEngineSupport.java
        │               ├── credit_notes
        │               │   ├── application
        │               │   │   ├── architecture
        │               │   │   │   └── CreditNoteModuleBoundaryTest.java
        │               │   │   ├── create
        │               │   │   │   └── CreateCreditNoteUseCaseTest.java
        │               │   │   ├── delete
        │               │   │   │   └── DeleteCreditNoteUseCaseTest.java
        │               │   │   ├── get
        │               │   │   │   └── GetCreditNoteByIdUseCaseTest.java
        │               │   │   ├── list
        │               │   │   │   └── GetCreditNotesUseCaseTest.java
        │               │   │   ├── mappers
        │               │   │   │   └── CreditNoteMapperTest.java
        │               │   │   └── queries
        │               │   │       └── RepositoryCreditNoteLedgerQueryTest.java
        │               │   ├── domain
        │               │   │   ├── valueobjects
        │               │   │   │   ├── CreditNoteIdUnitTest.java
        │               │   │   │   └── CreditNoteNumberUnitTest.java
        │               │   │   └── CreditNoteUnitTest.java
        │               │   └── infrastructure
        │               │       ├── integration
        │               │       │   ├── LocalCreditNoteEffectsGatewayTest.java
        │               │       │   ├── LocalCreditNoteFinancialPositionGatewayTest.java
        │               │       │   └── LocalCreditNoteInvoiceGatewayTest.java
        │               │       ├── persistence
        │               │       │   └── adapters
        │               │       │       └── CreditNoteRepositoryAdapterTest.java
        │               │       └── web
        │               │           └── CreditNoteControllerTest.java
        │               ├── customers
        │               │   ├── balance
        │               │   │   ├── application
        │               │   │   │   ├── add
        │               │   │   │   │   └── AddToCustomerBalanceUseCaseTest.java
        │               │   │   │   ├── consume
        │               │   │   │   │   └── ConsumeCustomerBalanceUseCaseTest.java
        │               │   │   │   ├── get
        │               │   │   │   │   └── GetCustomerBalanceUseCaseTest.java
        │               │   │   │   ├── mappers
        │               │   │   │   │   └── BalanceMapperTest.java
        │               │   │   │   ├── services
        │               │   │   │   │   ├── InvoiceBalanceConsumptionServiceTest.java
        │               │   │   │   │   └── ReturnBalanceMutationServiceTest.java
        │               │   │   │   └── CreateCustomerCashBalanceOperationUseCaseTest.java
        │               │   │   ├── domain
        │               │   │   │   ├── BalanceTransactionTest.java
        │               │   │   │   ├── CustomerBalanceBoundaryTest.java
        │               │   │   │   └── CustomerBalanceTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── integration
        │               │   │       │   ├── LocalCreditNoteBalanceApiTest.java
        │               │   │       │   ├── LocalCustomerActorGatewayTest.java
        │               │   │       │   └── LocalCustomerCashGatewayTest.java
        │               │   │       ├── persistence
        │               │   │       │   ├── adapters
        │               │   │       │   │   └── CustomerBalanceRepositoryAdapterTest.java
        │               │   │       │   └── mappers
        │               │   │       │       ├── BalanceTransactionJpaMapperTest.java
        │               │   │       │       └── CustomerBalanceJpaMapperTest.java
        │               │   │       └── web
        │               │   │           └── CustomerBalanceControllerTest.java
        │               │   ├── credit
        │               │   │   ├── application
        │               │   │   │   ├── exceptions
        │               │   │   │   │   └── CreditLimitExceededExceptionTest.java
        │               │   │   │   ├── overdue
        │               │   │   │   │   └── GetOverdueInvoicesUseCaseTest.java
        │               │   │   │   ├── queries
        │               │   │   │   │   └── RepositoryCustomerCreditContextQueryTest.java
        │               │   │   │   ├── services
        │               │   │   │   │   └── CustomerCreditEligibilityServiceTest.java
        │               │   │   │   ├── status
        │               │   │   │   │   └── GetCustomerCreditStatusUseCaseTest.java
        │               │   │   │   └── CustomerCreditContractTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── integration
        │               │   │       │   └── LocalCreditValidationGatewayTest.java
        │               │   │       └── web
        │               │   │           └── CustomerCreditControllerTest.java
        │               │   ├── profile
        │               │   │   ├── application
        │               │   │   │   ├── delete
        │               │   │   │   │   └── DeleteCustomerUseCaseTest.java
        │               │   │   │   ├── exceptions
        │               │   │   │   │   └── CustomerNotFoundExceptionTest.java
        │               │   │   │   ├── get
        │               │   │   │   │   └── GetCustomerUseCaseTest.java
        │               │   │   │   ├── list
        │               │   │   │   │   └── ListCustomersUseCaseTest.java
        │               │   │   │   ├── mappers
        │               │   │   │   │   └── CustomerMapperTest.java
        │               │   │   │   ├── register
        │               │   │   │   │   └── RegisterCustomerUseCaseTest.java
        │               │   │   │   └── update
        │               │   │   │       └── UpdateCustomerUseCaseTest.java
        │               │   │   ├── domain
        │               │   │   │   ├── valueobjects
        │               │   │   │   │   ├── AddressIdUnitTest.java
        │               │   │   │   │   ├── CreditDaysUnitTest.java
        │               │   │   │   │   ├── CreditLimitUnitTest.java
        │               │   │   │   │   ├── CustomerAliasUnitTest.java
        │               │   │   │   │   ├── CustomerCodeUnitTest.java
        │               │   │   │   │   ├── CustomerIdUnitTest.java
        │               │   │   │   │   ├── CustomerTypeUnitTest.java
        │               │   │   │   │   ├── EmailAddressUnitTest.java
        │               │   │   │   │   ├── EmailIdUnitTest.java
        │               │   │   │   │   ├── LegalNameUnitTest.java
        │               │   │   │   │   ├── PaymentTypeUnitTest.java
        │               │   │   │   │   ├── PhoneIdUnitTest.java
        │               │   │   │   │   ├── PhoneNumberUnitTest.java
        │               │   │   │   │   └── RifUnitTest.java
        │               │   │   │   ├── CustomerAddressUnitTest.java
        │               │   │   │   ├── CustomerEmailUnitTest.java
        │               │   │   │   ├── CustomerPhoneUnitTest.java
        │               │   │   │   └── CustomerUnitTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── persistence
        │               │   │       │   └── adapters
        │               │   │       │       └── JpaCustomerRepositoryTest.java
        │               │   │       └── web
        │               │   │           └── CustomerControllerTest.java
        │               │   ├── testing
        │               │   │   └── CustomerTestHelper.java
        │               │   └── warehouse_access
        │               │       ├── application
        │               │       │   ├── exceptions
        │               │       │   │   └── CustomerWarehouseAccessAlreadyExistsExceptionTest.java
        │               │       │   ├── grant
        │               │       │   │   └── GrantWarehouseAccessUseCaseTest.java
        │               │       │   ├── list
        │               │       │   │   └── ListCustomerWarehousesUseCaseTest.java
        │               │       │   ├── revoke
        │               │       │   │   └── RevokeWarehouseAccessUseCaseTest.java
        │               │       │   └── CustomerWarehouseBoundaryTest.java
        │               │       ├── domain
        │               │       │   └── CustomerWarehouseAccessUnitTest.java
        │               │       └── infrastructure
        │               │           ├── integration
        │               │           │   └── LocalWarehouseDirectoryGatewayTest.java
        │               │           ├── persistence
        │               │           │   └── adapters
        │               │           │       └── JpaCustomerWarehouseAccessRepositoryTest.java
        │               │           └── web
        │               │               └── CustomerWarehouseAccessControllerTest.java
        │               ├── dashboard
        │               │   ├── application
        │               │   │   ├── ports
        │               │   │   │   └── out
        │               │   │   │       └── DashboardQueryGatewayTest.java
        │               │   │   └── summary
        │               │   │       └── DashboardArchitectureTest.java
        │               │   └── infrastructure
        │               │       └── persistence
        │               │           └── queries
        │               │               └── JdbcDashboardQueryAdapterTest.java
        │               ├── debit_notes
        │               │   ├── application
        │               │   │   ├── architecture
        │               │   │   │   └── DebitNoteModuleBoundaryTest.java
        │               │   │   ├── create
        │               │   │   │   └── CreateDebitNoteUseCaseTest.java
        │               │   │   ├── delete
        │               │   │   │   └── DeleteDebitNoteUseCaseTest.java
        │               │   │   ├── get
        │               │   │   │   └── GetDebitNoteByIdUseCaseTest.java
        │               │   │   ├── list
        │               │   │   │   └── GetDebitNotesUseCaseTest.java
        │               │   │   ├── mappers
        │               │   │   │   └── DebitNoteMapperTest.java
        │               │   │   └── queries
        │               │   │       └── RepositoryDebitNoteLedgerQueryTest.java
        │               │   ├── domain
        │               │   │   ├── valueobjects
        │               │   │   │   ├── DebitNoteIdUnitTest.java
        │               │   │   │   └── DebitNoteNumberUnitTest.java
        │               │   │   └── DebitNoteUnitTest.java
        │               │   └── infrastructure
        │               │       ├── integration
        │               │       │   └── LocalDebitNoteInvoiceGatewayTest.java
        │               │       ├── persistence
        │               │       │   └── adapters
        │               │       │       └── DebitNoteRepositoryAdapterTest.java
        │               │       └── web
        │               │           └── DebitNoteControllerTest.java
        │               ├── infrastructure
        │               │   └── config
        │               │       └── GlobalExceptionHandlerUnitTest.java
        │               ├── invoices
        │               │   ├── application
        │               │   │   ├── architecture
        │               │   │   │   ├── operations
        │               │   │   │   │   └── InvoiceEditingBoundaryTest.java
        │               │   │   │   ├── CreditConsumerBoundaryTest.java
        │               │   │   │   ├── InvoiceAuthorizationBoundaryTest.java
        │               │   │   │   ├── InvoiceCreationBoundaryTest.java
        │               │   │   │   ├── InvoiceEditingBoundaryTest.java
        │               │   │   │   ├── InvoiceInventoryBoundaryTest.java
        │               │   │   │   └── StockReadBoundaryTest.java
        │               │   │   ├── cancel
        │               │   │   │   └── CancelInvoiceUseCaseUnitTest.java
        │               │   │   ├── confirm
        │               │   │   │   └── ConfirmInvoiceUseCaseTest.java
        │               │   │   ├── create
        │               │   │   │   └── CreateInvoiceUseCaseTest.java
        │               │   │   ├── delete
        │               │   │   │   └── DeleteInvoiceUseCaseTest.java
        │               │   │   ├── duplicate
        │               │   │   │   └── DuplicateInvoiceUseCaseTest.java
        │               │   │   ├── get
        │               │   │   │   └── GetInvoiceByIdUseCaseUnitTest.java
        │               │   │   ├── list
        │               │   │   │   └── GetInvoicesUseCaseTest.java
        │               │   │   ├── mappers
        │               │   │   │   └── InvoiceMapperTest.java
        │               │   │   ├── ports
        │               │   │   │   └── in
        │               │   │   │       └── InvoiceReturnCommandValidationTest.java
        │               │   │   ├── queries
        │               │   │   │   ├── RepositoryInvoiceCreditLedgerQueryTest.java
        │               │   │   │   └── RepositoryInvoiceReturnAdjustmentsTest.java
        │               │   │   ├── services
        │               │   │   │   └── InvoiceAuthorizationServiceTest.java
        │               │   │   ├── update
        │               │   │   │   └── UpdateInvoiceUseCaseTest.java
        │               │   │   └── usecases
        │               │   │       ├── ConfirmInvoiceUseCaseUnitTest.java.bak
        │               │   │       ├── CreateInvoiceUseCaseUnitTest.java.bak
        │               │   │       ├── DeleteInvoiceUseCaseUnitTest.java.bak
        │               │   │       ├── DuplicateInvoiceUseCaseUnitTest.java.bak
        │               │   │       ├── GetInvoicesUseCaseUnitTest.java.bak
        │               │   │       └── UpdateInvoiceUseCaseUnitTest.java.bak
        │               │   ├── domain
        │               │   │   ├── exceptions
        │               │   │   │   └── InvoiceNotFoundExceptionTest.java
        │               │   │   ├── valueobjects
        │               │   │   │   ├── InvoiceIdUnitTest.java
        │               │   │   │   ├── InvoiceLineIdUnitTest.java
        │               │   │   │   ├── InvoiceNumberUnitTest.java
        │               │   │   │   ├── InvoiceReturnAdjustmentTest.java
        │               │   │   │   ├── InvoiceStatusUnitTest.java
        │               │   │   │   └── PaymentTypeUnitTest.java
        │               │   │   ├── GetInvoiceByIdUseCaseNotesTest.java
        │               │   │   ├── InvoiceLineAllocationUnitTest.java
        │               │   │   ├── InvoiceLineUnitTest.java
        │               │   │   ├── InvoiceReturnBoundaryTest.java
        │               │   │   └── InvoiceUnitTest.java
        │               │   └── infrastructure
        │               │       ├── integration
        │               │       │   ├── CustomerCreditTermsContractTest.java
        │               │       │   ├── InvoiceAuthorizationWiringTest.java
        │               │       │   ├── LocalInventoryGatewayAdapterTest.java
        │               │       │   ├── LocalInvoiceFinancialPositionGatewayTest.java
        │               │       │   ├── LocalInvoiceNoteApiTest.java
        │               │       │   ├── LocalInvoiceNoteDetailsAdapterTest.java
        │               │       │   └── LocalInvoiceReturnDetailsAdapterTest.java
        │               │       ├── persistence
        │               │       │   ├── adapters
        │               │       │   │   └── InvoiceRepositoryAdapterTest.java
        │               │       │   └── entities
        │               │       │       └── InvoiceLineEntityUnitTest.java
        │               │       ├── services
        │               │       │   └── InvoiceStatusServiceImplTest.java
        │               │       └── web
        │               │           └── InvoiceControllerTest.java
        │               ├── notifications
        │               │   ├── application
        │               │   │   └── services
        │               │   │       └── NotificationServiceTest.java
        │               │   └── infrastructure
        │               │       └── integration
        │               │           └── AuthNotificationRecipientAdapterTest.java
        │               ├── orders
        │               │   ├── application
        │               │   │   ├── architecture
        │               │   │   │   ├── GetOrdersCategoryQueryBoundaryTest.java
        │               │   │   │   └── OrderInvoiceConversionBoundaryTest.java
        │               │   │   ├── cancel
        │               │   │   │   └── CancelOrderUseCaseUnitTest.java
        │               │   │   ├── confirm
        │               │   │   │   └── ConfirmOrderUseCaseUnitTest.java
        │               │   │   ├── convert_to_invoice
        │               │   │   │   └── ConvertToInvoiceUseCaseUnitTest.java
        │               │   │   ├── create
        │               │   │   │   └── CreateOrderUseCaseUnitTest.java
        │               │   │   ├── delete
        │               │   │   │   └── DeleteOrderUseCaseUnitTest.java
        │               │   │   ├── get
        │               │   │   │   └── GetOrderByIdUseCaseUnitTest.java
        │               │   │   ├── list
        │               │   │   │   └── GetOrdersUseCaseTest.java
        │               │   │   ├── mappers
        │               │   │   │   └── OrderMapperTest.java
        │               │   │   ├── products
        │               │   │   │   └── GetProductsForOrderUseCaseUnitTest.java
        │               │   │   ├── update
        │               │   │   │   └── UpdateOrderUseCaseUnitTest.java
        │               │   │   └── usecases
        │               │   │       └── GetOrdersUseCaseUnitTest.java.bak
        │               │   ├── domain
        │               │   │   ├── exceptions
        │               │   │   │   ├── OrderCannotBeDeletedExceptionTest.java
        │               │   │   │   ├── OrderNotFoundExceptionTest.java
        │               │   │   │   └── OrderNotPendingExceptionTest.java
        │               │   │   ├── valueobjects
        │               │   │   │   ├── OrderIdUnitTest.java
        │               │   │   │   ├── OrderNumberUnitTest.java
        │               │   │   │   └── OrderStatusUnitTest.java
        │               │   │   ├── OrderLineUnitTest.java
        │               │   │   └── OrderUnitTest.java
        │               │   └── infrastructure
        │               │       ├── config
        │               │       │   └── OrderConfigTest.java
        │               │       ├── integration
        │               │       │   └── InvoiceCreationContractTest.java
        │               │       ├── integration_tests
        │               │       │   └── OrderHistoricalCategoryMySqlIT.java
        │               │       ├── persistence
        │               │       │   ├── adapters
        │               │       │   │   └── OrderRepositoryAdapterTest.java
        │               │       │   └── OrderCategorySnapshotMappingTest.java
        │               │       └── web
        │               │           └── OrderControllerTest.java
        │               ├── payment_methods
        │               │   ├── application
        │               │   │   ├── architecture
        │               │   │   │   └── PaymentMethodBoundaryTest.java
        │               │   │   ├── create
        │               │   │   │   └── CreatePaymentMethodUseCaseTest.java
        │               │   │   ├── delete
        │               │   │   │   └── DeletePaymentMethodUseCaseTest.java
        │               │   │   ├── mappers
        │               │   │   │   └── PaymentMethodMapperTest.java
        │               │   │   ├── queries
        │               │   │   │   ├── GetPaymentMethodByIdUseCaseTest.java
        │               │   │   │   └── ListPaymentMethodsUseCaseTest.java
        │               │   │   ├── toggle
        │               │   │   │   └── TogglePaymentMethodUseCaseTest.java
        │               │   │   └── update
        │               │   │       └── UpdatePaymentMethodUseCaseTest.java
        │               │   ├── domain
        │               │   │   ├── valueobjects
        │               │   │   │   └── PaymentMethodIdUnitTest.java
        │               │   │   └── PaymentMethodUnitTest.java
        │               │   └── infrastructure
        │               │       ├── integration
        │               │       │   ├── LocalPaymentMethodUsageGatewayTest.java
        │               │       │   └── PaymentMethodUsageWiringTest.java
        │               │       ├── persistence
        │               │       │   └── adapters
        │               │       │       └── PaymentMethodRepositoryAdapterTest.java
        │               │       └── web
        │               │           └── PaymentMethodControllerTest.java
        │               ├── payments
        │               │   ├── application
        │               │   │   └── architecture
        │               │   │       ├── CustomerCreditQueryBoundaryTest.java
        │               │   │       └── PaymentModuleBoundaryTest.java
        │               │   ├── collection
        │               │   │   ├── application
        │               │   │   │   ├── approve
        │               │   │   │   │   └── ApprovePaymentUseCaseTest.java
        │               │   │   │   ├── create
        │               │   │   │   │   ├── CreatePaymentRequestTest.java
        │               │   │   │   │   └── CreatePaymentUseCaseUnitTest.java
        │               │   │   │   ├── delete
        │               │   │   │   │   └── DeletePaymentUseCaseTest.java
        │               │   │   │   ├── mappers
        │               │   │   │   │   └── PaymentMapperTest.java
        │               │   │   │   ├── pay_customer_debt
        │               │   │   │   │   └── PayAllCustomerDebtUseCaseTest.java
        │               │   │   │   ├── pay_invoice
        │               │   │   │   │   └── PayInvoiceUseCaseTest.java
        │               │   │   │   ├── pay_partial_debt
        │               │   │   │   │   └── PayPartialDebtUseCaseTest.java
        │               │   │   │   ├── queries
        │               │   │   │   │   ├── GetAllPaymentsUseCaseTest.java
        │               │   │   │   │   └── GetPaymentByIdUseCaseUnitTest.java
        │               │   │   │   ├── reject
        │               │   │   │   │   └── RejectPaymentUseCaseTest.java
        │               │   │   │   ├── services
        │               │   │   │   │   ├── CashInvoiceSettlementServiceTest.java
        │               │   │   │   │   └── ReturnPaymentOperationsServiceTest.java
        │               │   │   │   └── update
        │               │   │   │       └── UpdatePaymentUseCaseTest.java
        │               │   │   ├── domain
        │               │   │   │   ├── valueobjects
        │               │   │   │   │   ├── InvoiceRemainingBalanceUnitTest.java
        │               │   │   │   │   ├── PaymentIdUnitTest.java
        │               │   │   │   │   ├── PaymentNumberUnitTest.java
        │               │   │   │   │   ├── PaymentSourceTest.java
        │               │   │   │   │   └── PaymentStatusTest.java
        │               │   │   │   ├── PaymentAllocationServiceTest.java
        │               │   │   │   ├── PaymentAllocationTest.java
        │               │   │   │   └── PaymentUnitTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── integration
        │               │   │       │   ├── CreditLedgerWiringTest.java
        │               │   │       │   ├── LocalCreditLedgerGatewaysTest.java
        │               │   │       │   ├── LocalPaymentCashRegisterGatewayTest.java
        │               │   │       │   ├── LocalPaymentInvoiceGatewayTest.java
        │               │   │       │   └── RepositoryInvoicePaymentTotalQueryTest.java
        │               │   │       ├── persistence
        │               │   │       │   ├── adapters
        │               │   │       │   │   ├── PaymentAllocationRepositoryAdapterTest.java
        │               │   │       │   │   └── PaymentRepositoryAdapterTest.java
        │               │   │       │   └── queries
        │               │   │       │       └── JpaPaymentMethodUsageQueryTest.java
        │               │   │       └── web
        │               │   │           └── PaymentControllerTest.java
        │               │   ├── credit
        │               │   │   ├── application
        │               │   │   │   └── services
        │               │   │   │       └── CustomerCreditQueryServiceTest.java
        │               │   │   └── domain
        │               │   │       └── CustomerCreditPolicyTest.java
        │               │   └── infrastructure
        │               │       └── config
        │               │           └── PaymentConfigTest.java
        │               ├── price_types
        │               │   ├── application
        │               │   │   ├── create
        │               │   │   │   └── CreatePriceTypeUseCaseUnitTest.java
        │               │   │   ├── delete
        │               │   │   │   └── DeletePriceTypeUseCaseUnitTest.java
        │               │   │   ├── exceptions
        │               │   │   │   └── DuplicatePriceTypeNameExceptionTest.java
        │               │   │   ├── queries
        │               │   │   │   ├── GetPriceTypeUseCaseUnitTest.java
        │               │   │   │   └── ListPriceTypesUseCaseTest.java
        │               │   │   ├── update
        │               │   │   │   └── UpdatePriceTypeUseCaseUnitTest.java
        │               │   │   └── usecases
        │               │   │       └── ListPriceTypesUseCaseUnitTest.java.bak
        │               │   ├── domain
        │               │   │   ├── valueobjects
        │               │   │   │   └── ProductPriceUnitTest.java
        │               │   │   └── PriceTypeUnitTest.java
        │               │   └── infrastructure
        │               │       ├── persistence
        │               │       │   └── adapters
        │               │       │       └── JpaPriceTypeRepositoryTest.java
        │               │       └── web
        │               │           └── PriceTypeControllerTest.java
        │               ├── products
        │               │   ├── application
        │               │   │   ├── architecture
        │               │   │   │   ├── ProductCatalogBoundaryTest.java
        │               │   │   │   └── ProductStockBoundaryTest.java
        │               │   │   ├── create
        │               │   │   │   └── CreateProductUseCaseTest.java
        │               │   │   ├── delete
        │               │   │   │   └── DeleteProductUseCaseTest.java
        │               │   │   ├── exceptions
        │               │   │   │   ├── CompositeProductCannotBeComponentExceptionTest.java
        │               │   │   │   ├── DuplicateProductCodeExceptionTest.java
        │               │   │   │   ├── DuplicateProductComponentExceptionTest.java
        │               │   │   │   └── ProductNotFoundExceptionTest.java
        │               │   │   ├── mappers
        │               │   │   │   └── ProductMapperTest.java
        │               │   │   ├── photos
        │               │   │   │   ├── RemoveProductPhotoUseCaseTest.java
        │               │   │   │   └── UploadProductPhotoUseCaseTest.java
        │               │   │   ├── ports
        │               │   │   │   └── out
        │               │   │   │       └── ProductReferenceGatewayTest.java
        │               │   │   ├── queries
        │               │   │   │   ├── GetProductUseCaseTest.java
        │               │   │   │   └── ListProductsUseCaseTest.java
        │               │   │   ├── stock
        │               │   │   │   └── GetProductStockUseCaseTest.java
        │               │   │   ├── transactions
        │               │   │   │   └── GetProductsForTransactionUseCaseTest.java
        │               │   │   └── update
        │               │   │       └── UpdateProductUseCaseTest.java
        │               │   ├── domain
        │               │   │   ├── valueobjects
        │               │   │   │   └── ProductPriceUnitTest.java
        │               │   │   ├── ProductComponentUnitTest.java
        │               │   │   └── ProductUnitTest.java
        │               │   └── infrastructure
        │               │       ├── integration
        │               │       │   ├── LocalProductFileStorageGatewayAdapterTest.java
        │               │       │   ├── ProductCatalogGatewayIntegrationTest.java
        │               │       │   └── ProductStockGatewayIntegrationTest.java
        │               │       ├── integration_tests
        │               │       │   └── ProductPersistenceQueriesMySqlIT.java
        │               │       ├── persistence
        │               │       │   ├── adapters
        │               │       │   │   └── JpaProductRepositoryTest.java
        │               │       │   ├── queries
        │               │       │   │   └── JpaProductUsageQueriesTest.java
        │               │       │   └── repositories
        │               │       │       ├── ProductPriceMappingTest.java
        │               │       │       ├── ProductPricePersistenceBoundaryTest.java
        │               │       │       ├── ProductReferenceQueryBoundaryTest.java
        │               │       │       ├── ProductStockFilterTest.java
        │               │       │       └── ProductStockQueryBoundaryTest.java
        │               │       └── web
        │               │           └── ProductControllerTest.java
        │               ├── purchases
        │               │   ├── application
        │               │   │   ├── architecture
        │               │   │   │   ├── PurchaseIdentityCatalogBoundaryTest.java
        │               │   │   │   ├── PurchaseProductCatalogContractTest.java
        │               │   │   │   └── PurchaseWarehouseReadBoundaryTest.java
        │               │   │   ├── cancel
        │               │   │   │   └── CancelPurchaseUseCaseUnitTest.java
        │               │   │   ├── confirm
        │               │   │   │   └── ConfirmPurchaseUseCaseUnitTest.java
        │               │   │   ├── create
        │               │   │   │   └── CreatePurchaseUseCaseUnitTest.java
        │               │   │   ├── products
        │               │   │   │   └── GetProductsForPurchaseUseCaseTest.java
        │               │   │   ├── queries
        │               │   │   │   └── RepositorySupplierPurchaseUsageQueryTest.java
        │               │   │   └── services
        │               │   │       └── PurchaseDetailResponseAssemblerTest.java
        │               │   ├── domain
        │               │   │   ├── valueobjects
        │               │   │   │   ├── PurchaseIdUnitTest.java
        │               │   │   │   ├── PurchaseNumberUnitTest.java
        │               │   │   │   └── PurchaseStatusUnitTest.java
        │               │   │   ├── PurchaseLineAllocationUnitTest.java
        │               │   │   ├── PurchaseLineUnitTest.java
        │               │   │   ├── PurchaseRepositoryIntegrationTest.java
        │               │   │   ├── PurchaseUnitTest.java
        │               │   │   └── UpdatePurchaseCatalogTest.java
        │               │   └── infrastructure
        │               │       ├── integration
        │               │       │   └── LocalPurchaseSupplierGatewayAdapterTest.java
        │               │       └── web
        │               │           └── PurchaseControllerTest.java
        │               ├── quotes
        │               │   ├── application
        │               │   │   ├── architecture
        │               │   │   │   ├── QuoteCancellationBoundaryTest.java
        │               │   │   │   ├── QuoteInvoiceConversionBoundaryTest.java
        │               │   │   │   ├── QuoteModuleBoundaryTest.java
        │               │   │   │   └── QuoteOrderConversionBoundaryTest.java
        │               │   │   ├── assign_warehouses
        │               │   │   │   └── AssignQuoteWarehousesUseCaseTest.java
        │               │   │   ├── cancel
        │               │   │   │   └── CancelQuoteUseCaseTest.java
        │               │   │   ├── convert_to_invoice
        │               │   │   │   └── ConvertQuoteToInvoiceUseCaseTest.java
        │               │   │   ├── convert_to_order
        │               │   │   │   └── ConvertQuoteToOrderUseCaseTest.java
        │               │   │   ├── create
        │               │   │   │   └── CreateQuoteUseCaseTest.java
        │               │   │   ├── delete
        │               │   │   │   └── DeleteQuoteUseCaseTest.java
        │               │   │   ├── get
        │               │   │   │   └── GetQuoteByIdUseCaseTest.java
        │               │   │   ├── list
        │               │   │   │   └── GetQuotesUseCaseTest.java
        │               │   │   ├── mappers
        │               │   │   │   └── QuoteMapperTest.java
        │               │   │   ├── products
        │               │   │   │   └── GetProductsForQuoteUseCaseTest.java
        │               │   │   ├── update
        │               │   │   │   └── UpdateQuoteUseCaseTest.java
        │               │   │   └── warehouses
        │               │   │       └── GetWarehousesForQuoteUseCaseTest.java
        │               │   ├── domain
        │               │   │   ├── exceptions
        │               │   │   │   ├── InvalidQuoteAllocationExceptionTest.java
        │               │   │   │   ├── InvalidQuoteLineExceptionTest.java
        │               │   │   │   ├── QuoteNotFoundExceptionTest.java
        │               │   │   │   └── QuoteStateExceptionTest.java
        │               │   │   ├── valueobjects
        │               │   │   │   ├── QuoteAllocationIdTest.java
        │               │   │   │   ├── QuoteLineIdTest.java
        │               │   │   │   ├── QuoteNumberTest.java
        │               │   │   │   └── QuoteStatusTest.java
        │               │   │   ├── QuoteAllocationTest.java
        │               │   │   ├── QuoteCategorySnapshotMappingTest.java
        │               │   │   ├── QuoteLineTest.java
        │               │   │   └── QuoteTest.java
        │               │   └── infrastructure
        │               │       ├── integration
        │               │       │   └── LocalOrderCreationGatewayTest.java
        │               │       ├── persistence
        │               │       │   └── adapters
        │               │       │       └── JpaQuoteRepositoryAdapterTest.java
        │               │       └── web
        │               │           └── QuoteControllerTest.java
        │               ├── receivables
        │               │   ├── application
        │               │   │   ├── architecture
        │               │   │   │   └── ReceivablesBoundaryTest.java
        │               │   │   ├── customer_debt
        │               │   │   │   ├── GetCustomerDebtUseCaseTest.java
        │               │   │   │   └── SearchCustomerDebtUseCaseTest.java
        │               │   │   ├── customer_invoices
        │               │   │   │   └── GetCustomerInvoicesUseCaseTest.java
        │               │   │   ├── dto
        │               │   │   │   └── response
        │               │   │   │       └── ReceivablesSummaryResponseTest.java
        │               │   │   ├── payment_history
        │               │   │   │   └── GetPaymentHistoryUseCaseTest.java
        │               │   │   └── summary
        │               │   │       └── GetReceivablesSummaryUseCaseTest.java
        │               │   └── infrastructure
        │               │       ├── config
        │               │       │   └── ReceivablesConfigTest.java
        │               │       ├── integration
        │               │       │   └── LocalReceivablesCustomerProfileGatewayAdapterTest.java
        │               │       ├── persistence
        │               │       │   └── adapters
        │               │       │       └── ReceivablesRepositoryAdapterTest.java
        │               │       └── web
        │               │           └── ReceivablesControllerTest.java
        │               ├── reports
        │               │   ├── application
        │               │   │   └── architecture
        │               │   │       ├── AccountsReceivableReportsBoundaryTest.java
        │               │   │       ├── CommercialCatalogBoundaryTest.java
        │               │   │       ├── InvoiceReportsBoundaryTest.java
        │               │   │       ├── OrderReportsBoundaryTest.java
        │               │   │       ├── PaymentReportsBoundaryTest.java
        │               │   │       ├── PurchaseReportsBoundaryTest.java
        │               │   │       └── QuoteReportsBoundaryTest.java
        │               │   ├── finance
        │               │   │   ├── application
        │               │   │   │   ├── dto
        │               │   │   │   │   ├── AccountsReceivableReportDataTest.java
        │               │   │   │   │   ├── CollectionsReportDataTest.java
        │               │   │   │   │   └── PaymentHistoryReportDataTest.java
        │               │   │   │   ├── generate_accounts_receivable_report_pdf
        │               │   │   │   │   └── GenerateAccountsReceivableReportPdfUseCaseImplTest.java
        │               │   │   │   ├── generate_collections_report_pdf
        │               │   │   │   │   └── GenerateCollectionsReportPdfUseCaseImplTest.java
        │               │   │   │   ├── generate_payment_history_report_pdf
        │               │   │   │   │   └── GeneratePaymentHistoryReportPdfUseCaseImplTest.java
        │               │   │   │   ├── generate_payment_pdf
        │               │   │   │   │   └── GeneratePaymentPdfUseCaseImplTest.java
        │               │   │   │   └── ports
        │               │   │   │       └── out
        │               │   │   │           └── PaymentPdfPortTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── config
        │               │   │       │   └── FinancesReportConfigTest.java
        │               │   │       ├── integration
        │               │   │       │   ├── PaymentDocumentQueryAdapterTest.java
        │               │   │       │   └── ReceivablesReportQueryAdapterTest.java
        │               │   │       ├── pdf
        │               │   │       │   ├── AccountsReceivablePdfContentTest.java
        │               │   │       │   └── PaymentPdfContentTest.java
        │               │   │       └── web
        │               │   │           └── FinancesReportControllerTest.java
        │               │   ├── infrastructure
        │               │   │   └── pdf
        │               │   │       └── PdfStatusColorsTest.java
        │               │   ├── inventory
        │               │   │   ├── application
        │               │   │   │   ├── dto
        │               │   │   │   │   ├── query
        │               │   │   │   │   │   ├── CatalogPdfQueryTest.java
        │               │   │   │   │   │   └── CatalogReportQueryTest.java
        │               │   │   │   │   └── response
        │               │   │   │   │       ├── CatalogReportResponseTest.java
        │               │   │   │   │       └── CommercialCatalogResponseTest.java
        │               │   │   │   ├── generate_catalog_pdf
        │               │   │   │   │   └── GenerateCatalogPdfUseCaseImplTest.java
        │               │   │   │   ├── generate_commercial_catalog_pdf
        │               │   │   │   │   └── GenerateCommercialCatalogPdfUseCaseImplTest.java
        │               │   │   │   ├── get_catalog_report
        │               │   │   │   │   └── GetCatalogReportUseCaseImplTest.java
        │               │   │   │   └── ports
        │               │   │   │       └── out
        │               │   │   │           └── CatalogPdfPortTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── config
        │               │   │       │   └── InventoryReportConfigTest.java
        │               │   │       ├── integration
        │               │   │       │   └── FileStorageMediaAdapterTest.java
        │               │   │       ├── pdf
        │               │   │       │   └── CommercialCatalogPdfContentTest.java
        │               │   │       ├── persistence
        │               │   │       │   ├── adapters
        │               │   │       │   │   └── CatalogReportRepositoryAdapterTest.java
        │               │   │       │   └── projections
        │               │   │       │       └── CatalogReportProjectionTest.java
        │               │   │       └── web
        │               │   │           ├── CatalogReportControllerTest.java
        │               │   │           └── CommercialCatalogControllerTest.java
        │               │   ├── invoices
        │               │   │   ├── application
        │               │   │   │   ├── generate_invoice_pdf
        │               │   │   │   │   └── GenerateInvoicePdfUseCaseImplTest.java
        │               │   │   │   └── generate_invoices_report_pdf
        │               │   │   │       └── GenerateInvoicesReportPdfUseCaseImplTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── config
        │               │   │       │   └── InvoicesReportConfigTest.java
        │               │   │       ├── integration
        │               │   │       │   └── InvoiceDocumentQueryAdapterTest.java
        │               │   │       ├── pdf
        │               │   │       │   └── InvoicePdfContentTest.java
        │               │   │       └── web
        │               │   │           └── InvoicesReportControllerTest.java
        │               │   ├── movements
        │               │   │   ├── application
        │               │   │   │   ├── dto
        │               │   │   │   │   ├── query
        │               │   │   │   │   │   └── MovementsPdfQueryTest.java
        │               │   │   │   │   └── response
        │               │   │   │   │       └── MovementReportResponseTest.java
        │               │   │   │   ├── generate_movements_excel
        │               │   │   │   │   └── GenerateMovementsExcelUseCaseImplTest.java
        │               │   │   │   ├── generate_movements_pdf
        │               │   │   │   │   └── GenerateMovementsPdfUseCaseImplTest.java
        │               │   │   │   └── ports
        │               │   │   │       └── out
        │               │   │   │           └── MovementsPdfPortTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── config
        │               │   │       │   └── MovementsReportConfigTest.java
        │               │   │       ├── persistence
        │               │   │       │   ├── adapters
        │               │   │       │   │   └── MovementsReportRepositoryAdapterTest.java
        │               │   │       │   └── projections
        │               │   │       │       └── MovementsReportProjectionTest.java
        │               │   │       └── web
        │               │   │           └── MovementsReportControllerTest.java
        │               │   ├── orders
        │               │   │   ├── application
        │               │   │   │   ├── generate_order_pdf
        │               │   │   │   │   └── GenerateOrderPdfUseCaseImplTest.java
        │               │   │   │   └── generate_orders_report_pdf
        │               │   │   │       └── GenerateOrdersReportPdfUseCaseImplTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── config
        │               │   │       │   └── OrdersReportConfigTest.java
        │               │   │       ├── integration
        │               │   │       │   └── OrderDocumentQueryAdapterTest.java
        │               │   │       ├── pdf
        │               │   │       │   └── OrderPdfContentTest.java
        │               │   │       └── web
        │               │   │           └── OrdersReportControllerTest.java
        │               │   ├── purchases
        │               │   │   ├── application
        │               │   │   │   ├── generate_purchase_pdf
        │               │   │   │   │   └── GeneratePurchasePdfUseCaseImplTest.java
        │               │   │   │   └── generate_purchases_report_pdf
        │               │   │   │       └── GeneratePurchasesReportPdfUseCaseImplTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── config
        │               │   │       │   └── PurchasesReportConfigTest.java
        │               │   │       ├── integration
        │               │   │       │   └── PurchaseDocumentQueryAdapterTest.java
        │               │   │       ├── pdf
        │               │   │       │   └── PurchasePdfContentTest.java
        │               │   │       └── web
        │               │   │           └── PurchasesReportControllerTest.java
        │               │   ├── quotes
        │               │   │   ├── application
        │               │   │   │   ├── generate_quote_pdf
        │               │   │   │   │   └── GenerateQuotePdfUseCaseImplTest.java
        │               │   │   │   └── generate_quotes_report_pdf
        │               │   │   │       └── GenerateQuotesReportPdfUseCaseImplTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── config
        │               │   │       │   └── QuotesReportConfigTest.java
        │               │   │       ├── integration
        │               │   │       │   └── QuoteDocumentQueryAdapterTest.java
        │               │   │       ├── pdf
        │               │   │       │   └── QuotePdfContentTest.java
        │               │   │       └── web
        │               │   │           └── QuotesReportControllerTest.java
        │               │   └── stock
        │               │       ├── application
        │               │       │   ├── dto
        │               │       │   │   ├── query
        │               │       │   │   │   └── StockPdfQueryTest.java
        │               │       │   │   └── response
        │               │       │   │       └── StockReportResponseTest.java
        │               │       │   ├── generate_stock_excel
        │               │       │   │   └── GenerateStockExcelUseCaseImplTest.java
        │               │       │   ├── generate_stock_pdf
        │               │       │   │   └── GenerateStockPdfUseCaseImplTest.java
        │               │       │   └── ports
        │               │       │       └── out
        │               │       │           └── StockPdfPortTest.java
        │               │       └── infrastructure
        │               │           ├── config
        │               │           │   └── StockReportConfigTest.java
        │               │           ├── persistence
        │               │           │   ├── adapters
        │               │           │   │   └── StockReportRepositoryAdapterTest.java
        │               │           │   └── projections
        │               │           │       └── StockReportProjectionTest.java
        │               │           └── web
        │               │               └── StockReportControllerTest.java
        │               ├── returns
        │               │   ├── application
        │               │   │   ├── architecture
        │               │   │   │   ├── ReturnFinancialBoundaryTest.java
        │               │   │   │   └── ReturnInventoryBoundaryTest.java
        │               │   │   ├── create
        │               │   │   │   └── CreateReturnUseCaseTest.java
        │               │   │   ├── delete
        │               │   │   │   └── DeleteReturnUseCaseTest.java
        │               │   │   ├── get
        │               │   │   │   └── GetReturnByIdUseCaseTest.java
        │               │   │   ├── list
        │               │   │   │   └── GetAllReturnsUseCaseTest.java
        │               │   │   ├── mappers
        │               │   │   │   └── ReturnMapperTest.java
        │               │   │   ├── services
        │               │   │   │   └── ReturnResponseAssemblerTest.java
        │               │   │   └── usecases
        │               │   │       └── ReturnFinancialFlowsTest.java
        │               │   ├── domain
        │               │   │   ├── CancelledAllocationUnitTest.java
        │               │   │   ├── ReturnIdUnitTest.java
        │               │   │   ├── ReturnInvoiceReferenceBoundaryTest.java
        │               │   │   ├── ReturnLineAllocationUnitTest.java
        │               │   │   ├── ReturnLineUnitTest.java
        │               │   │   ├── ReturnNumberUnitTest.java
        │               │   │   └── ReturnUnitTest.java
        │               │   └── infrastructure
        │               │       ├── config
        │               │       │   └── ReturnConfigTest.java
        │               │       ├── integration
        │               │       │   ├── InvoiceReturnAdjustmentAdapterTest.java
        │               │       │   ├── LocalReturnInventoryGatewayAdapterTest.java
        │               │       │   └── ReturnFinancialWiringTest.java
        │               │       ├── integration_tests
        │               │       │   └── ReturnsFinancialRollbackMySqlIT.java
        │               │       ├── persistence
        │               │       │   └── adapters
        │               │       │       └── JpaReturnRepositoryTest.java
        │               │       └── web
        │               │           └── ReturnControllerTest.java
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
        │               │   ├── domain
        │               │   │   ├── valueobjects
        │               │   │   │   ├── EmailAddressUnitTest.java
        │               │   │   │   ├── LegalNameUnitTest.java
        │               │   │   │   ├── PhoneNumberUnitTest.java
        │               │   │   │   ├── RifUnitTest.java
        │               │   │   │   ├── SupplierAliasUnitTest.java
        │               │   │   │   ├── SupplierCodeUnitTest.java
        │               │   │   │   └── SupplierIdUnitTest.java
        │               │   │   ├── SupplierPurchaseUsageTest.java
        │               │   │   └── SupplierUnitTest.java
        │               │   └── infrastructure
        │               │       ├── integration
        │               │       │   ├── LocalPurchaseUsageGatewayAdapterTest.java
        │               │       │   └── SupplierPurchaseDirectoryAdapterTest.java
        │               │       ├── persistence
        │               │       │   └── adapters
        │               │       │       └── JpaSupplierRepositoryTest.java
        │               │       └── web
        │               │           └── SupplierControllerTest.java
        │               ├── warehouses
        │               │   ├── management
        │               │   │   ├── application
        │               │   │   │   ├── create
        │               │   │   │   │   ├── CreateWarehouseUseCaseTest.java
        │               │   │   │   │   └── DuplicateWarehouseCodeExceptionTest.java
        │               │   │   │   ├── delete
        │               │   │   │   │   └── DeleteWarehouseUseCaseTest.java
        │               │   │   │   ├── exceptions
        │               │   │   │   │   ├── WarehouseNotActiveExceptionTest.java
        │               │   │   │   │   └── WarehouseNotFoundExceptionTest.java
        │               │   │   │   ├── queries
        │               │   │   │   │   ├── GetWarehouseUseCaseTest.java
        │               │   │   │   │   ├── ListWarehousesUseCaseTest.java
        │               │   │   │   │   └── RepositoryWarehouseDirectoryQueryTest.java
        │               │   │   │   └── update
        │               │   │   │       └── UpdateWarehouseUseCaseTest.java
        │               │   │   ├── domain
        │               │   │   │   └── WarehouseUnitTest.java
        │               │   │   └── infrastructure
        │               │   │       ├── persistence
        │               │   │       │   └── adapters
        │               │   │       │       └── JpaWarehouseRepositoryTest.java
        │               │   │       └── web
        │               │   │           └── WarehouseControllerTest.java
        │               │   └── stock
        │               │       ├── application
        │               │       │   ├── adjustment
        │               │       │   │   └── StockAdjustmentUseCaseTest.java
        │               │       │   ├── entry
        │               │       │   │   └── StockEntryUseCaseTest.java
        │               │       │   ├── exceptions
        │               │       │   │   ├── InsufficientStockExceptionTest.java
        │               │       │   │   └── ProductNotInWarehouseExceptionTest.java
        │               │       │   ├── exit
        │               │       │   │   └── StockExitUseCaseTest.java
        │               │       │   ├── invoices
        │               │       │   │   └── InvoiceInventoryServiceTest.java
        │               │       │   ├── movements
        │               │       │   │   └── GetStockMovementsUseCaseTest.java
        │               │       │   ├── ports
        │               │       │   │   └── in
        │               │       │   │       └── InvoiceStockCommandTest.java
        │               │       │   ├── queries
        │               │       │   │   ├── GetWarehouseStockUseCaseTest.java
        │               │       │   │   └── StockAvailabilityQueryServiceTest.java
        │               │       │   ├── returns
        │               │       │   │   └── ReturnInventoryServiceTest.java
        │               │       │   ├── transfer
        │               │       │   │   └── TransferStockUseCaseTest.java
        │               │       │   └── ManualStockCatalogBoundaryTest.java
        │               │       ├── domain
        │               │       │   ├── MovementTypeUnitTest.java
        │               │       │   ├── ReferenceTypeTest.java
        │               │       │   ├── StockMovementUnitTest.java
        │               │       │   └── WarehouseStockTest.java
        │               │       └── infrastructure
        │               │           ├── concurrency
        │               │           │   ├── InvoiceInventoryConcurrencyIT.java
        │               │           │   ├── ManualStockConcurrencyIT.java
        │               │           │   ├── StockDeductionConcurrencyIT.java
        │               │           │   └── WarehouseStockConcurrencyIT.java
        │               │           ├── integration
        │               │           │   ├── LocalStockCatalogGatewayAdapterTest.java
        │               │           │   └── StockAvailabilityConsumerAdaptersTest.java
        │               │           ├── persistence
        │               │           │   └── adapters
        │               │           │       ├── JpaStockMovementRepositoryTest.java
        │               │           │       └── JpaWarehouseStockRepositoryTest.java
        │               │           └── web
        │               │               ├── StockMovementControllerTest.java
        │               │               └── WarehouseStockControllerTest.java
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
