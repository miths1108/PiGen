SELECT * FROM company_name AS cn, movie_companies AS mc, title AS t WHERE cn.cn1_country_code ='[us]' AND t.t_production_year  between 2000 and 2010 AND t.t_id = mc.mc_movie_id AND cn.cn1_id = mc.mc_company_id;
