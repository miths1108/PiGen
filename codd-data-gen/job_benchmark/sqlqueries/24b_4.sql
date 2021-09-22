SELECT * FROM keyword AS k, movie_keyword AS mk, title AS t WHERE k.k_keyword  in ('hero', 'martial-arts', 'hand-to-hand-combat', 'computer-animated-movie') AND t.t_production_year  > 2010 AND t.t_id = mk.mk_movie_id AND k.k_id = mk.mk_keyword_id;
