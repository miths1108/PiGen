-- start query 1 in stream 0 using template query18.tpl
select  i_item_id
 from catalog_sales, customer_demographics cd2, customer, customer_address, date_dim, item
 where cs_sold_date_sk = d_date_sk and
       cs_item_sk = i_item_sk and
       cs_bill_customer_sk = c_customer_sk and
       c_current_cdemo_sk = cd2.cd_demo_sk and
       c_current_addr_sk = ca_address_sk and
       c_birth_month in (9,5,12,4,1,10) and
       d_year = 2001 and
       ca_state in ('ND','WI','AL'
                   ,'NC','OK','MS','TN') and
       i_rec_start_date >= cast('1997-10-27' as date)
 group by i_item_id;

-- end query 1 in stream 0 using template query18.tpl
