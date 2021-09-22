select distinct web_site_id
from web_sales, web_site, date_dim
where ws_sold_date_sk = d_date_sk
and ws_web_site_sk = web_site_sk
and d_date between '2001-08-21' and '2001-09-4'
;