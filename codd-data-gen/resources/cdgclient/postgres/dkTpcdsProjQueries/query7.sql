-- start query 1 in stream 0 using template query7.tpl
select  i_item_id
 from store_sales, customer_demographics, date_dim, item, promotion
 where ss_sold_date_sk = d_date_sk and
       ss_item_sk = i_item_sk and
       ss_cdemo_sk = cd_demo_sk and
       ss_promo_sk = p_promo_sk and
       cd_gender = 'F' and 
       cd_marital_status = 'W' and
       cd_education_status = 'Primary' and
       (p_channel_email = 'N' or p_channel_event = 'N') and
       d_year = 1998 and
       i_rec_start_date >= cast('1997-10-27' as date)
 group by i_item_id;

-- end query 1 in stream 0 using template query7.tpl
