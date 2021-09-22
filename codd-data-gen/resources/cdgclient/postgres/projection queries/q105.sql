select distinct c_customer_id, c_first_name, c_last_name, d_year
 from customer, web_sales, date_dim
 where c_customer_sk = ws_bill_customer_sk
   and ws_sold_date_sk = d_date_sk
   and d_year in (1998,1998+1)
;