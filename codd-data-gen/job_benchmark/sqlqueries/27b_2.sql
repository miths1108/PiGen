SELECT * FROM movie_info AS mi, title AS t WHERE mi.mi_info IN ('Sweden', 'Germany','Swedish', 'German') AND t.t_production_year  = 1998 AND mi.mi_movie_id = t.t_id;
