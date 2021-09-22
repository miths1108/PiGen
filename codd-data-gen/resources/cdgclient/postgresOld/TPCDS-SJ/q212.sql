-- start query 86 in stream 0 using template query40.tpl
select *
from catalog_sales, item, date_dim
where i_current_price between 0.99 and 1.49
 and i_item_sk          = cs_item_sk
 and cs_sold_date_sk    = d_date_sk
 and d_date between '1998-02-08' and '1998-04-08'
;

-- end query 86 in stream 0 using template query40.tpl