SELECT * FROM info_type AS it2, kind_type AS kt, movie_info_idx AS mi_idx, title AS t WHERE it2.it_info  = 'rating' AND kt.kt_kind  in ('movie', 'episode') AND mi_idx.mii_info  < '7.0' AND t.t_production_year  > 2009 AND kt.kt_id = t.t_kind_id AND t.t_id = mi_idx.mii_movie_id AND it2.it_id = mi_idx.mii_info_type_id;
