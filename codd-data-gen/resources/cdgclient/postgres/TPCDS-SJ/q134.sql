SELECT *
        FROM web_sales, item
where i_item_sk=ws_item_sk
       and i_category='Shoes';
