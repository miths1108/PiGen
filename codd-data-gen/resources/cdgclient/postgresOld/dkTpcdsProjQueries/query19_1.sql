-- start query 1 in stream 0 using template query19.tpl
select i_brand
 from date_dim, store_sales, item,customer,customer_address,store
 where d_date_sk = ss_sold_date_sk
   and ss_item_sk = i_item_sk
   and i_manager_id=7
   and d_moy=11
   and d_year=1999
   and ss_customer_sk = c_customer_sk 
   and c_current_addr_sk = ca_address_sk
   and ss_store_sk = s_store_sk 
 group by i_brand;

-- end query 1 in stream 0 using template query19.tpl
