-- start query 39 in stream 0 using template query66.tpl
select distinct w_warehouse_name, w_warehouse_sq_ft, w_city, w_county, w_state, w_country, d_year
     from
          web_sales, warehouse, date_dim, time_dim, ship_mode
     where
            ws_sold_date_sk = d_date_sk
        and ws_warehouse_sk =  w_warehouse_sk 
	and ws_sold_time_sk = t_time_sk
 	and ws_ship_mode_sk = sm_ship_mode_sk
        and d_year = 1999
 	and t_time between 46185 and 46185+28800 
 	and sm_carrier in ('DIAMOND','ZOUROS')
;
