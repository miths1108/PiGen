SELECT distinct d_year, d_qoy, i_category
FROM web_sales, item, date_dim
WHERE ws_sold_date_sk = d_date_sk
AND ws_item_sk = i_item_sk
;
