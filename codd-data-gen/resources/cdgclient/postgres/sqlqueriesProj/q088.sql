-- start query 83 in stream 0 using template query56.tpl
select distinct(i_item_id)
from item
where i_color in ('almond','dodger','dim');