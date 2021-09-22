select *
 from store_sales, date_dim
 where d_quarter_name = '2001Q1'
   and d_date_sk = ss_sold_date_sk
;