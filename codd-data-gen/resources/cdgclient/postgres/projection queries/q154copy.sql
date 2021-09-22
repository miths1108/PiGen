select distinct i_item_id,i_item_desc,i_current_price
 from item, catalog_sales
 where i_current_price between 6 and 10
 and i_manufact_id in (691,853,946)
 and cs_item_sk = i_item_sk
 ;

-- end query 31 in stream 0 using template query37.tpl