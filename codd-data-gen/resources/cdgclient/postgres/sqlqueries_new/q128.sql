-- start query 90 in stream 0 using template query18.tpl
select *
 from customer_address
 where ca_state in ('TN','IL','GA'
                   ,'MO','CO','OH','NM')
 ;

-- end query 90 in stream 0 using template query18.tpl
