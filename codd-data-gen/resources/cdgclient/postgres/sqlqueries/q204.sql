select *
from    store_sales, date_dim
 where  ss_sold_date_sk = d_date_sk
        and d_month_seq between 1217 and 1219
;