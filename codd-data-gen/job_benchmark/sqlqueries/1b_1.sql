SELECT * FROM company_type AS ct, movie_companies AS mc, title AS t WHERE ct.ct_kind = 'production companies' AND t.t_production_year between 2005 and 2010 AND ct.ct_id = mc.mc_company_type_id AND t.t_id = mc.mc_movie_id;
