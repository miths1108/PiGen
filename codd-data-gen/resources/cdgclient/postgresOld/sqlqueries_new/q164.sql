select *
 from catalog_sales, date_dim
 where cs_sold_date_sk = d_date_sk
   and d_quarter_name in ('2001Q1','2001Q2','2001Q3')
;