SELECT * FROM movie_info AS mi, title AS t WHERE t.t_production_year >2000 AND t.t_id = mi.mi_movie_id;
