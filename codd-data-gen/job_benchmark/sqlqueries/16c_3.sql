SELECT * FROM keyword AS k, movie_keyword AS mk, title AS t WHERE k.k_keyword ='character-name-in-title' AND t.t_episode_nr < 100 AND t.t_id = mk.mk_movie_id AND mk.mk_keyword_id = k.k_id;
