select *
from catalog_sales, date_dim
where cs_net_profit > 1
      and cs_net_paid > 0
      and cs_quantity > 0
      and cs_sold_date_sk = d_date_sk
      and d_year = 2000
      and d_moy = 12
;