create database if not exists example_test character set utf8mb4 collate utf8mb4_unicode_ci;
grant all privileges on example_test.* to example_test;

set password for example_test = password('password');
