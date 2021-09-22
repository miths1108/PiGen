-- start query 90 in stream 0 using template query18.tpl
select *
 from catalog_sales, customer_demographics, customer, date_dim
 where cs_sold_date_sk = d_date_sk and
       cs_bill_cdemo_sk = cd_demo_sk and
       cs_bill_customer_sk = c_customer_sk and
       cd_gender = 'F' and 
       cd_education_status = '4 yr Degree' and
       c_birth_month in (6,5,12,4,3,7) and
       d_year = 2001
 ;

-- end query 90 in stream 0 using template query18.tpl
