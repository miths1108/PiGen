-- start query 1 in stream 0 using template query20.tpl
select i_current_price
 from	catalog_sales
     ,item 
     ,date_dim
 where cs_item_sk = i_item_sk 
   and i_category in ('Jewelry', 'Sports', 'Books')
   and cs_sold_date_sk = d_date_sk
 and d_date between cast('2001-01-12' as date) 
 				and (cast('2001-01-12' as date) + 30)
 group by i_current_price;

-- end query 1 in stream 0 using template query20.tpl
