select distinct ca_state
 from customer_address, customer, store_sales, date_dim, item
 where       ca_address_sk = c_current_addr_sk
 	and c_customer_sk = ss_customer_sk
 	and ss_sold_date_sk = d_date_sk
 	and ss_item_sk = i_item_sk
 	and d_month_seq = 1180
 	and i_current_price > 1.2 * 9.52
;