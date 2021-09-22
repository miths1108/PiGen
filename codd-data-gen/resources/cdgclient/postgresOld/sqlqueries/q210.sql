select *
  from web_sales, date_dim 
  where ws_sold_date_sk = d_date_sk 
	and d_week_seq between 5114 and 5375
	and d_year = 2001
;