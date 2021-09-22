select i_brand_id
       from web_sales
           ,item
           ,date_dim
       where ws_item_sk = i_item_sk
         and ws_sold_date_sk = d_date_sk
         and d_year = 1998+2
         and d_moy = 11
         and i_rec_start_date >= cast('1997-10-27' as date)
       group by i_brand_id;