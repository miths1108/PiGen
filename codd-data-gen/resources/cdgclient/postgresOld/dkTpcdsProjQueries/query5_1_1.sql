select s_store_id
from store_sales,
     date_dim,
     store
 where ss_sold_date_sk = d_date_sk
       and d_date between cast('1998-08-04' as date) 
                  and (cast('1998-08-04' as date) +  14)
       and ss_store_sk = s_store_sk
       and s_rec_start_date >= cast('1997-03-13' as date) 
 group by s_store_id;