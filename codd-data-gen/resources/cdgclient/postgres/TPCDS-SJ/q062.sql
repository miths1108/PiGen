select *
 from catalog_sales, customer, customer_address, date_dim
 where cs_sales_price > 500
 	and cs_sold_date_sk = d_date_sk
 	and d_qoy = 2 and d_year = 1998
 ;


