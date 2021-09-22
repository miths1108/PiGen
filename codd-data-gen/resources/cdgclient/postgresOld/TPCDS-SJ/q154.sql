select *
 from item, catalog_sales
 where i_current_price between 4 and 10
 and i_manufact_id in (744,691,853,946)
 and cs_item_sk = i_item_sk
 ;

-- end query 31 in stream 0 using template query37.tpl
