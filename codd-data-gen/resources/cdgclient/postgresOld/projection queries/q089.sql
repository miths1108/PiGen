select distinct i_item_id
 from
 	store_sales,
 	date_dim,
         customer_address,
         item
 where ss_item_sk              = i_item_sk
 and     ss_sold_date_sk         = d_date_sk
 and     d_year                  = 1998
 and     d_moy                   = 1
 and     ss_addr_sk              = ca_address_sk
 and     ca_gmt_offset           = -5 
and i_color in ('almond','dodger','dim')
 ;
 
