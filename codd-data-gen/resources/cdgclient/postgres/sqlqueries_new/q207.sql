select *
from store_sales, store, item
where ss_item_sk = i_item_sk
  and ss_store_sk = s_store_sk
and s_market_id=8
and i_color = 'lawn'
;
