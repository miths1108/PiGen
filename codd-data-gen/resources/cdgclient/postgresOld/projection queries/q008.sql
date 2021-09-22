select distinct cc_call_center_id, cc_name, cc_manager
from
        call_center, catalog_returns, date_dim
where
        cr_call_center_sk       = cc_call_center_sk
and     cr_returned_date_sk     = d_date_sk
and     d_year                  = 2000 
and     d_moy                   = 12
;
