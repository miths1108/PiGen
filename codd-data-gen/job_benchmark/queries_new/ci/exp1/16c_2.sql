SELECT distinct n.n_name, t.t_episode_nr FROM cast_info AS ci, name AS n, title AS t WHERE t.t_episode_nr < 100 AND n.n_id = ci.ci_person_id AND ci.ci_movie_id = t.t_id;
