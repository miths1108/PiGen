SELECT * FROM info_type AS it2, movie_info_idx AS mi_idx, title AS t WHERE it2.it_info  = 'votes' AND t.t_id = mi_idx.mii_movie_id AND it2.it_id = mi_idx.mii_info_type_id;
