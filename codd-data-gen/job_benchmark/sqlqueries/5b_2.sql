SELECT * FROM info_type AS it, movie_info AS mi, title AS t WHERE mi.mi_info IN ('USA', 'America') AND t.t_production_year > 2010 AND t.t_id = mi.mi_movie_id AND it.it_id = mi.mi_info_type_id;
