SELECT * FROM keyword AS k, movie_keyword AS mk, title AS t WHERE k.k_keyword  in ('murder', 'blood', 'gore', 'death', 'female-nudity') AND t.t_id = mk.mk_movie_id AND k.k_id = mk.mk_keyword_id;
