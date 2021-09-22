select *
from catalog_sales, item, date_dim
where cs_sold_date_sk = d_date_sk
         and cs_item_sk = i_item_sk
         and i_category = 'Children'
         and i_class = 'toddlers'
         and d_moy = 5
         and d_year = 2001
;