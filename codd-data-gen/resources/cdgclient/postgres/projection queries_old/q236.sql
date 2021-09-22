select distinct cp_catalog_page_id
from catalog_returns, date_dim, catalog_page
where cr_returned_date_sk = d_date_sk
and cr_catalog_page_sk = cp_catalog_page_sk
and d_date between '2001-08-21' and '2001-09-4'
;