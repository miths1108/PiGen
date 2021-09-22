select * 
 from  store_returns,  date_dim
 where
 d_year = 2001
 and sr_returned_date_sk =  d_date_sk
 and d_moy               between 4 and  10
;