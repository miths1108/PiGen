-- start query 45 in stream 0 using template query3.tpl
select distinct d_year,i_brand,i_brand_id
 from  date_dim 
      ,store_sales
      ,item
 where d_date_sk = ss_sold_date_sk
   and ss_item_sk = i_item_sk
   and i_manufact_id = 783
   and d_moy=11
 ;

-- end query 45 in stream 0 using template query3.tpl
