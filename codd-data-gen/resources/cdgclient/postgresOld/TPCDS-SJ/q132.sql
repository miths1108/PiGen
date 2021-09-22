SELECT *
 FROM catalog_sales, item
where i_item_sk=cs_item_sk
       and i_category='Shoes';
