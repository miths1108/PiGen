select distinct(ss_ext_sales_price)
                  from store_sales
                  where ss_quantity between 21 and 40;