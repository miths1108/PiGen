select distinct i_class_id 
from store_sales, item 
where ss_item_sk=i_item_sk and 
ss_sales_price between 20 and 100 and 
i_class_id between 4 and 12
and i_manufact_id>100;