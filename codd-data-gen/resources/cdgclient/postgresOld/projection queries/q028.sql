-- start query 28 in stream 0 using template query69.tpl
select distinct cd_gender,
          cd_marital_status,
          cd_education_status,
          cd_purchase_estimate,
          cd_credit_rating
 from
  customer c,customer_address ca, customer_demographics
 where
  c.c_current_addr_sk = ca.ca_address_sk and
cd_demo_sk = c.c_current_cdemo_sk and 
  ca_state in ('SD','KY','MO')
  ;
   
