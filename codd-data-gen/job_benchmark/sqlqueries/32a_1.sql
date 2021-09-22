SELECT * FROM keyword AS k, movie_keyword AS mk, title AS t1 WHERE k.k_keyword ='based-on-novel' AND mk.mk_keyword_id = k.k_id AND t1.t_id = mk.mk_movie_id AND mk.mk_movie_id = t1.t_id;
