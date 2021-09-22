-- start query 17 in stream 0 using template query94.tpl
select *
from
   web_sales 
  ,date_dim
  ,customer_address
  ,web_site
where
    d_date between '2002-4-01' and '2002-6-01'
and  ws_ship_date_sk = d_date_sk
and  ws_ship_addr_sk = ca_address_sk
and ca_state = 'MN'
and  ws_web_site_sk = web_site_sk
and web_company_name = 'pri'
;

-- end query 17 in stream 0 using template query94.tpl
