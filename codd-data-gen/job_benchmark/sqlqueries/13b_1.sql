SELECT * FROM info_type AS it, movie_info_idx AS miidx WHERE it.it_info ='rating' AND it.it_id = miidx.mii_info_type_id;
