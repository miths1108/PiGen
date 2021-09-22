-- query 12

select distinct
	l_shipmode
from
	orders,
	lineitem
where
	o_orderkey = l_orderkey
	and l_shipmode in ('TRUCK', 'AIR')
	-- and l_commitdate < l_receiptdate
	-- and l_shipdate < l_commitdate
	and l_receiptdate >= '1997-01-01'
	and l_receiptdate < '1998-01-01'
;