-- start query 11 in stream 0 using template query86.tpl
select distinct(i_category,i_class)
 from
    web_sales
   ,date_dim d1
   ,item
 where
    d1.d_month_seq between 1211 and 1211+11
 and d1.d_date_sk = ws_sold_date_sk
 and i_item_sk  = ws_item_sk
;

-- end query 11 in stream 0 using template query86.tpl