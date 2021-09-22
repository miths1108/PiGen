SELECT * FROM
    t03,
    t06,
    t04,
    t05,
    t13
WHERE 1=1 
    AND t03_F_t13 = t13_P
    AND t03_F_t04 = t04_P
    AND t03_F_t06 = t06_P
    AND t04_F_t05 = t05_P
    AND ((t06_c006 = 2) AND (t06_c007 = 2) AND (t06_c005 = 2)) AND ((t04_c002 = 8) OR (t04_c002 = 6) OR (t04_c002 = 12) OR (t04_c002 = 4) OR (t04_c002 = 2) OR (t04_c002 = 10)) AND ((t05_c007 = 22) OR (t05_c007 = 6) OR (t05_c007 = 4) OR (t05_c007 = 12) OR (t05_c007 = 2) OR (t05_c007 = 18) OR (t05_c007 = 16)) AND ((t13_c004 = 2) OR (t13_c005 = 2))
;