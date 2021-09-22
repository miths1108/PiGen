select *
 from store_sales, store, customer_demographics, household_demographics, customer_address, date_dim
 where s_store_sk = ss_store_sk
 and  ss_sold_date_sk = d_date_sk 
 and ss_hdemo_sk=hd_demo_sk
 and cd_demo_sk = ss_cdemo_sk
 and ss_addr_sk = ca_address_sk
 and d_year = 2001  
  and cd_marital_status = 'U'
  and cd_education_status = '4 yr Degree'
  and ss_sales_price between 50.00 and 100.00   
  and hd_dep_count = 1
  and ca_country = 'United States'
  and ca_state in ('ND', 'IL', 'AL')
  and ss_net_profit between 100 and 200  
;