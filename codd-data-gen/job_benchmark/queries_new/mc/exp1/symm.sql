select distinct mc.mc_note, t.t_season_nr FROM company_name AS cn, movie_companies AS mc, title AS t WHERE  t.t_season_nr>=20 and t.t_episode_nr between 100 and 300  and t.t_id = mc.mc_movie_id AND cn.cn1_id = mc.mc_company_id;

