SELECT * FROM info_type AS it1, movie_info AS mi, title AS t WHERE it1.it_info  = 'budget' AND t.t_id = mi.mi_movie_id AND it1.it_id = mi.mi_info_type_id;
