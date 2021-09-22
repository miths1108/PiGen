SELECT * FROM info_type AS it1, movie_info AS mi, title AS t WHERE it1.it_info  = 'release dates' AND t.t_production_year  > 2000 AND t.t_id = mi.mi_movie_id AND it1.it_id = mi.mi_info_type_id;
