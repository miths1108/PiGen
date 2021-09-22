-- start query 68 in stream 0 using template query23.tpl
select *
  from store_sales
      ,date_dim 
  where ss_sold_date_sk = d_date_sk
    and d_year in (1999,1999+1,1999+2,1999+3)
;
