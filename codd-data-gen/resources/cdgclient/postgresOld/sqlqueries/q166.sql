select *
from  web_sales, date_dim
where ws_net_profit > 1
	and ws_net_paid > 0
        and ws_quantity > 0
        and ws_sold_date_sk = d_date_sk
        and d_year = 2000
        and d_moy = 12
;
