select web_site_id
    from  web_sales,
     date_dim,
     web_site
 where ws_sold_date_sk = d_date_sk
       and d_date between cast('1998-08-04' as date)
                  and (cast('1998-08-04' as date) +  14)
       and ws_web_site_sk = web_site_sk
       and web_rec_start_date >= cast('1997-08-16' as date)
 group by web_site_id;