ALTER TABLE `warehouse_stock`
  MODIFY COLUMN `quantity` decimal(15,4) NOT NULL DEFAULT '0.0000',
  MODIFY COLUMN `reserved` decimal(15,4) NOT NULL DEFAULT '0.0000';

ALTER TABLE `stock_movements`
  MODIFY COLUMN `quantity` decimal(15,4) NOT NULL,
  MODIFY COLUMN `previous_stock` decimal(15,4) NOT NULL,
  MODIFY COLUMN `new_stock` decimal(15,4) NOT NULL;
