select distinct i_item_id, i_item_desc
 from catalog_sales, date_dim, item
 where i_item_sk = cs_item_sk
   and cs_sold_date_sk = d_date_sk
   and d_quarter_name in ('2001Q1','2001Q2','2001Q3')
;