select distinct d_year
from web_sales, date_dim 
where ws_sold_date_sk = d_date_sk
;
