select distinct(ss_net_paid_inc_tax)
                  from store_sales
                  where ss_quantity between 21 and 40;