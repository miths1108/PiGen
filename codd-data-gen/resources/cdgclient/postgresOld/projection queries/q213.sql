-- start query 87 in stream 0 using template query72.tpl
select distinct i_item_desc, d_week_seq
from catalog_sales, item, customer_demographics, household_demographics, date_dim
where i_item_sk = cs_item_sk
and cs_bill_cdemo_sk = cd_demo_sk
and cs_bill_hdemo_sk = hd_demo_sk
and cs_sold_date_sk = d_date_sk
and hd_buy_potential = 'from1001to5000'
  and d_year = 1998
  and cd_marital_status = 'M'
;