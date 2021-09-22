select *
 from item, store_sales
where i_manufact_id in (198,999,168,196)
 and ss_item_sk = i_item_sk
;