-- start query 1 in stream 0 using template query3.tpl
select  dt.d_year
 from  date_dim dt 
      ,store_sales
      ,item
 where dt.d_date_sk = store_sales.ss_sold_date_sk
   and store_sales.ss_item_sk = item.i_item_sk
   and item.i_manufact_id = 436
   and dt.d_moy=12
 group by dt.d_year;

-- end query 1 in stream 0 using template query3.tpl
