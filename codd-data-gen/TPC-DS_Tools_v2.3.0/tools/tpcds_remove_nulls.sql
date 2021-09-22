-- For two tables R (fact table) and S (dimension table), when there is a query having no filter on any column of S then the number of tuples in S before and after joining with R must be same.
-- But when foreign keys are null and then rows get reduce without filter. So we remove nulls from foreign keys

DELETE FROM customer WHERE c_current_addr_sk IS NULL;
UPDATE customer SET c_current_cdemo_sk = 68377 WHERE c_current_cdemo_sk IS NULL;

DELETE FROM catalog_returns WHERE cr_returned_date_sk IS NULL;
DELETE FROM catalog_returns WHERE cr_catalog_page_sk IS NULL;

DELETE FROM catalog_sales WHERE cs_bill_customer_sk IS NULL;
DELETE FROM catalog_sales WHERE cs_catalog_page_sk IS NULL;
DELETE FROM catalog_sales WHERE cs_sold_date_sk IS NULL;
DELETE FROM catalog_sales WHERE cs_ship_customer_sk IS NULL;
DELETE FROM catalog_sales WHERE cs_item_sk IS NULL;
DELETE FROM catalog_sales WHERE cs_ship_addr_sk IS NULL;
DELETE FROM catalog_sales WHERE cs_ship_date_sk IS NULL;
DELETE FROM catalog_sales WHERE cs_call_center_sk IS NULL;

DELETE FROM store_returns WHERE sr_returned_date_sk IS NULL;

DELETE FROM store_sales WHERE ss_store_sk IS NULL;
DELETE FROM store_sales WHERE ss_cdemo_sk IS NULL;
DELETE FROM store_sales WHERE ss_promo_sk IS NULL;
DELETE FROM store_sales WHERE ss_sold_date_sk IS NULL;
DELETE FROM store_sales WHERE ss_store_sk IS NULL;
DELETE FROM store_sales WHERE ss_item_sk IS NULL;
DELETE FROM store_sales WHERE ss_customer_sk IS NULL;
DELETE FROM store_sales WHERE ss_hdemo_sk IS NULL;
DELETE FROM store_sales WHERE ss_addr_sk IS NULL;

DELETE FROM web_sales WHERE ws_sold_date_sk IS NULL;
DELETE FROM web_sales WHERE ws_web_site_sk IS NULL;
DELETE FROM web_sales WHERE ws_bill_customer_sk IS NULL;
DELETE FROM web_sales WHERE ws_item_sk IS NULL;
