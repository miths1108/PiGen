SELECT * FROM aka_title AS at, title AS t WHERE t.t_production_year  between 2005 and 2010 AND t.t_id = at.at_movie_id;
