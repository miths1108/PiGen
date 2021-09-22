select *
 from web_sales, date_dim
 where ws_sold_date_sk = d_date_sk
   and d_year between 1999 AND 1999 + 2
;