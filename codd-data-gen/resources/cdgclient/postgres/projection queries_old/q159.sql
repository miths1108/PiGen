select distinct ca_state
 from catalog_returns
     ,date_dim
     ,customer_address
 where cr_returned_date_sk = d_date_sk 
   and d_year =1998
   and cr_returning_addr_sk = ca_address_sk 
;