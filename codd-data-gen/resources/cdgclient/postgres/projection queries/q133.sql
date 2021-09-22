SELECT distinct d_year, i_brand_id, i_class_id, i_category_id, i_manufact_id
        FROM store_sales, date_dim, item
where i_item_sk=ss_item_sk and d_date_sk=ss_sold_date_sk
       and i_category='Shoes';