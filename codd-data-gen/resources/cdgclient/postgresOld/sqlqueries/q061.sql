-- start query 56 in stream 0 using template query89.tpl
select *
from item, store_sales, date_dim
where ss_item_sk = i_item_sk and
      ss_sold_date_sk = d_date_sk and
      d_year in (2002) and
        ((i_category in ('Jewelry','Women','Shoes') and
          i_class in ('mens watch','dresses','mens')
         )
      or (i_category in ('Men','Sports','Music') and
          i_class in ('sports-apparel','sailing','pop') 
        ))
;

-- end query 56 in stream 0 using template query89.tpl