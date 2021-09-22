SELECT * FROM info_type AS it, movie_info_idx AS mi_idx, title AS t WHERE it.it_info ='rating' AND mi_idx.mii_info  > '2.0' AND t.t_production_year > 1990 AND t.t_id = mi_idx.mii_movie_id AND it.it_id = mi_idx.mii_info_type_id;
