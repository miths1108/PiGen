-- start query 59 in stream 0 using template query52.tpl
select distinct d_year, i_brand, i_brand_id
 from date_dim, store_sales, item
 where d_date_sk = ss_sold_date_sk
    and ss_item_sk = i_item_sk
    and i_manager_id = 1
    and d_moy=12
    and d_year=2000
 ;

-- end query 59 in stream 0 using template query52.tpl