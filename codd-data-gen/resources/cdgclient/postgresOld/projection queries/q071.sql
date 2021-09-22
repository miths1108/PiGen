-- start query 65 in stream 0 using template query20.tpl
select distinct i_item_id,i_item_desc,i_category,i_class,i_current_price
 from 	catalog_sales
     ,item 
     ,date_dim
 where cs_item_sk = i_item_sk 
   and i_category in ('Shoes', 'Women', 'Music')
   and cs_sold_date_sk = d_date_sk
 and d_date between '1999-06-03' and '1999-07-03'
;

-- end query 65 in stream 0 using template query20.tpl
