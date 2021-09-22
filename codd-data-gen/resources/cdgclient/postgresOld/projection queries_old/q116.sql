-- start query 69 in stream 0 using template query14.tpl

select distinct(i_brand_id,i_class_id,i_category_id)
 from store_sales
     ,item iss
     ,date_dim d1
 where ss_item_sk = iss.i_item_sk
   and ss_sold_date_sk = d1.d_date_sk
   and d1.d_year between 1999 AND 1999 + 2
;   