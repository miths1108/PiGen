select *
from store_sales, date_dim
where
 d_moy               = 4 
 and d_year              = 1999
 and d_date_sk           = ss_sold_date_sk
;