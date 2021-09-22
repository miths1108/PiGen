SELECT * FROM movie_info AS mi, title AS t WHERE mi.mi_info  IN ('Bulgaria') AND t.t_production_year > 2010 AND t.t_id = mi.mi_movie_id;
