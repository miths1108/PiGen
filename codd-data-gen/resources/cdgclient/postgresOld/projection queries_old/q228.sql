-- start query 96 in stream 0 using template query83.tpl
select distinct i_item_id
 from store_returns, item, date_dim
 where sr_item_sk = i_item_sk
 and   sr_returned_date_sk   = d_date_sk
 and   d_date in ('1999-11-11', '1999-10-02', '1999-04-17', '1999-09-29', '1999-11-14', '1999-04-15', '1999-11-10', '1999-11-13', '1999-09-28', '1999-10-03', '1999-04-13', '1999-11-12', '1999-11-15', '1999-04-16', '1999-10-01', '1999-04-19', '1999-04-18', '1999-11-09', '1999-04-14', '1999-09-30')
;