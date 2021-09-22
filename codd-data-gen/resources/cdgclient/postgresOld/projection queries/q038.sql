-- start query 35 in stream 0 using template query67.tpl
select distinct i_category, i_class, i_brand, i_product_name, d_year, d_qoy, d_moy, s_store_id
            from store_sales
                ,date_dim
                ,item, store
       where  ss_sold_date_sk=d_date_sk
          and ss_item_sk=i_item_sk
	and ss_store_sk = s_store_sk
          and d_month_seq between 1214 and 1214+11
;

-- end query 35 in stream 0 using template query67.tpl
