select *
from web_sales,date_dim
where ws_sold_date_sk = d_date_sk
  and d_qoy = 1
  and d_year = 2000
;

