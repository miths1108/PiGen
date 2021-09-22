select *
  FROM   store_sales, item
  WHERE  ss_item_sk = i_item_sk and
         i_color in ('lavender','metallic','beige','gainsboro','chartreuse','lemon') and
         i_current_price between 6 and 6 + 10 and
         i_current_price between 6 + 1 and 6 + 15
;
