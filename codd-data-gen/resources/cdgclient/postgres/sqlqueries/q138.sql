select *
 from store_sales, date_dim 
 where
 d_year = 2001
 and d_date_sk = ss_sold_date_sk
 and d_moy               between 4 and  10
;
