-- start query 26 in stream 0 using template query10.tpl
select *
 from
  customer,customer_address
 where
  c_current_addr_sk = ca_address_sk and
  ca_county in ('Yellowstone County','Montgomery County','Divide County','Cedar County','Manassas Park city')
;
