SELECT * FROM info_type AS it1, movie_info AS mi WHERE it1.it_info = 'genres' AND mi.mi_info  in ('Drama', 'Horror') AND mi.mi_info_type_id = it1.it_id;
