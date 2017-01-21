MYSQL_PORT=$$(docker-compose port mysql 3306 | awk -F':' '{print $$2}')

db:
	MYSQL_PWD=password mysql -u root --port $(MYSQL_PORT) --protocol tcp example_test

up:
	docker-compose up -d

implode:
	docker-compose stop; docker-compose rm; rm -rf tmp
