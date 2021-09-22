SELECT * FROM link_type AS lt, movie_link AS ml, title AS t WHERE t.t_production_year  = 1998 AND lt.lt_id = ml.ml_link_type_id AND ml.ml_movie_id = t.t_id;
