SELECT distinct cn.cn1_name FROM company_name AS cn, company_type AS ct, movie_companies AS mc, title AS t WHERE cn.cn1_country_code  = '[us]' AND t.t_production_year = 1990 AND t.t_id = mc.mc_movie_id AND cn.cn1_id = mc.mc_company_id AND ct.ct_id = mc.mc_company_type_id;
