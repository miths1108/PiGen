select *
 from web_sales, date_dim
 where ws_sold_date_sk = d_date_sk
   and d_year = 2000+2 
   and d_moy = 11
;