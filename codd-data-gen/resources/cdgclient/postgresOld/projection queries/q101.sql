-- start query 77 in stream 0 using template query87.tpl
select distinct c_last_name, c_first_name, d_date
       from store_sales, date_dim, customer
       where store_sales.ss_sold_date_sk = date_dim.d_date_sk
	and store_sales.ss_customer_sk = customer.c_customer_sk
         and d_month_seq between 1214 and 1214+11
;  
