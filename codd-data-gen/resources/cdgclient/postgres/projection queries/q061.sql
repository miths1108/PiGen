select distinct i_category, i_brand, s_store_name, s_company_name,d_moy
from item, store_sales, date_dim, store
where ss_item_sk = i_item_sk and
      ss_sold_date_sk = d_date_sk and
      ss_store_sk = s_store_sk and
      d_year in (2002) and
        ((i_category in ('Jewelry','Women') and
          i_class in ('mens watch','dresses')
         )
      or (i_category in ('Men','Sports') and
          i_class in ('sports-apparel','sailing') 
        ))
;