SELECT * FROM info_type AS it3, name AS n, person_info AS pi WHERE it3.it_info  = 'height' AND n.n_gender ='f' AND n.n_id = pi.pi_person_id AND it3.it_id = pi.pi_info_type_id;
