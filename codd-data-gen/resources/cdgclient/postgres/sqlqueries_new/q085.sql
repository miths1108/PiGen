-- start query 89 in stream 0 using template query79.tpl
select *
    from store_sales,date_dim,store,household_demographics
    where store_sales.ss_sold_date_sk = date_dim.d_date_sk
    and store_sales.ss_store_sk = store.s_store_sk  
    and store_sales.ss_hdemo_sk = household_demographics.hd_demo_sk
    and (household_demographics.hd_dep_count = 1 or household_demographics.hd_vehicle_count > -1)
    and date_dim.d_dow = 1
    and date_dim.d_year in (1999,1999+1,1999+2) 
    and store.s_number_employees between 200 and 295
;

-- end query 89 in stream 0 using template query79.tpl