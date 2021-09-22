-- start query 26 in stream 0 using template query10.tpl
select distinct cd_gender,
          cd_marital_status,
          cd_education_status,
          cd_purchase_estimate,
          cd_credit_rating,
          cd_dep_count,
          cd_dep_employed_count,
          cd_dep_college_count
 from
  customer,customer_address, customer_demographics
 where
  c_current_addr_sk = ca_address_sk and
cd_demo_sk = c_current_cdemo_sk and 
  ca_county in ('Yellowstone County','Montgomery County','Divide County','Cedar County','Manassas Park city')
;
