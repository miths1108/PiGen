select *
from
   store_sales, date_dim
where
    d_year = 2000
and d_moy  = 8
and ss_sold_date_sk   = d_date_sk
;