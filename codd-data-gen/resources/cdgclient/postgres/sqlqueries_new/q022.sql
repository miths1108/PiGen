-- start query 25 in stream 0 using template query16.tpl
select *
from
   catalog_sales
  ,call_center
where
    cs_call_center_sk = cc_call_center_sk
and cc_county in ('Williamson County','Williamson County','Williamson County','Williamson County',
                  'Williamson County')
;

-- end query 25 in stream 0 using template query16.tpl
