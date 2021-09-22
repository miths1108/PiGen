SELECT * FROM company_name AS cn, company_type AS ct, movie_companies AS mc, title AS t WHERE (cn.cn1_country_code <'[pl]' OR  cn.cn1_country_code >'[pl]') AND ct.ct_kind ='production companies' AND mc.mc_note = 'EMPTYSTRING' AND t.t_production_year  = 1998 AND t.t_id = mc.mc_movie_id AND mc.mc_company_type_id = ct.ct_id AND mc.mc_company_id = cn.cn1_id;
