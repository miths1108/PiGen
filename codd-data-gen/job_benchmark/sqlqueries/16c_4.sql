SELECT * FROM company_name AS cn, movie_companies AS mc, title AS t WHERE cn.cn1_country_code ='[us]' AND t.t_episode_nr < 100 AND t.t_id = mc.mc_movie_id AND mc.mc_company_id = cn.cn1_id;
