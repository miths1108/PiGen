SELECT distinct t.t_production_year,t.t_series_years FROM company_name AS cn, movie_companies AS mc, title AS t WHERE cn.cn1_country_code ='[sm]' AND cn.cn1_id = mc.mc_company_id AND mc.mc_movie_id = t.t_id;
