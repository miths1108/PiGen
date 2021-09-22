select  *
  from web_sales,
     date_dim,
     item,
     promotion
 where ws_sold_date_sk = d_date_sk
       and d_date between '1999-08-25' and '1999-09-25'
       and ws_item_sk = i_item_sk
       and i_current_price > 50
       and ws_promo_sk = p_promo_sk
       and p_channel_tv = 'N'
;