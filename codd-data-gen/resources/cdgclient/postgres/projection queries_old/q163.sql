select distinct i_item_id, i_item_desc
 from store_returns, date_dim, item
 where i_item_sk = sr_item_sk
   and sr_returned_date_sk = d_date_sk
   and d_quarter_name in ('2001Q1','2001Q2','2001Q3')
;