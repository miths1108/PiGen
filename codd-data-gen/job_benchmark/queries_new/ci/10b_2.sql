SELECT distinct ci.ci_note, chn.cn_name,t.t_production_year FROM char_name AS chn, cast_info AS ci, role_type AS rt, title AS t WHERE rt.rt_role  = 'actor' AND t.t_production_year > 2010 AND chn.cn_name IN ('Himself', 'Herself') AND t.t_id = ci.ci_movie_id AND chn.cn_id = ci.ci_person_role_id AND rt.rt_id = ci.ci_role_id;
