select *
from catalog_sales, date_dim
where cs_sold_date_sk = d_date_sk
and d_date between '2001-08-21' and '2001-09-4'
;
