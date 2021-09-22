SELECT * FROM cast_info AS ci, name AS n, title AS t WHERE ci.ci_note  in ('(producer)', '(executive producer)') AND n.n_gender  = 'm' AND t.t_id = ci.ci_movie_id AND n.n_id = ci.ci_person_id;
