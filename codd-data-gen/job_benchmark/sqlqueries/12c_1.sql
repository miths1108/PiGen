SELECT * FROM movie_info AS mi, title AS t WHERE mi.mi_info  in ('Drama', 'Horror', 'Western', 'Family') AND t.t_production_year  between 2000 and 2010 AND t.t_id = mi.mi_movie_id;
