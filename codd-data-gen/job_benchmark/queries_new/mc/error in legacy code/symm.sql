select distinct mc.mc_note, t.t_season_nr FROM company_name AS cn, movie_companies AS mc, title AS t WHERE ((t.t_season_nr<5 and t.t_production_year>1950) or (t.t_season_nr>10 and t.t_production_year<1980))  and t.t_id = mc.mc_movie_id AND cn.cn1_id = mc.mc_company_id;

