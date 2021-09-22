select *
 from store_returns, date_dim
 where sr_returned_date_sk = d_date_sk
   and d_quarter_name in ('2001Q1','2001Q2','2001Q3')
;
