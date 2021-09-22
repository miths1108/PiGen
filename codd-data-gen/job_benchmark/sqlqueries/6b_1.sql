SELECT * FROM cast_info AS ci, name AS n, title AS t WHERE t.t_production_year > 2014 AND t.t_id = ci.ci_movie_id AND n.n_id = ci.ci_person_id;
