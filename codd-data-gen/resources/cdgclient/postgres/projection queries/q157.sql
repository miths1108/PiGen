-- start query 33 in stream 0 using template query85.tpl
select distinct r_reason_desc
 from web_returns, customer_demographics, customer_address, reason 
 where cd_demo_sk = wr_returning_cdemo_sk
   and ca_address_sk = wr_refunded_addr_sk
   and r_reason_sk = wr_reason_sk
   and
   (
    (
     cd_marital_status = 'S'
     and
     cd_education_status = '4 yr Degree'
     )
   or
    (
     cd_marital_status = 'M'
     and
     cd_education_status = 'Primary' 
     )
   or
    (
     cd_marital_status = 'U'
     and
     cd_education_status = '2 yr Degree'
     )
   )
   and
   (
    (
     ca_country = 'United States'
     and
     ca_state in ('IL', 'MT', 'AR')
     )
    or
    (
     ca_country = 'United States'
     and
     ca_state in ('WI', 'TX', 'GA')
     )
    or
    (
     ca_country = 'United States'
     and
     ca_state in ('RI', 'KY', 'IN')
    )
   )
;

-- end query 33 in stream 0 using template query85.tpl