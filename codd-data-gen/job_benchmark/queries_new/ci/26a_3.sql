SELECT distinct ci.ci_note, chn.cn_name,t.t_production_year FROM char_name AS chn, cast_info AS ci, kind_type AS kt, name AS n, title AS t WHERE kt.kt_kind  = 'movie' AND t.t_production_year  > 2000 AND chn.cn_name = 'Zombie' AND kt.kt_id = t.t_kind_id AND t.t_id = ci.ci_movie_id AND chn.cn_id = ci.ci_person_role_id AND n.n_id = ci.ci_person_id;
