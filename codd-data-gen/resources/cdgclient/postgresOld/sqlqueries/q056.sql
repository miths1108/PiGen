select *
from catalog_sales, date_dim
 where
  cs_sold_date_sk = d_date_sk and
  d_year = 2001 and
  d_qoy < 4
;