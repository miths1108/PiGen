SELECT distinct d_year, d_qoy, i_category
FROM catalog_sales, item, date_dim
WHERE cs_sold_date_sk = d_date_sk
AND cs_item_sk = i_item_sk
;
