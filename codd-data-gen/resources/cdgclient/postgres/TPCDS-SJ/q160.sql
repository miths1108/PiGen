select *
 from catalog_returns
     ,customer_address
     ,customer
 where 
       ca_address_sk = c_current_addr_sk
       and ca_state = 'TN'
       and cr_returning_customer_sk = c_customer_sk
 ;