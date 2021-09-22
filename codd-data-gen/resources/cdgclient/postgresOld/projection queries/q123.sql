select distinct i_brand_id,i_class_id,i_category_id
 from catalog_sales, item, date_dim
 where cs_item_sk = i_item_sk
   and cs_sold_date_sk = d_date_sk
   and d_year = 2000+2 
   and d_moy = 11
;