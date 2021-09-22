SELECT * FROM char_name AS chn, cast_info AS ci, name AS n, role_type AS rt, title AS t WHERE ci.ci_note  in ('(voice)', '(voice: Japanese version)', '(voice) (uncredited)', '(voice: English version)') AND n.n_gender ='f' AND rt.rt_role ='actress' AND chn.cn_name = 'Computer' AND ci.ci_movie_id = t.t_id AND ci.ci_role_id = rt.rt_id AND n.n_id = ci.ci_person_id AND chn.cn_id = ci.ci_person_role_id;
