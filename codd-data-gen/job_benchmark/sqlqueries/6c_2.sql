SELECT * FROM keyword AS k, movie_keyword AS mk, title AS t WHERE k.k_keyword = 'marvel-cinematic-universe' AND t.t_production_year > 2014 AND k.k_id = mk.mk_keyword_id AND t.t_id = mk.mk_movie_id;
