select *
from store_sales, date_dim
where  	ss_net_profit > 1
	and ss_net_paid > 0 
	and ss_quantity > 0
	and ss_sold_date_sk = d_date_sk
        and d_year = 2000
        and d_moy = 12
;
