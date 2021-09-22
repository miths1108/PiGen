SELECT * FROM info_type AS it, name AS n, person_info AS pi WHERE it.it_info ='mini biography' AND n.n_name_pcode_cf BETWEEN 'A' AND 'F' AND (n.n_gender='m' OR (n.n_gender = 'f')) AND pi.pi_note ='Volker Boehm' AND n.n_id = pi.pi_person_id AND it.it_id = pi.pi_info_type_id;