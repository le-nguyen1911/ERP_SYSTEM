ALTER TABLE purchase_order
DROP COLUMN IF EXISTS requisition_id;

DROP TABLE IF EXISTS purchase_requisition CASCADE;

