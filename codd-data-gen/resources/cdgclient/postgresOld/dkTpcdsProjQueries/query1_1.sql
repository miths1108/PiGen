SELECT   sr_customer_sk
         FROM     store_returns , 
                  date_dim 
         WHERE    sr_returned_date_sk = d_date_sk 
         AND      d_year =2000;