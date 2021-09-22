select c_email_address
 from customer
     ,catalog_sales
     ,date_dim
 where c_customer_sk = cs_bill_customer_sk
   and cs_sold_date_sk = d_date_sk
   and c_birth_year >= 1924
 group by c_email_address;