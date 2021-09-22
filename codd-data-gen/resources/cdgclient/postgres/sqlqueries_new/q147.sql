select *
 from catalog_sales, item, date_dim
 where cs_item_sk = i_item_sk
   and d_date between  '2002-03-05' and '2002-03-11'
   and cs_sold_date_sk = d_date_sk;