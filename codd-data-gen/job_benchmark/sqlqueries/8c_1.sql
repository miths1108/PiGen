SELECT * FROM cast_info AS ci, name AS n1, role_type AS rt, title AS t WHERE rt.rt_role ='writer' AND n1.n_id = ci.ci_person_id AND ci.ci_movie_id = t.t_id AND ci.ci_role_id = rt.rt_id;
