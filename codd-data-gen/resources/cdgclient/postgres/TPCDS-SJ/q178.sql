select *
from store_sales,date_dim
where ss_sold_date_sk = d_date_sk
  and d_qoy = 2
  and d_year = 2000
;

