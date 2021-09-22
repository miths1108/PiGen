-- start query 5 in stream 0 using template query39.tpl
select distinct w_warehouse_name,d_moy
      from inventory
         ,warehouse
          ,date_dim
      where inv_warehouse_sk = w_warehouse_sk
        and inv_date_sk = d_date_sk
        and d_year =1999
;

-- end query 5 in stream 0 using template query39.tpl
