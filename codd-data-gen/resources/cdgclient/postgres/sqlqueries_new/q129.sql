select *
 from catalog_sales, date_dim
 where cs_sold_date_sk = d_date_sk
 	and d_qoy = 2 and d_year = 1998
 ;
