select *
      from inventory
          ,date_dim
      where inv_date_sk = d_date_sk
        and d_year =1999
;