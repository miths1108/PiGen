select distinct d_week_seq
  from store_sales, date_dim 
  where
	d_date_sk = ss_sold_date_sk;