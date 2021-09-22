-- start query 59 in stream 0 using template query52.tpl
select distinct(dt.d_year,item.i_brand,item.i_brand_id)
 from date_dim dt
     ,store_sales
     ,item
 where dt.d_date_sk = store_sales.ss_sold_date_sk
    and store_sales.ss_item_sk = item.i_item_sk
    and item.i_manager_id = 1
    and dt.d_moy=12
    and dt.d_year=2000
 ;

-- end query 59 in stream 0 using template query52.tpl