select distinct c_last_name, c_first_name, s_store_name, s_state, i_color, i_current_price, i_manager_id, i_units, i_size
from store_sales, store, item, customer
where ss_customer_sk = c_customer_sk
  and ss_item_sk = i_item_sk
  and ss_store_sk = s_store_sk
  and s_market_id = 8
and i_color = 'coral'
;
