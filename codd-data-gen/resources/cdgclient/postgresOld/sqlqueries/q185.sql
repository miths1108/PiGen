select *
from store_returns, reason
            where sr_reason_sk = r_reason_sk
              and r_reason_desc = 'reason 25'
;
