select  distinct c_last_name,c_first_name
from catalog_sales, date_dim, customer
       where d_year = 1999 
         and d_moy = 6 
         and cs_sold_date_sk = d_date_sk 
         and cs_bill_customer_sk = c_customer_sk 
;