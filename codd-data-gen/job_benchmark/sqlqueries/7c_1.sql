SELECT * FROM aka_name AS an, name AS n WHERE n.n_name_pcode_cf BETWEEN 'A' AND 'F' AND (n.n_gender='m' OR (n.n_gender = 'f')) AND n.n_id = an.an_person_id;
