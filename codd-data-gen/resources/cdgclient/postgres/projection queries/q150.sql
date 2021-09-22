select distinct i_product_name, s_store_name, s_zip, ca_street_number, ca_street_name, ca_city, ca_zip, d_year
  FROM   store_sales, date_dim, store, customer, customer_address, item
  WHERE  
	ss_store_sk = s_store_sk AND
	ss_item_sk = i_item_sk and
	ss_customer_sk = c_customer_sk AND
        c_current_addr_sk = ca_address_sk and
        c_first_shipto_date_sk = d_date_sk and
        i_color in ('lavender','metallic','beige','gainsboro','chartreuse','lemon') and
        i_current_price between 6 and 6 + 10 and
        i_current_price between 6 + 1 and 6 + 15
;