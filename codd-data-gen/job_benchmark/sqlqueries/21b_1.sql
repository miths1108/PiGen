SELECT * FROM link_type AS lt, movie_link AS ml, title AS t WHERE t.t_production_year BETWEEN 2000 and 2010 AND lt.lt_id = ml.ml_link_type_id AND ml.ml_movie_id = t.t_id;
