select *
 from customer_address, customer
 where ca_address_sk = c_current_addr_sk
       and ca_state = 'IL'
;
