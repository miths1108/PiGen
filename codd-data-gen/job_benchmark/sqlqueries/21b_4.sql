SELECT * FROM movie_info AS mi, title AS t WHERE mi.mi_info IN ('Germany', 'German') AND t.t_production_year BETWEEN 2000 and 2010 AND mi.mi_movie_id = t.t_id;
