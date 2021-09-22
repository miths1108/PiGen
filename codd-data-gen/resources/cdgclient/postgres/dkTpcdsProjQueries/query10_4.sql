select * 
          from catalog_sales,date_dim,customer c
          where c.c_customer_sk = cs_ship_customer_sk and
                cs_sold_date_sk = d_date_sk and
                d_year = 2002 and
                d_moy between 4 and 4+3;