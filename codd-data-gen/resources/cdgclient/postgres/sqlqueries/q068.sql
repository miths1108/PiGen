select *
from store_sales, date_dim
where ss_sold_date_sk = d_date_sk
  and d_qoy = 1 and d_year = 2001
;