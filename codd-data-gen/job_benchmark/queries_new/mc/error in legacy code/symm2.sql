select distinct mc.mc_note, t.t_season_nr FROM company_name AS cn, movie_companies AS mc, title AS t WHERE ((t.t_season_nr<7 and t.t_production_year>1970) or (t.t_season_nr>9 and t.t_production_year<2000))  and t.t_id = mc.mc_movie_id AND cn.cn1_id = mc.mc_company_id;

