SELECT * FROM cast_info AS ci, name AS n, title AS t WHERE n.n_gender='m' AND t.t_production_year BETWEEN 1980 AND 1984 AND ci.ci_person_id = n.n_id AND t.t_id = ci.ci_movie_id;

