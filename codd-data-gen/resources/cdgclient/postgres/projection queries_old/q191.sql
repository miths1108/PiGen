select distinct s_store_name, s_company_id, s_street_number, s_street_name, s_street_type, s_suite_number, s_city, s_county, s_state, s_zip
from
   store_sales, store, date_dim
where
    d_year = 2000
and d_moy  = 8
and ss_sold_date_sk   = d_date_sk
and ss_store_sk = s_store_sk
;
