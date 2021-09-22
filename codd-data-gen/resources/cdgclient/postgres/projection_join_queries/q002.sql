select distinct i_class_id 
from store_sales, item 
where ss_item_sk=i_item_sk and 
ss_sales_price<30 and 
i_class_id between 8 and 16
and i_manufact_id<300;

