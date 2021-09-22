-- start query 55 in stream 0 using template query22.tpl
select *
       from inventory, date_dim
       where inv_date_sk=d_date_sk
       and d_month_seq between 1199 and 1199 + 11
;

-- end query 55 in stream 0 using template query22.tpl
