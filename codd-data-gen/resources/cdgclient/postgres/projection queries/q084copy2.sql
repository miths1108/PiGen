-- start query 90 in stream 0 using template query18.tpl
select distinct i_item_id, ca_country, ca_state, ca_county
 from catalog_sales, customer_demographics, customer, customer_address, date_dim, item
 where cs_sold_date_sk = d_date_sk and
       cs_item_sk = i_item_sk and
       cs_bill_cdemo_sk = cd_demo_sk and
       cs_bill_customer_sk = c_customer_sk and
       cd_gender = 'F' and 
       cd_education_status = '4 yr Degree' and
       c_current_addr_sk = ca_address_sk and
       c_birth_month in (1,3,7,8) and
       d_year = 2001 and
       ca_state in ('TX','CO','OH','NY')
 ;

-- end query 90 in stream 0 using template query18.tpl
