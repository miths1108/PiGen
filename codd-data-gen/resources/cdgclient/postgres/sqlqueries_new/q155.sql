select *
 from web_sales, date_dim
 where ws_sold_date_sk = d_date_sk 
   and d_year = 2002
   and ws_sales_price between 50.00 and 200.00
   and ws_net_profit between 50 and 300
;

