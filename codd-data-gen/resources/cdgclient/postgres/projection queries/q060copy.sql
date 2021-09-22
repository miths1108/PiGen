-- start query 55 in stream 0 using template query22.tpl
select distinct i_product_name,i_brand,i_class,i_category
       from inventory
           ,date_dim
           ,item
           ,warehouse
       where inv_date_sk=d_date_sk
              and inv_item_sk=i_item_sk
              and inv_warehouse_sk = w_warehouse_sk
              and d_month_seq between 1206 and 1206 + 2
;

-- end query 55 in stream 0 using template query22.tpl
