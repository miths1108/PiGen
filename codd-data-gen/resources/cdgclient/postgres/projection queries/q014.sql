select distinct ca_zip, ca_state
 from web_sales, customer, customer_address, date_dim, item
 where ws_bill_customer_sk = c_customer_sk
 	and c_current_addr_sk = ca_address_sk 
	and ws_item_sk = i_item_sk 
 	and ws_sold_date_sk = d_date_sk
and i_item_id in ( 'AAAAAAAACAAAAAAA', 'AAAAAAAACAAAAAAA', 'AAAAAAAAEAAAAAAA', 'AAAAAAAAHAAAAAAA', 'AAAAAAAAKAAAAAAA', 'AAAAAAAANAAAAAAA', 'AAAAAAAAABAAAAAA', 'AAAAAAAADBAAAAAA', 'AAAAAAAAGBAAAAAA', 'AAAAAAAAMBAAAAAA')
 	and d_qoy = 2 and d_year = 1999
 ;


