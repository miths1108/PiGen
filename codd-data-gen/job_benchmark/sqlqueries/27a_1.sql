SELECT * FROM complete_cast AS cc, comp_cast_type AS cct1, title AS t WHERE cct1.cct_kind  in ('cast', 'crew') AND t.t_production_year BETWEEN 1950 and 2000 AND t.t_id = cc.cc_movie_id AND cct1.cct_id = cc.cc_subject_id;
