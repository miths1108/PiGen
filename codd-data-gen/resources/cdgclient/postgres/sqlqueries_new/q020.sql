-- start query 23 in stream 0 using template query46.tpl
select *
    from store_sales,date_dim,store,household_demographics 
    where store_sales.ss_sold_date_sk = date_dim.d_date_sk
    and store_sales.ss_store_sk = store.s_store_sk  
    and store_sales.ss_hdemo_sk = household_demographics.hd_demo_sk
    and (household_demographics.hd_dep_count = 8 or
         household_demographics.hd_vehicle_count= -1)
    and date_dim.d_dow in (6,0)
    and date_dim.d_year in (2000,2000+1,2000+2) 
    and store.s_city in ('Midway','Fairview','Fairview','Fairview','Fairview') 
  ;

-- end query 23 in stream 0 using template query46.tpl