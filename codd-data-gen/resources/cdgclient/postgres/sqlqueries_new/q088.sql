-- start query 25 in stream 0 using template query16.tpl
select *
from
   date_dim
where
    d_date between '2000-3-01' and '2000-5-01'
;

-- end query 25 in stream 0 using template query16.tpl
