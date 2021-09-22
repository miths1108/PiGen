-- start query 17 in stream 0 using template query94.tpl
select *
from
   web_sales 
  ,web_site
where
    ws_web_site_sk = web_site_sk
and web_company_name = 'pri'
;

-- end query 17 in stream 0 using template query94.tpl
