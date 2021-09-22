select distinct s_state, s_county
 from
    store_sales, store, date_dim
 where
    d_month_seq between 1181 and 1181+11
 and d_date_sk = ss_sold_date_sk
and s_store_sk  = ss_store_sk
 ;
