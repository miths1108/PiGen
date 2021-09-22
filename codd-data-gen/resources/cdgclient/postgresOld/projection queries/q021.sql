-- start query 24 in stream 0 using template query62.tpl
select distinct w_warehouse_name, sm_type, web_name
from
   web_sales
  ,warehouse
  ,ship_mode
  ,web_site
  ,date_dim
where
    d_month_seq between 1212 and 1212 + 11
and ws_ship_date_sk   = d_date_sk
and ws_warehouse_sk   = w_warehouse_sk
and ws_ship_mode_sk   = sm_ship_mode_sk
and ws_web_site_sk    = web_site_sk
;

-- end query 24 in stream 0 using template query62.tpl
