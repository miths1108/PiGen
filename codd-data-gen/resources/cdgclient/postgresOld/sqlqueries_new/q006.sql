select *
 from
    web_sales
   ,date_dim 
 where
    d_month_seq between 1211 and 1211+11
 and d_date_sk = ws_sold_date_sk
;