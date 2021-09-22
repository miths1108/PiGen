select *
from catalog_sales, customer_demographics, household_demographics, date_dim
where cs_bill_cdemo_sk = cd_demo_sk
and cs_bill_hdemo_sk = hd_demo_sk
and cs_sold_date_sk = d_date_sk
and hd_buy_potential = 'from1001to5000'
  and d_year = 1998
  and cd_marital_status = 'M'
;
