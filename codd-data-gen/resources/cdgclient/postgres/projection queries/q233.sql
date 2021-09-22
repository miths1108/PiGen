select distinct s_store_id
from store_sales, store, date_dim
where ss_sold_date_sk = d_date_sk
and d_date between '2001-08-21' and '2001-09-4'
and ss_store_sk = s_store_sk
;