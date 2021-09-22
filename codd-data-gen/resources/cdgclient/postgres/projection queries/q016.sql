-- start query 21 in stream 0 using template query36.tpl
select distinct i_category,i_class
 from
    store_sales
   ,date_dim
   ,item
   ,store
 where
    d_year = 2001 
 and d_date_sk = ss_sold_date_sk
 and i_item_sk  = ss_item_sk 
 and s_store_sk  = ss_store_sk
 and s_state in ('TN','TN','TN','TN',
                 'TN','TN','TN','TN')
  ;

-- end query 21 in stream 0 using template query36.tpl
