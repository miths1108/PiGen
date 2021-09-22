select *
  from catalog_sales, date_dim 
  where cs_sold_date_sk = d_date_sk 
	and d_week_seq between 5114 and 5376
	and d_year = 2002
;