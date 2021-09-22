select distinct mc.mc_note, t.t_season_nr FROM company_name AS cn, movie_companies AS mc, title AS t WHERE t.t_season_nr between 24 and 30 and t.t_episode_nr between 150 and 500 and t.t_id = mc.mc_movie_id AND cn.cn1_id = mc.mc_company_id;

