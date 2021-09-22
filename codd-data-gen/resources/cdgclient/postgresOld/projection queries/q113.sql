select distinct i_item_desc, d_date
  from store_sales, date_dim, item
  where ss_sold_date_sk = d_date_sk
    and ss_item_sk = i_item_sk 
    and d_year in (1999,1999+1,1999+2,1999+3)
;