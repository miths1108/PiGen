select distinct i_item_id, i_item_desc, s_store_id, s_store_name
from store_sales, date_dim, item, store
where
 d_moy               = 4 
 and d_year              = 1999
 and d_date_sk           = ss_sold_date_sk
 and i_item_sk              = ss_item_sk
 and s_store_sk             = ss_store_sk
;