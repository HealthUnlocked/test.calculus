use example_test;

create table forum (
  forum_id    bigint       not null auto_increment,
  name        varchar(100) not null,
  description text         not null,
  date_created datetime(3) not null default current_timestamp(3),

  primary key (forum_id)
);

create table user (
  user_id  bigint          not null auto_increment,
  username varchar(100)    not null,
  email    varchar(100)    not null,
  date_created datetime(3) not null default current_timestamp(3),

  primary key (user_id)
);

create table membership (
  forum_id     bigint      not null,
  user_id      bigint      not null,
  date_created datetime(3) not null default current_timestamp(3),

  primary key (forum_id, user_id),

  foreign key (user_id)  references user  (user_id),
  foreign key (forum_id) references forum (forum_id)
);

create table post (
  post_id      bigint       not null auto_increment,
  forum_id     bigint       not null,
  user_id      bigint       not null,
  title        varchar(100) not null,
  body         text         not null,
  date_created datetime(3)  not null default current_timestamp(3),

  primary key (post_id),

  foreign key (user_id)  references user  (user_id),
  foreign key (forum_id) references forum (forum_id)
);
