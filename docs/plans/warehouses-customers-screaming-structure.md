# Complete Warehouses and Customers structure proposal

Status: proposed paths; no Java sources have been moved. Inventory date: 2026-10-16.

Paths below are relative to `src/main/java/com/cashly/cashly_api`. Every current production source appears once in each corresponding tree and once in the move table. Class names and behavior remain unchanged in the initial structural migration.

Proposed trees additionally include `overview` with three `[NEW, optional]` sources per module: a controller, a read use case and its response. These six additions show the location of combined views explicitly; they do not exist today and are not entries in the 216-file relocation manifest. The current trees, ownership counts and move tables cover existing sources only. The new overview use cases would be wired in the existing module root configs when implemented.

See [decisions, responsibilities, dependencies and migration phases](warehouses-customers-screaming-plan.md). The [JSON manifest](warehouses-customers-screaming-moves.json) includes SHA-256 hashes of reviewed source contents. Hashes identify this review snapshot; they are not proof of runtime correctness.

## warehouses

86 production sources. Proposed ownership: `infrastructure`: 1, `management`: 23, `stock`: 62. Root `infrastructure` is the existing mixed composition config.

### Current complete tree

```text
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

### Proposed complete tree

```text
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
    ├── overview
    │   ├── application
    │   │   ├── GetWarehouseOverviewUseCase.java [NEW, optional]
    │   │   └── WarehouseOverviewResponse.java [NEW, optional]
    │   └── infrastructure
    │       └── web
    │           └── WarehouseOverviewController.java [NEW, optional]
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

### Exact production moves

| Current path | Proposed path | Responsibility |
|---|---|---|
| `warehouses/infrastructure/config/WarehouseConfig.java` | `warehouses/infrastructure/config/WarehouseConfig.java` | Keep mixed wiring as module composition root; currently wires management, stock and invoice inventory. Split only in a later change. |
| `warehouses/application/dto/request/CreateWarehouseRequest.java` | `warehouses/management/application/create/CreateWarehouseRequest.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/usecases/CreateWarehouseUseCase.java` | `warehouses/management/application/create/CreateWarehouseUseCase.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/exceptions/DuplicateWarehouseCodeException.java` | `warehouses/management/application/create/DuplicateWarehouseCodeException.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/usecases/DeleteWarehouseUseCase.java` | `warehouses/management/application/delete/DeleteWarehouseUseCase.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/exceptions/WarehouseHasActiveStockException.java` | `warehouses/management/application/delete/WarehouseHasActiveStockException.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/dto/response/WarehouseDetailResponse.java` | `warehouses/management/application/dto/WarehouseDetailResponse.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/dto/response/WarehouseListResponse.java` | `warehouses/management/application/dto/WarehouseListResponse.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/exceptions/WarehouseNotActiveException.java` | `warehouses/management/application/exceptions/WarehouseNotActiveException.java` | Relocate to management ownership; preserve behavior and class name. Stock may reference these management errors within the same bounded context during phase 1. |
| `warehouses/application/exceptions/WarehouseNotFoundException.java` | `warehouses/management/application/exceptions/WarehouseNotFoundException.java` | Relocate to management ownership; preserve behavior and class name. Stock may reference these management errors within the same bounded context during phase 1. |
| `warehouses/application/ports/in/WarehouseDirectoryEntry.java` | `warehouses/management/application/ports/in/WarehouseDirectoryEntry.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/ports/in/WarehouseDirectoryQuery.java` | `warehouses/management/application/ports/in/WarehouseDirectoryQuery.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/usecases/GetWarehouseUseCase.java` | `warehouses/management/application/queries/GetWarehouseUseCase.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/usecases/ListWarehousesUseCase.java` | `warehouses/management/application/queries/ListWarehousesUseCase.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/services/RepositoryWarehouseDirectoryQuery.java` | `warehouses/management/application/queries/RepositoryWarehouseDirectoryQuery.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/dto/request/UpdateWarehouseRequest.java` | `warehouses/management/application/update/UpdateWarehouseRequest.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/usecases/UpdateWarehouseUseCase.java` | `warehouses/management/application/update/UpdateWarehouseUseCase.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/domain/entities/Warehouse.java` | `warehouses/management/domain/Warehouse.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/domain/repositories/WarehouseRepository.java` | `warehouses/management/domain/WarehouseRepository.java` | Relocate to management ownership; preserve behavior and class name. Active-stock/deletability read remains an explicit internal cross-submodule query exception. |
| `warehouses/infrastructure/config/WarehouseDirectoryConfig.java` | `warehouses/management/infrastructure/config/WarehouseDirectoryConfig.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/adapters/JpaWarehouseRepository.java` | `warehouses/management/infrastructure/persistence/adapters/JpaWarehouseRepository.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/entities/WarehouseEntity.java` | `warehouses/management/infrastructure/persistence/entities/WarehouseEntity.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/repositories/SpringDataWarehouseRepository.java` | `warehouses/management/infrastructure/persistence/repositories/SpringDataWarehouseRepository.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/in/controllers/WarehouseController.java` | `warehouses/management/infrastructure/web/WarehouseController.java` | Relocate to management ownership; preserve behavior and class name. |
| `warehouses/application/dto/request/StockAdjustmentRequest.java` | `warehouses/stock/application/adjustment/StockAdjustmentRequest.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/usecases/StockAdjustmentUseCase.java` | `warehouses/stock/application/adjustment/StockAdjustmentUseCase.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/services/StockDeductionService.java` | `warehouses/stock/application/deduction/StockDeductionService.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/dto/request/StockBatchItem.java` | `warehouses/stock/application/dto/StockBatchItem.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/dto/response/WarehouseStockResponse.java` | `warehouses/stock/application/dto/WarehouseStockResponse.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/dto/request/StockEntryRequest.java` | `warehouses/stock/application/entry/StockEntryRequest.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/usecases/StockEntryUseCase.java` | `warehouses/stock/application/entry/StockEntryUseCase.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/exceptions/InsufficientStockException.java` | `warehouses/stock/application/exceptions/InsufficientStockException.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/exceptions/ProductNotInWarehouseException.java` | `warehouses/stock/application/exceptions/ProductNotInWarehouseException.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/dto/request/StockExitRequest.java` | `warehouses/stock/application/exit/StockExitRequest.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/usecases/StockExitUseCase.java` | `warehouses/stock/application/exit/StockExitUseCase.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/services/InvoiceInventoryService.java` | `warehouses/stock/application/invoices/InvoiceInventoryService.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/usecases/GetStockMovementsUseCase.java` | `warehouses/stock/application/movements/GetStockMovementsUseCase.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/dto/response/StockMovementResponse.java` | `warehouses/stock/application/movements/StockMovementResponse.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/in/AvailableStockProductsQuery.java` | `warehouses/stock/application/ports/in/AvailableStockProductsQuery.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/in/InvoiceInventoryApi.java` | `warehouses/stock/application/ports/in/InvoiceInventoryApi.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/in/InvoiceStockCommand.java` | `warehouses/stock/application/ports/in/InvoiceStockCommand.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/in/ProductReferenceQuery.java` | `warehouses/stock/application/ports/in/ProductReferenceQuery.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/in/ProductStockQuery.java` | `warehouses/stock/application/ports/in/ProductStockQuery.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/in/PurchaseInventoryApi.java` | `warehouses/stock/application/ports/in/PurchaseInventoryApi.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/in/PurchaseStockCommand.java` | `warehouses/stock/application/ports/in/PurchaseStockCommand.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/in/PurchaseWarehouseQuery.java` | `warehouses/stock/application/ports/in/PurchaseWarehouseQuery.java` | Relocate to stock ownership; preserve behavior and class name. Composite read for purchasing includes stock and warehouse directory; owned by stock for this organizational plan. |
| `warehouses/application/ports/in/ReturnInventoryApi.java` | `warehouses/stock/application/ports/in/ReturnInventoryApi.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/in/ReturnStockCommand.java` | `warehouses/stock/application/ports/in/ReturnStockCommand.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/in/StockAvailabilityQuery.java` | `warehouses/stock/application/ports/in/StockAvailabilityQuery.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/in/StockDeductionApi.java` | `warehouses/stock/application/ports/in/StockDeductionApi.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/out/CustomerWarehouseAccessGateway.java` | `warehouses/stock/application/ports/out/CustomerWarehouseAccessGateway.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/ports/out/StockCatalogGateway.java` | `warehouses/stock/application/ports/out/StockCatalogGateway.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/services/PurchaseInventoryService.java` | `warehouses/stock/application/purchases/PurchaseInventoryService.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/usecases/GetWarehouseStockUseCase.java` | `warehouses/stock/application/queries/GetWarehouseStockUseCase.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/services/RepositoryProductStockQuery.java` | `warehouses/stock/application/queries/RepositoryProductStockQuery.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/queries/RepositoryPurchaseWarehouseQuery.java` | `warehouses/stock/application/queries/RepositoryPurchaseWarehouseQuery.java` | Relocate to stock ownership; preserve behavior and class name. Composite read for purchasing includes stock and warehouse directory; owned by stock for this organizational plan. |
| `warehouses/application/services/StockAvailabilityQueryService.java` | `warehouses/stock/application/queries/StockAvailabilityQueryService.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/services/ReturnInventoryService.java` | `warehouses/stock/application/returns/ReturnInventoryService.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/dto/request/TransferStockRequest.java` | `warehouses/stock/application/transfer/TransferStockRequest.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/application/usecases/TransferStockUseCase.java` | `warehouses/stock/application/transfer/TransferStockUseCase.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/domain/entities/MovementType.java` | `warehouses/stock/domain/MovementType.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/domain/entities/ReferenceType.java` | `warehouses/stock/domain/ReferenceType.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/domain/entities/StockMovement.java` | `warehouses/stock/domain/StockMovement.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/domain/repositories/StockMovementRepository.java` | `warehouses/stock/domain/StockMovementRepository.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/domain/entities/WarehouseStock.java` | `warehouses/stock/domain/WarehouseStock.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/domain/repositories/WarehouseStockRepository.java` | `warehouses/stock/domain/WarehouseStockRepository.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/config/ProductStockQueryConfig.java` | `warehouses/stock/infrastructure/config/ProductStockQueryConfig.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/config/PurchaseInventoryConfig.java` | `warehouses/stock/infrastructure/config/PurchaseInventoryConfig.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/config/PurchaseWarehouseQueryConfig.java` | `warehouses/stock/infrastructure/config/PurchaseWarehouseQueryConfig.java` | Relocate to stock ownership; preserve behavior and class name. Composite read for purchasing includes stock and warehouse directory; owned by stock for this organizational plan. |
| `warehouses/infrastructure/config/ReturnInventoryConfig.java` | `warehouses/stock/infrastructure/config/ReturnInventoryConfig.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/config/StockAvailabilityQueryConfig.java` | `warehouses/stock/infrastructure/config/StockAvailabilityQueryConfig.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/config/StockDeductionConfig.java` | `warehouses/stock/infrastructure/config/StockDeductionConfig.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/integration/CustomerWarehouseAccessAdapter.java` | `warehouses/stock/infrastructure/integration/CustomerWarehouseAccessAdapter.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/integration/LocalStockCatalogGatewayAdapter.java` | `warehouses/stock/infrastructure/integration/LocalStockCatalogGatewayAdapter.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/adapters/JpaStockMovementRepository.java` | `warehouses/stock/infrastructure/persistence/adapters/JpaStockMovementRepository.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/adapters/JpaWarehouseStockRepository.java` | `warehouses/stock/infrastructure/persistence/adapters/JpaWarehouseStockRepository.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/entities/StockMovementEntity.java` | `warehouses/stock/infrastructure/persistence/entities/StockMovementEntity.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/entities/WarehouseStockEntity.java` | `warehouses/stock/infrastructure/persistence/entities/WarehouseStockEntity.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/queries/JpaAvailableStockProductsQuery.java` | `warehouses/stock/infrastructure/persistence/queries/JpaAvailableStockProductsQuery.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/queries/JpaWarehousesProductReferenceQuery.java` | `warehouses/stock/infrastructure/persistence/queries/JpaWarehousesProductReferenceQuery.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/repositories/SpringDataAvailableStockProductsRepository.java` | `warehouses/stock/infrastructure/persistence/repositories/SpringDataAvailableStockProductsRepository.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/repositories/SpringDataStockMovementRepository.java` | `warehouses/stock/infrastructure/persistence/repositories/SpringDataStockMovementRepository.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/repositories/SpringDataWarehouseStockRepository.java` | `warehouses/stock/infrastructure/persistence/repositories/SpringDataWarehouseStockRepository.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/out/persistence/repositories/SpringDataWarehousesProductReferenceRepository.java` | `warehouses/stock/infrastructure/persistence/repositories/SpringDataWarehousesProductReferenceRepository.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/in/controllers/StockMovementController.java` | `warehouses/stock/infrastructure/web/StockMovementController.java` | Relocate to stock ownership; preserve behavior and class name. |
| `warehouses/infrastructure/adapters/in/controllers/WarehouseStockController.java` | `warehouses/stock/infrastructure/web/WarehouseStockController.java` | Relocate to stock ownership; preserve behavior and class name. |

## customers

130 production sources. Proposed ownership: `balance`: 35, `credit`: 15, `infrastructure`: 1, `profile`: 61, `warehouse_access`: 18. Root `infrastructure` is the existing mixed composition config.

### Current complete tree

```text
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

### Proposed complete tree

```text
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
    ├── overview
    │   ├── application
    │   │   ├── CustomerOverviewResponse.java [NEW, optional]
    │   │   └── GetCustomerOverviewUseCase.java [NEW, optional]
    │   └── infrastructure
    │       └── web
    │           └── CustomerOverviewController.java [NEW, optional]
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

### Exact production moves

| Current path | Proposed path | Responsibility |
|---|---|---|
| `customers/application/usecases/AddToCustomerBalanceUseCase.java` | `customers/balance/application/add/AddToCustomerBalanceUseCase.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/dto/request/CreateAdvanceRequest.java` | `customers/balance/application/advance/CreateAdvanceRequest.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/usecases/CreateCustomerAdvanceUseCase.java` | `customers/balance/application/advance/CreateCustomerAdvanceUseCase.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/usecases/ConsumeCustomerBalanceUseCase.java` | `customers/balance/application/consume/ConsumeCustomerBalanceUseCase.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/dto/response/BalanceTransactionResponse.java` | `customers/balance/application/dto/response/BalanceTransactionResponse.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/dto/response/CustomerBalanceMutationResponse.java` | `customers/balance/application/dto/response/CustomerBalanceMutationResponse.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/dto/response/CustomerBalanceResponse.java` | `customers/balance/application/dto/response/CustomerBalanceResponse.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/usecases/GetCustomerBalanceUseCase.java` | `customers/balance/application/get/GetCustomerBalanceUseCase.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/mappers/BalanceMapper.java` | `customers/balance/application/mappers/BalanceMapper.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/ports/in/CreditNoteBalanceApi.java` | `customers/balance/application/ports/in/CreditNoteBalanceApi.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/ports/in/InvoiceBalanceConsumption.java` | `customers/balance/application/ports/in/InvoiceBalanceConsumption.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/ports/in/ReturnBalanceMutation.java` | `customers/balance/application/ports/in/ReturnBalanceMutation.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/ports/out/CustomerActorGateway.java` | `customers/balance/application/ports/out/CustomerActorGateway.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/ports/out/CustomerCashGateway.java` | `customers/balance/application/ports/out/CustomerCashGateway.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/dto/request/CreateRefundRequest.java` | `customers/balance/application/refund/CreateRefundRequest.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/usecases/RefundCustomerBalanceUseCase.java` | `customers/balance/application/refund/RefundCustomerBalanceUseCase.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/services/InvoiceBalanceConsumptionService.java` | `customers/balance/application/services/InvoiceBalanceConsumptionService.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/services/ReturnBalanceMutationService.java` | `customers/balance/application/services/ReturnBalanceMutationService.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/domain/entities/BalanceTransaction.java` | `customers/balance/domain/BalanceTransaction.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/domain/entities/CustomerBalance.java` | `customers/balance/domain/CustomerBalance.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/domain/repositories/CustomerBalanceRepository.java` | `customers/balance/domain/CustomerBalanceRepository.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/domain/valueobjects/BalanceType.java` | `customers/balance/domain/valueobjects/BalanceType.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/config/InvoiceBalanceConsumptionConfig.java` | `customers/balance/infrastructure/config/InvoiceBalanceConsumptionConfig.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/config/ReturnBalanceMutationConfig.java` | `customers/balance/infrastructure/config/ReturnBalanceMutationConfig.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/adapters/out/integration/LocalCreditNoteBalanceApi.java` | `customers/balance/infrastructure/integration/LocalCreditNoteBalanceApi.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/adapters/out/integration/LocalCustomerActorGateway.java` | `customers/balance/infrastructure/integration/LocalCustomerActorGateway.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/adapters/out/integration/LocalCustomerCashGateway.java` | `customers/balance/infrastructure/integration/LocalCustomerCashGateway.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/adapters/out/persistence/adapters/CustomerBalanceRepositoryAdapter.java` | `customers/balance/infrastructure/persistence/adapters/CustomerBalanceRepositoryAdapter.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/adapters/out/persistence/entities/BalanceTransactionEntity.java` | `customers/balance/infrastructure/persistence/entities/BalanceTransactionEntity.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/adapters/out/persistence/entities/BalanceTxSequenceEntity.java` | `customers/balance/infrastructure/persistence/entities/BalanceTxSequenceEntity.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/adapters/out/persistence/entities/CustomerBalanceEntity.java` | `customers/balance/infrastructure/persistence/entities/CustomerBalanceEntity.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/adapters/out/persistence/mappers/BalanceTransactionJpaMapper.java` | `customers/balance/infrastructure/persistence/mappers/BalanceTransactionJpaMapper.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/adapters/out/persistence/mappers/CustomerBalanceJpaMapper.java` | `customers/balance/infrastructure/persistence/mappers/CustomerBalanceJpaMapper.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/adapters/out/persistence/repositories/CustomerBalanceJpaRepository.java` | `customers/balance/infrastructure/persistence/repositories/CustomerBalanceJpaRepository.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/infrastructure/adapters/in/controllers/CustomerBalanceController.java` | `customers/balance/infrastructure/web/CustomerBalanceController.java` | Customer prepaid/available balance and its auditable mutations belong to balance. |
| `customers/application/dto/response/CreditStatusResponse.java` | `customers/credit/application/dto/response/CreditStatusResponse.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/application/dto/response/OverdueInvoiceResponse.java` | `customers/credit/application/dto/response/OverdueInvoiceResponse.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/application/exceptions/CreditLimitExceededException.java` | `customers/credit/application/exceptions/CreditLimitExceededException.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/application/usecases/credit/GetOverdueInvoicesUseCase.java` | `customers/credit/application/overdue/GetOverdueInvoicesUseCase.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/application/ports/in/CustomerCreditContextEntry.java` | `customers/credit/application/ports/in/CustomerCreditContextEntry.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/application/ports/in/CustomerCreditContextQuery.java` | `customers/credit/application/ports/in/CustomerCreditContextQuery.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/application/ports/in/CustomerCreditEligibilityQuery.java` | `customers/credit/application/ports/in/CustomerCreditEligibilityQuery.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/application/ports/in/CustomerCreditLimits.java` | `customers/credit/application/ports/in/CustomerCreditLimits.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/application/ports/out/CreditValidationGateway.java` | `customers/credit/application/ports/out/CreditValidationGateway.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/application/services/RepositoryCustomerCreditContextQuery.java` | `customers/credit/application/queries/RepositoryCustomerCreditContextQuery.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/application/services/CustomerCreditEligibilityService.java` | `customers/credit/application/services/CustomerCreditEligibilityService.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/application/usecases/credit/GetCustomerCreditStatusUseCase.java` | `customers/credit/application/status/GetCustomerCreditStatusUseCase.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/infrastructure/config/CustomerCreditQueryConfig.java` | `customers/credit/infrastructure/config/CustomerCreditQueryConfig.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/infrastructure/adapters/out/integration/LocalCreditValidationGateway.java` | `customers/credit/infrastructure/integration/LocalCreditValidationGateway.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/infrastructure/adapters/in/controllers/CustomerCreditController.java` | `customers/credit/infrastructure/web/CustomerCreditController.java` | Credit eligibility and credit-status read composition belong to credit; no new aggregate. |
| `customers/infrastructure/config/CustomerConfig.java` | `customers/infrastructure/config/CustomerConfig.java` | Module composition root currently wires all four submodules; preserve intact for behavior-neutral migration. |
| `customers/application/exceptions/CustomerHasAssociatedRecordsException.java` | `customers/profile/application/delete/CustomerHasAssociatedRecordsException.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/usecases/DeleteCustomerUseCase.java` | `customers/profile/application/delete/DeleteCustomerUseCase.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/dto/request/AddressRequest.java` | `customers/profile/application/dto/request/AddressRequest.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/dto/request/EmailRequest.java` | `customers/profile/application/dto/request/EmailRequest.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/dto/request/PhoneRequest.java` | `customers/profile/application/dto/request/PhoneRequest.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/dto/response/AddressResponse.java` | `customers/profile/application/dto/response/AddressResponse.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/dto/response/CustomerDetailResponse.java` | `customers/profile/application/dto/response/CustomerDetailResponse.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/dto/response/CustomerListResponse.java` | `customers/profile/application/dto/response/CustomerListResponse.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/dto/response/EmailResponse.java` | `customers/profile/application/dto/response/EmailResponse.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/dto/response/PhoneResponse.java` | `customers/profile/application/dto/response/PhoneResponse.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/exceptions/CustomerNotFoundException.java` | `customers/profile/application/exceptions/CustomerNotFoundException.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/exceptions/DuplicateCustomerRifException.java` | `customers/profile/application/exceptions/DuplicateCustomerRifException.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/usecases/GetCustomerUseCase.java` | `customers/profile/application/get/GetCustomerUseCase.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/usecases/ListCustomersUseCase.java` | `customers/profile/application/list/ListCustomersUseCase.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/mappers/CustomerMapper.java` | `customers/profile/application/mappers/CustomerMapper.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/ports/in/CustomerCreditTermsQuery.java` | `customers/profile/application/ports/in/CustomerCreditTermsQuery.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/ports/in/CustomerProductPricingQuery.java` | `customers/profile/application/ports/in/CustomerProductPricingQuery.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/ports/in/CustomerProfileQuery.java` | `customers/profile/application/ports/in/CustomerProfileQuery.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/ports/in/CustomerProfileSnapshot.java` | `customers/profile/application/ports/in/CustomerProfileSnapshot.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/ports/in/InvoiceCustomerQuery.java` | `customers/profile/application/ports/in/InvoiceCustomerQuery.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/queries/RepositoryCustomerCreditTermsQuery.java` | `customers/profile/application/queries/RepositoryCustomerCreditTermsQuery.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/services/RepositoryCustomerProductPricingQuery.java` | `customers/profile/application/queries/RepositoryCustomerProductPricingQuery.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/queries/RepositoryCustomerProfileQuery.java` | `customers/profile/application/queries/RepositoryCustomerProfileQuery.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/queries/RepositoryInvoiceCustomerQuery.java` | `customers/profile/application/queries/RepositoryInvoiceCustomerQuery.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/dto/request/CreateCustomerRequest.java` | `customers/profile/application/register/CreateCustomerRequest.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/dto/response/CustomerCreateResponse.java` | `customers/profile/application/register/CustomerCreateResponse.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/usecases/RegisterCustomerUseCase.java` | `customers/profile/application/register/RegisterCustomerUseCase.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/dto/request/UpdateCustomerRequest.java` | `customers/profile/application/update/UpdateCustomerRequest.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/usecases/UpdateCustomerUseCase.java` | `customers/profile/application/update/UpdateCustomerUseCase.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/entities/Customer.java` | `customers/profile/domain/Customer.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/entities/CustomerAddress.java` | `customers/profile/domain/CustomerAddress.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/services/CustomerCodeGenerator.java` | `customers/profile/domain/CustomerCodeGenerator.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/entities/CustomerEmail.java` | `customers/profile/domain/CustomerEmail.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/entities/CustomerPhone.java` | `customers/profile/domain/CustomerPhone.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/repositories/CustomerRepository.java` | `customers/profile/domain/CustomerRepository.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/AddressId.java` | `customers/profile/domain/valueobjects/AddressId.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/CreditDays.java` | `customers/profile/domain/valueobjects/CreditDays.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/CreditLimit.java` | `customers/profile/domain/valueobjects/CreditLimit.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/CustomerAlias.java` | `customers/profile/domain/valueobjects/CustomerAlias.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/CustomerCode.java` | `customers/profile/domain/valueobjects/CustomerCode.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/CustomerId.java` | `customers/profile/domain/valueobjects/CustomerId.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/CustomerType.java` | `customers/profile/domain/valueobjects/CustomerType.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/EmailAddress.java` | `customers/profile/domain/valueobjects/EmailAddress.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/EmailId.java` | `customers/profile/domain/valueobjects/EmailId.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/LegalName.java` | `customers/profile/domain/valueobjects/LegalName.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/PaymentType.java` | `customers/profile/domain/valueobjects/PaymentType.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/PhoneId.java` | `customers/profile/domain/valueobjects/PhoneId.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/PhoneNumber.java` | `customers/profile/domain/valueobjects/PhoneNumber.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/domain/valueobjects/Rif.java` | `customers/profile/domain/valueobjects/Rif.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/infrastructure/config/CustomerCreditTermsConfig.java` | `customers/profile/infrastructure/config/CustomerCreditTermsConfig.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/infrastructure/config/CustomerProductPricingConfig.java` | `customers/profile/infrastructure/config/CustomerProductPricingConfig.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/infrastructure/config/CustomerProfileQueryConfig.java` | `customers/profile/infrastructure/config/CustomerProfileQueryConfig.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/infrastructure/config/InvoiceCustomerQueryConfig.java` | `customers/profile/infrastructure/config/InvoiceCustomerQueryConfig.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/infrastructure/adapters/out/persistence/adapters/JpaCustomerRepository.java` | `customers/profile/infrastructure/persistence/adapters/JpaCustomerRepository.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/infrastructure/adapters/out/persistence/entities/CustomerAddressEntity.java` | `customers/profile/infrastructure/persistence/entities/CustomerAddressEntity.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/infrastructure/adapters/out/persistence/entities/CustomerEmailEntity.java` | `customers/profile/infrastructure/persistence/entities/CustomerEmailEntity.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/infrastructure/adapters/out/persistence/entities/CustomerEntity.java` | `customers/profile/infrastructure/persistence/entities/CustomerEntity.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/infrastructure/adapters/out/persistence/entities/CustomerPhoneEntity.java` | `customers/profile/infrastructure/persistence/entities/CustomerPhoneEntity.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/infrastructure/adapters/out/persistence/repositories/SpringDataCustomerRepository.java` | `customers/profile/infrastructure/persistence/repositories/SpringDataCustomerRepository.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/infrastructure/adapters/out/services/SequentialCustomerCodeGenerator.java` | `customers/profile/infrastructure/services/SequentialCustomerCodeGenerator.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/infrastructure/adapters/in/controllers/CustomerController.java` | `customers/profile/infrastructure/web/CustomerController.java` | Customer aggregate, contacts and configured commercial terms belong to profile. |
| `customers/application/dto/response/CustomerWarehouseAccessResponse.java` | `customers/warehouse_access/application/dto/response/CustomerWarehouseAccessResponse.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/application/exceptions/CustomerWarehouseAccessAlreadyExistsException.java` | `customers/warehouse_access/application/exceptions/CustomerWarehouseAccessAlreadyExistsException.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/application/exceptions/CustomerWarehouseAccessNotFoundException.java` | `customers/warehouse_access/application/exceptions/CustomerWarehouseAccessNotFoundException.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/application/usecases/GrantWarehouseAccessUseCase.java` | `customers/warehouse_access/application/grant/GrantWarehouseAccessUseCase.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/application/usecases/ListCustomerWarehousesUseCase.java` | `customers/warehouse_access/application/list/ListCustomerWarehousesUseCase.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/application/ports/in/CustomerWarehouseAccessQuery.java` | `customers/warehouse_access/application/ports/in/CustomerWarehouseAccessQuery.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/application/ports/out/WarehouseDirectoryGateway.java` | `customers/warehouse_access/application/ports/out/WarehouseDirectoryGateway.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/application/ports/out/model/WarehouseSnapshot.java` | `customers/warehouse_access/application/ports/out/model/WarehouseSnapshot.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/application/queries/RepositoryCustomerWarehouseAccessQuery.java` | `customers/warehouse_access/application/queries/RepositoryCustomerWarehouseAccessQuery.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/application/usecases/RevokeWarehouseAccessUseCase.java` | `customers/warehouse_access/application/revoke/RevokeWarehouseAccessUseCase.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/domain/entities/CustomerWarehouseAccess.java` | `customers/warehouse_access/domain/CustomerWarehouseAccess.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/domain/repositories/CustomerWarehouseAccessRepository.java` | `customers/warehouse_access/domain/CustomerWarehouseAccessRepository.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/infrastructure/config/CustomerWarehouseAccessQueryConfig.java` | `customers/warehouse_access/infrastructure/config/CustomerWarehouseAccessQueryConfig.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/infrastructure/adapters/out/integration/LocalWarehouseDirectoryGateway.java` | `customers/warehouse_access/infrastructure/integration/LocalWarehouseDirectoryGateway.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/infrastructure/adapters/out/persistence/adapters/JpaCustomerWarehouseAccessRepository.java` | `customers/warehouse_access/infrastructure/persistence/adapters/JpaCustomerWarehouseAccessRepository.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/infrastructure/adapters/out/persistence/entities/CustomerWarehouseAccessEntity.java` | `customers/warehouse_access/infrastructure/persistence/entities/CustomerWarehouseAccessEntity.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/infrastructure/adapters/out/persistence/repositories/SpringDataCustomerWarehouseAccessRepository.java` | `customers/warehouse_access/infrastructure/persistence/repositories/SpringDataCustomerWarehouseAccessRepository.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
| `customers/infrastructure/adapters/in/controllers/CustomerWarehouseAccessController.java` | `customers/warehouse_access/infrastructure/web/CustomerWarehouseAccessController.java` | Customer-to-warehouse entitlement lifecycle belongs to warehouse_access. |
