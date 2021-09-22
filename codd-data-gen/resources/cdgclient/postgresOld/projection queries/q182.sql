select distinct ca_county,d_qoy, d_year
from web_sales,date_dim,customer_address
where ws_sold_date_sk = d_date_sk
  and ws_bill_addr_sk=ca_address_sk
  and d_qoy = 3
  and d_year =2000
;