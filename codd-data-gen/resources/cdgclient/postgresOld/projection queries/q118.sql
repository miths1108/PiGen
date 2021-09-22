select distinct i_brand_id,i_class_id,i_category_id
 from web_sales, item, date_dim
 where ws_item_sk = i_item_sk
   and ws_sold_date_sk = d_date_sk
   and d_year between 1999 AND 1999 + 2
;