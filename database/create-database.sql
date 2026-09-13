drop database if exists flexpath_final;
create database if not exists flexpath_final;
use flexpath_final;

drop table if exists encounter_monster, encounter_player_character, encounter, player_character, monster, roles, users;

create table users (
    username varchar(255) primary key,
    password varchar(255)
);

create table roles (
    username varchar(255) not null,
    role varchar(250) not null,
    primary key (username, role),
    foreign key (username) references users(username) on delete cascade
);

-- table to store the different monster types that could be used in a battle
-- since you can homebrew, the creator's username is referenced
create table monster (
    id int primary key auto_increment,
    name varchar(100) not null,
    monster_type varchar(50) not null,
    challenge_rating decimal(4,2) not null,
    armor_class int not null,
    health int not null,
    description text,
    is_public boolean not null default false,
    creator_username varchar(255) not null,
    created_at timestamp default current_timestamp,
    foreign key (creator_username) references users(username) on delete cascade
);

-- table to store the different player characters to add to a battle
-- since this is homebrewed, the creator's username is referenced
create table player_character (
    id int primary key auto_increment,
    name varchar(100) not null,
    character_class varchar(50) not null,
    level int not null,
    armor_class int not null,
    health int not null,
    description text,
    is_public boolean not null default false,
    creator_username varchar(255) not null,
    created_at timestamp default current_timestamp,
    foreign key (creator_username) references users(username) on delete cascade
);

-- table to store the encounter
-- since this can be referenced later by the user, the username is referenced
create table encounter (
    id int primary key auto_increment,
    name varchar(100) not null,
    description text,
    is_public boolean not null default false,
    creator_username varchar(255) not null,
    created_at timestamp default current_timestamp,
    foreign key (creator_username) references users(username) on delete cascade
);
-- join table for the many/many of encounter and monster
-- allows for tracking the specific monster's stats in the specific encounter
-- foreign key references monster and encounter id
create table encounter_monster (
    id int primary key auto_increment,
    encounter_id int not null,
    monster_id int not null,
    current_health int not null,
    foreign key (encounter_id) references encounter(id) on delete cascade,
    foreign key (monster_id) references monster(id) on delete cascade
);

-- join table for the many/many of encounter and player character
-- allows for tracking the specifc character's stats in the specific encounter
-- foreign key references character and encounter id
create table encounter_player_character (
    id int primary key auto_increment,
    encounter_id int not null,
    player_character_id int not null,
    current_health int not null,
    foreign key (encounter_id) references encounter(id) on delete cascade,
    foreign key (player_character_id) references player_character(id) on delete cascade
);


insert into users (username, password) values ('admin', '$2a$10$tBTfzHzjmQVKza3VSa5lsOX6/iL93xPVLlLXYg2FhT6a.jb1o6VDq');
insert into roles (username, role) values ('admin', 'ADMIN');

insert into users (username, password) values ('user', '$2a$10$tBTfzHzjmQVKza3VSa5lsOX6/iL93xPVLlLXYg2FhT6a.jb1o6VDq');
insert into roles (username, role) values ('user', 'USER');
