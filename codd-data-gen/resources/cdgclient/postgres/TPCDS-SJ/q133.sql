SELECT *
        FROM store_sales, item
where i_item_sk=ss_item_sk
       and i_category='Shoes';
