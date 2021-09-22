SELECT * FROM cast_info AS ci, name AS n, title AS t WHERE ci.ci_note  in ('(writer)', '(head writer)', '(written by)', '(story)', '(story editor)') AND t.t_id = ci.ci_movie_id AND n.n_id = ci.ci_person_id;
