select *
from web_sales,date_dim, item, time_dim
where d_date_sk = ws_sold_date_sk
and i_item_sk = ws_item_sk
and t_time_sk = ws_sold_time_sk
and d_moy=12
and d_year=2002
and i_manager_id=1
and (t_meal_time = 'breakfast' or t_meal_time = 'dinner')
;
