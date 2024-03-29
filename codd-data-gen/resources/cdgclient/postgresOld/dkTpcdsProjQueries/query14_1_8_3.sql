select i_category_id
       from catalog_sales
           ,item
           ,date_dim
       where cs_item_sk = i_item_sk
         and cs_sold_date_sk = d_date_sk
         and d_year = 1998+2 
         and d_moy = 11
         and i_rec_start_date >= cast('1997-10-27' as date)
       group by i_category_id;