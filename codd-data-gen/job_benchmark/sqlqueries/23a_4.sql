SELECT * FROM company_name AS cn, company_type AS ct, kind_type AS kt, movie_companies AS mc, title AS t WHERE cn.cn1_country_code  = '[us]' AND kt.kt_kind  in ('movie') AND t.t_production_year  > 2000 AND kt.kt_id = t.t_kind_id AND t.t_id = mc.mc_movie_id AND cn.cn1_id = mc.mc_company_id AND ct.ct_id = mc.mc_company_type_id;
