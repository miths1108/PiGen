select *
from store_returns
,date_dim
where sr_returned_date_sk = d_date_sk
and d_year =1998
;

