#!/bin/zsh
# My first script

echo "This is a script which runs dsqgen and creates postgres compatible query*.sql for query*.tpl"



#c
#mv query_0.sql query1.sql

while read template_name
do
	./dsqgen -DIRECTORY . -DIALECT netezza -SCALE 100 -QUIET Y -TEMPLATE $template_name
	mv query_0.sql `echo $template_name | sed 's/.\{4\}$//'`.sql
done < "templates.lst"


echo "Created .sql files"
