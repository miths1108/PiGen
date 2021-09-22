select distinct ca_county,d_qoy, d_year
from store_sales,date_dim,customer_address
where ss_sold_date_sk = d_date_sk
  and ss_addr_sk=ca_address_sk
  and d_qoy = 1
  and d_year = 2000
;

