SELECT distinct k.k_keyword FROM keyword AS k, movie_keyword AS mk, title AS t WHERE k.k_keyword  in ('violence', 'blood') AND t.t_id = mk.mk_movie_id AND k.k_id = mk.mk_keyword_id;
