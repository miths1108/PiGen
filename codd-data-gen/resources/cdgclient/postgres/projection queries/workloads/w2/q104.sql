select distinct c_customer_id, c_first_name, c_last_name, d_year
from customer, store_sales, date_dim
 where c_customer_sk = ss_customer_sk
   and ss_sold_date_sk = d_date_sk
   and d_year in (1998,1998+1)
 ;
