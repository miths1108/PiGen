select distinct i_item_id,i_item_desc,i_current_price
 from item, store_sales
where i_manufact_id in (198,999,168,196)
 and ss_item_sk = i_item_sk
;
