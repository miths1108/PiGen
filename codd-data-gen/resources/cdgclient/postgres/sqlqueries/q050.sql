 select *
 from store_sales, date_dim
 where ss_sold_date_sk = d_date_sk and
	(
         d_year = 2000 or
         ( d_year = 2000-1 and d_moy =12) or
         ( d_year = 2000+1 and d_moy =1)
       )
 ;