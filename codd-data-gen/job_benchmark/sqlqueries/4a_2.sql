SELECT * FROM keyword AS k, movie_keyword AS mk, title AS t WHERE t.t_production_year > 2005 AND t.t_id = mk.mk_movie_id AND k.k_id = mk.mk_keyword_id;
