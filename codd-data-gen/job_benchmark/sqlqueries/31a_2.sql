SELECT * FROM info_type AS it1, movie_info AS mi, title AS t WHERE it1.it_info  = 'genres' AND mi.mi_info  in ('Horror', 'Thriller') AND t.t_id = mi.mi_movie_id AND it1.it_id = mi.mi_info_type_id;
