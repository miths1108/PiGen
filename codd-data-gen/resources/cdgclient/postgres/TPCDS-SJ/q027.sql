-- start query 27 in stream 0 using template query63.tpl
select *
from item, store_sales, date_dim
where ss_item_sk = i_item_sk
and ss_sold_date_sk = d_date_sk
and d_month_seq in (1206, 1206+1, 1206+2, 1206+3, 1206+4, 1206+5, 1206+6, 1206+7, 1206+8, 1206+9, 1206+10, 1206+11)
and (
  (i_category in ('Books','Children','Electronics')
   and i_class in ('personal','portable','refernece','self-help')
    and i_brand in ('scholaramalgamalg #14', 'scholaramalgamalg #7', 'exportiunivamalg #9', 'scholaramalgamalg #9')
    )
    or
     (i_category in ('Women','Music','Men')
       and i_class in ('accessories','classical','fragrances','pants')
       and i_brand in ('amalgimporto #1', 'edu packscholar #1',' exportiimporto #1', 'importoamalg #1')
      )
)
;

-- end query 27 in stream 0 using template query63.tpl