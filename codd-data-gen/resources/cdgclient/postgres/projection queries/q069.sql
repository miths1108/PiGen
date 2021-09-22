select distinct ca_zip
from customer_address, customer
WHERE ca_address_sk = c_current_addr_sk and
      c_preferred_cust_flag='Y'
;