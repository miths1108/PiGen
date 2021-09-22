SELECT distinct d_year, d_qoy, i_category
FROM store_sales, item, date_dim
WHERE ss_sold_date_sk = d_date_sk
AND ss_item_sk = i_item_sk
;