select distinct ca_state
 from web_returns, date_dim, customer_address
 where wr_returned_date_sk = d_date_sk 
   and d_year =2000
   and wr_returning_addr_sk = ca_address_sk 
;
