select distinct d_year
from store_sales, date_dim 
where ss_sold_date_sk = d_date_sk
;
