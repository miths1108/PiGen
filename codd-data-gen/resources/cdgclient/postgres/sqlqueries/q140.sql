select *
 from  catalog_sales, date_dim
 where
 d_year = 2001
 and d_moy               between 4 and  10
 and cs_sold_date_sk = d_date_sk
;