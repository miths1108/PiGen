select distinct cp_catalog_page_id
from catalog_sales, date_dim, catalog_page
where cs_sold_date_sk = d_date_sk
and cs_catalog_page_sk = cp_catalog_page_sk
and d_date between '2001-08-21' and '2001-09-4'
;