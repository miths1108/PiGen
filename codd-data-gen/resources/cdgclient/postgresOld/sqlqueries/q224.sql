select *
from store_sales, store, item
where ss_customer_sk = c_customer_sk
  and ss_item_sk = i_item_sk
  and ss_store_sk = s_store_sk
  and s_market_id = 8
and i_color = 'coral'
;
-- end query 92 in stream 0 using template query24.tpl