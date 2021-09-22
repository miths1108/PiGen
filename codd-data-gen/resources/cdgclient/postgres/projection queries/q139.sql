select distinct i_item_id,i_item_desc 
 from  store_returns,  date_dim,  item
 where
 d_year = 2001
 and sr_returned_date_sk =  d_date_sk
 and d_moy               between 4 and  10
 and sr_item_sk = i_item_sk
;