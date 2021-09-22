create index company_id_movie_companies on movie_companies(company_id);
create index company_type_id_movie_companies on movie_companies(company_type_id);
create index info_type_id_movie_info_idx on movie_info_idx(info_type_id);
create index info_type_id_movie_info on movie_info(info_type_id);
create index info_type_id_person_info on person_info(info_type_id);
create index keyword_id_movie_keyword on movie_keyword(keyword_id);
create index kind_id_aka_title on aka_title(kind_id);
create index kind_id_title on title(kind_id);
create index linked_movie_id_movie_link on movie_link(linked_movie_id);
create index link_type_id_movie_link on movie_link(link_type_id);
create index movie_id_aka_title on aka_title(movie_id);
create index movie_id_cast_info on cast_info(movie_id);
create index movie_id_complete_cast on complete_cast(movie_id);
create index movie_id_movie_companies on movie_companies(movie_id);
create index movie_id_movie_info_idx on movie_info_idx(movie_id);
create index movie_id_movie_keyword on movie_keyword(movie_id);
create index movie_id_movie_link on movie_link(movie_id);
create index movie_id_movie_info on movie_info(movie_id);
create index person_id_aka_name on aka_name(person_id);
create index person_id_cast_info on cast_info(person_id);
create index person_id_person_info on person_info(person_id);
create index person_role_id_cast_info on cast_info(person_role_id);
create index role_id_cast_info on cast_info(role_id);




ALTER TABLE aka_name ADD FOREIGN KEY (person_id)  REFERENCES name(id);

ALTER TABLE aka_title ADD FOREIGN KEY (movie_id)  REFERENCES title(id);
ALTER TABLE aka_title ADD FOREIGN KEY (kind_id)  REFERENCES kind_type(id);
ALTER TABLE aka_title ADD FOREIGN KEY (episode_of_id)  REFERENCES title(id);

ALTER TABLE cast_info ADD FOREIGN KEY (person_id)  REFERENCES name(id);
ALTER TABLE cast_info ADD FOREIGN KEY (movie_id)  REFERENCES title(id);
ALTER TABLE cast_info ADD FOREIGN KEY (person_role_id)  REFERENCES char_name(id);
ALTER TABLE cast_info ADD FOREIGN KEY (role_id)  REFERENCES role_type(id);

ALTER TABLE complete_cast ADD FOREIGN KEY (movie_id)  REFERENCES title(id);
ALTER TABLE complete_cast ADD FOREIGN KEY (subject_id)  REFERENCES comp_cast_type(id);
--ALTER TABLE complete_cast ADD FOREIGN KEY (status_id)  REFERENCES comp_cast_type(id);

ALTER TABLE movie_companies ADD FOREIGN KEY (movie_id)  REFERENCES title(id);
ALTER TABLE movie_companies ADD FOREIGN KEY (company_id)  REFERENCES name(id);
ALTER TABLE movie_companies ADD FOREIGN KEY (company_type_id)  REFERENCES company_type(id);

ALTER TABLE movie_info ADD FOREIGN KEY (movie_id)  REFERENCES title(id);
ALTER TABLE movie_info ADD FOREIGN KEY (info_type_id)  REFERENCES info_type(id);

ALTER TABLE movie_link ADD FOREIGN KEY (movie_id)  REFERENCES title(id);
ALTER TABLE movie_link ADD FOREIGN KEY (linked_movie_id)  REFERENCES title(id);
ALTER TABLE movie_link ADD FOREIGN KEY (link_type_id)  REFERENCES link_type(id);

ALTER TABLE person_info ADD FOREIGN KEY (person_id)  REFERENCES name(id);
ALTER TABLE person_info ADD FOREIGN KEY (info_type_id)  REFERENCES info_type(id);

ALTER TABLE title ADD FOREIGN KEY (kind_id)  REFERENCES kind_type(id);
/*ALTER TABLE title ADD FOREIGN KEY (episode_of_id)  REFERENCES title(id);*/
/*
UPDATES
ALTER TABLE movie_info_idx ADD FOREIGN KEY (mii_movie_id)  REFERENCES title(t_id);
ALTER TABLE movie_info_idx ADD FOREIGN KEY (mii_info_type_id)  REFERENCES info_type(it_id);
ALTER TABLE movie_companies ADD FOREIGN KEY (mc_company_id) REFERENCES company_name(cn1_id);
ALTER TABLE movie_keyword ADD FOREIGN KEY (mk_movie_id) REFERENCES title(t_id);
ALTER TABLE movie_keyword ADD FOREIGN KEY (mk_keyword_id) REFERENCES keyword(k_id);
update aka_title set at_movie_id = 6 where at_movie_id = 0;
ALTER TABLE aka_title ADD FOREIGN KEY (at_movie_id) REFERENCES title(t_id);

*/