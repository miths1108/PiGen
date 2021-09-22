select *
from  store_sales, store, date_dim, customer, customer_address, item
   where ss_sold_date_sk = d_date_sk
   and   ss_store_sk = s_store_sk
   and   ss_customer_sk= c_customer_sk
   and   ca_address_sk = c_current_addr_sk
   and   ss_item_sk = i_item_sk
   and   ca_gmt_offset = -6
   and   i_category = 'Electronics'
   and   s_gmt_offset = -5
   and   d_year = 2000
   and   d_moy  = 11
;

-- end query 97 in stream 0 using template query61.tpl