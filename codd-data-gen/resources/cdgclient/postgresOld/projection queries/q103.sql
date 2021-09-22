select distinct c_last_name, c_first_name, d_date
       from web_sales, date_dim, customer
       where web_sales.ws_sold_date_sk = date_dim.d_date_sk
and web_sales.ws_bill_customer_sk = customer.c_customer_sk
         and d_month_seq between 1214 and 1214+11
;

-- end query 77 in stream 0 using template query87.tpl
