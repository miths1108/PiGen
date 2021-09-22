SELECT distinct chn.cn_name FROM char_name AS chn, cast_info AS ci, name AS n, role_type AS rt, title AS t WHERE ci.ci_note  in ('(voice)', '(voice) (uncredited)', '(voice: English version)') AND n.n_gender ='f' AND rt.rt_role ='actress' AND t.t_title  = 'Shrek 2' AND t.t_production_year  between 2000 and 2010 AND t.t_id = ci.ci_movie_id AND n.n_id = ci.ci_person_id AND rt.rt_id = ci.ci_role_id AND chn.cn_id = ci.ci_person_role_id;
