SELECT * FROM kind_type AS kt, movie_keyword AS mk, title AS t WHERE kt.kt_kind  = 'movie' AND t.t_production_year  > 1950 AND kt.kt_id = t.t_kind_id AND t.t_id = mk.mk_movie_id;
