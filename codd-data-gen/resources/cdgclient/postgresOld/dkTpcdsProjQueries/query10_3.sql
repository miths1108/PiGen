select *
          from web_sales,date_dim,customer c
          where c.c_customer_sk = ws_bill_customer_sk and
                ws_sold_date_sk = d_date_sk and
                d_year = 2002 and
                d_moy between 4 ANd 4+3;