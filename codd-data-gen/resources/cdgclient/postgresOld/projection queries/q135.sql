select  distinct s_store_id 
  from store_sales, date_dim, store, item, promotion
 where ss_sold_date_sk = d_date_sk
       and d_date between '1999-08-25' and '1999-09-25'
       and ss_store_sk = s_store_sk
       and ss_item_sk = i_item_sk
       and i_current_price > 50
       and ss_promo_sk = p_promo_sk
       and p_channel_tv = 'N'
;