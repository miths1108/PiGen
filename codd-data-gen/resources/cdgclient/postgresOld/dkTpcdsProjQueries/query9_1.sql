select distinct(ss_ext_sales_price) 
                  from store_sales 
                  where ss_quantity between 1 and 20;