select d_week_seq
from web_sales 
     ,date_dim
 where d_date_sk = ws_sold_date_sk
 and d_year >= 1900
 group by d_week_seq;