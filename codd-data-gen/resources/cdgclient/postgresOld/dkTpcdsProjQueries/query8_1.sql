-- start query 1 in stream 0 using template query8.tpl
select  s_store_name
 from store_sales
     ,date_dim
     ,store
 where ss_store_sk = s_store_sk
  and ss_sold_date_sk = d_date_sk
  and d_qoy = 1 and d_year = 2002
  and s_rec_start_date >= cast('1997-03-13' as date)
 group by s_store_name;

-- end query 1 in stream 0 using template query8.tpl
