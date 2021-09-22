select distinct t.t_production_year, t.t_series_years FROM company_type AS ct, movie_companies AS mc, title AS t WHERE ((ct.ct_kind in ('production companies','distributors') AND t.t_production_year >2005) OR (ct.ct_kind in ('special effects companies','miscellaneous companies') AND t.t_production_year <2000)) AND ct.ct_id = mc.mc_company_type_id AND t.t_id = mc.mc_movie_id;