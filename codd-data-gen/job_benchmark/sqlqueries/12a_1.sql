SELECT * FROM movie_info AS mi, title AS t WHERE mi.mi_info  in ('Drama', 'Horror') AND t.t_production_year  between 2005 and 2008 AND t.t_id = mi.mi_movie_id;
