-- start query 1 in stream 0 using template query6.tpl
select  a.ca_state state
 from customer_address a
     ,customer c
     ,store_sales s
     ,date_dim d
     ,item i
 where       a.ca_address_sk = c.c_current_addr_sk
 	and c.c_customer_sk = s.ss_customer_sk
 	and s.ss_sold_date_sk = d.d_date_sk
 	and s.ss_item_sk = i.i_item_sk
 	and d.d_month_seq = 1201
 	and i.i_current_price > 1.2 * 9
 	and a.ca_gmt_offset >= -10.00
 group by a.ca_state;

-- end query 1 in stream 0 using template query6.tpl
