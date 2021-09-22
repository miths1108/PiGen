SELECT * FROM kind_type AS kt1, link_type AS lt, movie_link AS ml, title AS t1, title AS t2 WHERE kt1.kt_kind  in ('tv series') AND t2.t_production_year  = 2007 AND lt.lt_id = ml.ml_link_type_id AND t1.t_id = ml.ml_movie_id AND t2.t_id = ml.ml_linked_movie_id AND kt1.kt_id = t1.t_kind_id;