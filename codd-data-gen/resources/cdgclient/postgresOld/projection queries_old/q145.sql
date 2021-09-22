select distinct i_item_id
 from store_sales, item, date_dim
 where ss_item_sk = i_item_sk
   and d_date between  '2002-03-05' and '2002-03-11'
   and ss_sold_date_sk   = d_date_sk;