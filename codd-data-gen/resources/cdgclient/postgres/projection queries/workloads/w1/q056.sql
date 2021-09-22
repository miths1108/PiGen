select distinct ca_state, cd_gender, cd_marital_status, cd_dep_count, cd_dep_employed_count, cd_dep_college_count
from customer,customer_address,customer_demographics, catalog_sales, date_dim
 where
  c_current_addr_sk = ca_address_sk and
  cd_demo_sk = c_current_cdemo_sk and 
  c_customer_sk = cs_ship_customer_sk and
  cs_sold_date_sk = d_date_sk and
  d_year = 2001 and
  d_qoy < 4
;