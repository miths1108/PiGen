SELECT * FROM info_type AS it2, movie_info_idx AS mi_idx, title AS t WHERE it2.it_info = 'rating' AND mi_idx.mii_info  > '8.0' AND t.t_production_year  between 2005 and 2008 AND t.t_id = mi_idx.mii_movie_id AND mi_idx.mii_info_type_id = it2.it_id;