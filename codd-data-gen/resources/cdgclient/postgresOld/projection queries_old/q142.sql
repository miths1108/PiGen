select distinct d_year
from catalog_sales, date_dim 
where cs_sold_date_sk = d_date_sk
;
