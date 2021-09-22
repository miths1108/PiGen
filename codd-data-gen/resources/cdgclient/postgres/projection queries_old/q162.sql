select distinct i_item_id, i_item_desc, s_state
 from store_sales, date_dim, store, item
 where d_quarter_name = '2001Q1'
   and d_date_sk = ss_sold_date_sk
   and i_item_sk = ss_item_sk
   and s_store_sk = ss_store_sk
;