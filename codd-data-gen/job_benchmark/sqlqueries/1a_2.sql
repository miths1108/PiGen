SELECT * FROM info_type AS it, movie_info_idx AS mi_idx, title AS t WHERE it.it_info = 'top 250 rank' AND t.t_id = mi_idx.mii_movie_id AND /*mc.mc_movie_id = mi_idx.mii_movie_id AND*/ it.it_id = mi_idx.mii_info_type_id;
