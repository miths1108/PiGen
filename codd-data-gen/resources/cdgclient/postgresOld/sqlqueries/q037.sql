select *
 from
    store_sales, date_dim
 where
    d_month_seq between 1181 and 1181+11
 and d_date_sk = ss_sold_date_sk
 ;
