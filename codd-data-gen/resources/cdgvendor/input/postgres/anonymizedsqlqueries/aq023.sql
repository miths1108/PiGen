SELECT * FROM
    t04,
    t05
WHERE 1=1 
    AND t04_F_t05 = t05_P
    AND (t05_c004 = 10) OR (t05_c004 = 8) OR (t05_c004 = 4) OR (t05_c004 = 2) OR (t05_c004 = 6)
;