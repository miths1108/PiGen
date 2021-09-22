SELECT * FROM info_type AS it, movie_info AS mi, title AS t WHERE it.it_info  = 'release dates' AND t.t_title  = 'Shrek 2' AND t.t_production_year  between 2000 and 2010 AND t.t_id = mi.mi_movie_id AND it.it_id = mi.mi_info_type_id;