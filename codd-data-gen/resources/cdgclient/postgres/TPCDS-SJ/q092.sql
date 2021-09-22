-- start query 82 in stream 0 using template query55.tpl
select *
 from date_dim, store_sales, item
 where d_date_sk = ss_sold_date_sk
 	and ss_item_sk = i_item_sk
 	and i_manager_id=40
 	and d_moy=12
 	and d_year=2001
 ;

-- end query 82 in stream 0 using template query55.tpl