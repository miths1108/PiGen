SELECT * FROM char_name AS chn, cast_info AS ci, name AS n, role_type AS rt, title AS t WHERE ci.ci_note  = '(voice)' AND n.n_gender ='f' AND rt.rt_role ='actress' AND t.t_production_year  between 2007 and 2008 AND chn.cn_name IN ('Himself', 'Herself') AND t.t_id = ci.ci_movie_id AND n.n_id = ci.ci_person_id AND rt.rt_id = ci.ci_role_id AND chn.cn_id = ci.ci_person_role_id;