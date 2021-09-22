select *
 from store_sales, customer_demographics, customer_address, date_dim
 where ss_sold_date_sk = d_date_sk
 and cd_demo_sk = ss_cdemo_sk
 and ss_addr_sk = ca_address_sk
 and cd_marital_status = 'S'
 and cd_education_status = '4 yr Degree'
 and d_year = 1998
 and ss_sales_price between 50.00 and 200.00  
 and ca_country = 'United States'
 and ca_state in ('NY', 'VA', 'AR')
 and ss_net_profit between 150 and 3000 
 ;
