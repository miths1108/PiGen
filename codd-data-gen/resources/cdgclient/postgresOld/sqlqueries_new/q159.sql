select *
 from catalog_returns
     ,date_dim
 where cr_returned_date_sk = d_date_sk 
   and d_year =1998
;