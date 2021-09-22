SELECT distinct k.k_keyword FROM keyword AS k, kind_type AS kt, movie_keyword AS mk, title AS t WHERE k.k_keyword  in ( 'blood', 'violence','1940s-fashion') AND kt.kt_kind  in ('movie', 'episode') AND t.t_production_year  > 2008 AND kt.kt_id = t.t_kind_id AND t.t_id = mk.mk_movie_id AND k.k_id = mk.mk_keyword_id;
