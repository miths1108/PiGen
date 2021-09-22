select *
from web_returns, date_dim
where wr_returned_date_sk = d_date_sk
and d_date between '2001-08-21' and '2001-09-4'
;