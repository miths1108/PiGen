select distinct i_item_id,i_item_desc 
 from  catalog_sales, date_dim, item
 where
 d_year = 2001
 and d_moy               between 4 and  10
  and i_item_sk = cs_item_sk
 and cs_sold_date_sk = d_date_sk
;