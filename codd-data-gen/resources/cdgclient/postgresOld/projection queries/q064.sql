-- start query 61 in stream 0 using template query42.tpl
select distinct d_year, i_category_id, i_category
 from 	date_dim, store_sales, item
 where d_date_sk = ss_sold_date_sk
 	and ss_item_sk = i_item_sk
 	and i_manager_id = 1  	
 	and d_moy=11
 	and d_year=2001
 ;

-- end query 61 in stream 0 using template query42.tpl