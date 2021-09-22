-- modifying q002.sql
-- start query 2 in stream 0 using template query7.tpl
select distinct(i_item_id)
 from catalog_sales, customer_demographics, date_dim, item, promotion
 where cs_sold_date_sk = d_date_sk and
       cs_item_sk = i_item_sk and
       cs_bill_cdemo_sk = cd_demo_sk and
       cs_promo_sk = p_promo_sk and
       cd_gender = 'M' and 
       cd_marital_status = 'M' and
       cd_education_status = '4 yr Degree' and
       (p_channel_email = 'N' or p_channel_event = 'N') and
       ((d_date between '2000-03-22' and '2000-06-22') or d_year = 2001)
 ;

-- end query 2 in stream 0 using template query7.tpl