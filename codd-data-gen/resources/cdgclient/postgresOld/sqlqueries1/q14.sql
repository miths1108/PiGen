-- query 13

select distinct
	c_custkey
from
	customer, orders
where
	c_custkey = o_custkey
	--	and o_comment not like '%pending%packages%'
;
