SELECT * FROM aka_title AS at, title AS t WHERE t.t_production_year  > 1990 AND t.t_id = at.at_movie_id;
