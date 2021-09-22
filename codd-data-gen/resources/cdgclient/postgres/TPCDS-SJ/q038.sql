-- start query 35 in stream 0 using template query67.tpl
select *
            from store_sales
                ,date_dim
       where  ss_sold_date_sk=d_date_sk
          and d_month_seq between 1214 and 1214+11
;

-- end query 35 in stream 0 using template query67.tpl
