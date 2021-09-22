SELECT * FROM info_type AS it2, kind_type AS kt2, movie_info_idx AS mi_idx2, title AS t2 WHERE it2.it_info  = 'rating' AND kt2.kt_kind  in ('tv series') AND mi_idx2.mii_info  < '3.0' AND t2.t_production_year  between 2005 and 2008 AND it2.it_id = mi_idx2.mii_info_type_id AND t2.t_id = mi_idx2.mii_movie_id AND kt2.kt_id = t2.t_kind_id;