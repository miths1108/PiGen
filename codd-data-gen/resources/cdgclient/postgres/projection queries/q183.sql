select distinct c_customer_id, c_first_name, c_last_name, c_preferred_cust_flag, c_birth_country, c_login, c_email_address, d_year 
from customer, store_sales, date_dim
 where c_customer_sk = ss_customer_sk
   and ss_sold_date_sk = d_date_sk
;
