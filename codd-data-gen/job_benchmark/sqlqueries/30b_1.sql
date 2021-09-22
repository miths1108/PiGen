SELECT * FROM complete_cast AS cc, comp_cast_type AS cct1, title AS t WHERE cct1.cct_kind  = 'cast' AND t.t_id = cc.cc_movie_id AND cct1.cct_id = cc.cc_subject_id;
