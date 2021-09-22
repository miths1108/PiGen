select cp_catalog_page_id
from catalog_sales,
     date_dim,
     catalog_page
 where cs_sold_date_sk = d_date_sk
       and d_date between cast('1998-08-04' as date)
                  and (cast('1998-08-04' as date) +  14)
       and cs_catalog_page_sk = cp_catalog_page_sk
       and cp_catalog_number >= 1
 group by cp_catalog_page_id;