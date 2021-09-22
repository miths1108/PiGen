select  
    c_custkey,
    c_name,
    c_acctbal,
    n_name,
    c_address,
    c_phone,
    c_comment
from
    nation ,
    customer ,
         orders ,
         lineitem
where
        c_custkey = o_custkey
  and l_orderkey = o_orderkey
  and o_orderdate >= date '1996-06-16'
  and o_orderdate < date '1996-09-15'
  and l_returnflag = 'y'
  and c_nationkey = n_nationkey
group by
    c_custkey,
    c_name,
    c_acctbal,
    c_phone,
    n_name,
    c_address,
    c_comment;




                                                                             QUERY PLAN                                                                              
---------------------------------------------------------------------------------------------------------------------------------------------------------------------
 Limit  (cost=153809.07..153809.07 rows=1 width=280) (actual time=296.721..299.359 rows=0 loops=1)
   ->  Sort  (cost=153809.07..153809.07 rows=1 width=280) (actual time=274.339..276.976 rows=0 loops=1)
         Sort Key: (sum((lineitem.l_extendedprice * ('1'::numeric - lineitem.l_discount)))) DESC
         Sort Method: quicksort  Memory: 25kB
         ->  GroupAggregate  (cost=153808.91..153809.06 rows=1 width=280) (actual time=274.329..276.966 rows=0 loops=1)
               Group Key: customer.c_custkey, nation.n_name
               ->  Gather Merge  (cost=153808.91..153809.03 rows=1 width=260) (actual time=274.326..276.963 rows=0 loops=1)
                     Workers Planned: 2
                     Workers Launched: 2
                     ->  Sort  (cost=152808.89..152808.89 rows=1 width=260) (actual time=248.825..248.828 rows=0 loops=3)
                           Sort Key: customer.c_custkey, nation.n_name
                           Sort Method: quicksort  Memory: 25kB
                           Worker 0:  Sort Method: quicksort  Memory: 25kB
                           Worker 1:  Sort Method: quicksort  Memory: 25kB
                           ->  Nested Loop  (cost=0.99..152808.88 rows=1 width=260) (actual time=248.784..248.787 rows=0 loops=3)
                                 ->  Nested Loop  (cost=0.85..152808.71 rows=1 width=164) (actual time=248.784..248.785 rows=0 loops=3)
                                       ->  Nested Loop  (cost=0.43..152807.99 rows=1 width=20) (actual time=248.783..248.784 rows=0 loops=3)
                                             ->  Parallel Seq Scan on lineitem  (cost=0.00..152799.54 rows=1 width=20) (actual time=248.782..248.782 rows=0 loops=3)
                                                   Filter: (l_returnflag = 'y'::bpchar)
                                                   Rows Removed by Filter: 2000405
                                             ->  Index Scan using orders_pkey on orders  (cost=0.43..8.45 rows=1 width=12) (never executed)
                                                   Index Cond: (o_orderkey = lineitem.l_orderkey)
                                                   Filter: ((o_orderdate >= '1996-06-16'::date) AND (o_orderdate < '1996-09-15'::date))
                                       ->  Index Scan using customer_pkey on customer  (cost=0.42..0.73 rows=1 width=152) (never executed)
                                             Index Cond: (c_custkey = orders.o_custkey)
                                 ->  Index Scan using nation_pkey on nation  (cost=0.14..0.16 rows=1 width=108) (never executed)
                                       Index Cond: (n_nationkey = customer.c_nationkey)
 Planning Time: 1.322 ms
 JIT:
   Functions: 64
   Options: Inlining false, Optimization false, Expressions true, Deforming true
   Timing: Generation 8.951 ms, Inlining 0.000 ms, Optimization 2.953 ms, Emission 43.275 ms, Total 55.180 ms
 Execution Time: 305.024 ms
