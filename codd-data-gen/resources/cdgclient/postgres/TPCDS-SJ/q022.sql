-- start query 25 in stream 0 using template query16.tpl
select *
from
   catalog_sales
  ,date_dim
  ,customer_address
  ,call_center
where
    d_date between '2000-3-01' and '2000-5-01'
and cs_ship_date_sk = d_date_sk
and cs_ship_addr_sk = ca_address_sk
and ca_state = 'MN'
and cs_call_center_sk = cc_call_center_sk
and cc_county in ('Williamson County','Williamson County','Williamson County','Williamson County',
                  'Williamson County')
;

-- end query 25 in stream 0 using template query16.tpl
