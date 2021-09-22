select distinct i_category,i_class
 from
    web_sales
   ,date_dim
   ,item
 where
    d_month_seq between 1211 and 1211+11
 and d_date_sk = ws_sold_date_sk
 and i_item_sk  = ws_item_sk
;
