select *
from web_sales, date_dim
where ws_sold_date_sk=d_date_sk
  and d_month_seq between 1200 and 1200+11;