SELECT ca_zip
            FROM customer_address, customer
            WHERE ca_address_sk = c_current_addr_sk and
                  c_preferred_cust_flag='Y' and
                  ca_gmt_offset >= -10.00
            group by ca_zip;