-- start query 1 in stream 0 using template query16.tpl
select  cs_order_number
from
   catalog_sales cs1
  ,date_dim
  ,customer_address
  ,call_center
where
    d_date between '1999-5-01' and 
           (cast('1999-5-01' as date) + 60)
and cs1.cs_ship_date_sk = d_date_sk
and cs1.cs_ship_addr_sk = ca_address_sk
and ca_state = 'OH'
and cs1.cs_call_center_sk = cc_call_center_sk
and cc_county in ('Ziebach County','Williamson County','Walker County','Williamson County',
                  'Ziebach County'
)

group by cs_order_number;

-- end query 1 in stream 0 using template query16.tpl
