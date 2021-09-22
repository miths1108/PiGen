-- start query 18 in stream 0 using template query45.tpl
select *
 from web_sales, date_dim, item
 where ws_item_sk = i_item_sk 
 	and ws_sold_date_sk = d_date_sk
and i_item_id in ( 'AAAAAAAACAAAAAAA', 'AAAAAAAACAAAAAAA', 'AAAAAAAAEAAAAAAA', 'AAAAAAAAHAAAAAAA', 'AAAAAAAAKAAAAAAA', 'AAAAAAAANAAAAAAA', 'AAAAAAAAABAAAAAA', 'AAAAAAAADBAAAAAA', 'AAAAAAAAGBAAAAAA', 'AAAAAAAAMBAAAAAA')
 	and d_qoy = 2 and d_year = 1999
 ;


