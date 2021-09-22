select *
from
   store_returns, date_dim
where
    d_year = 2000
and d_moy  = 8
and sr_returned_date_sk   = d_date_sk
;