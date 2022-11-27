CREATE TABLE IF NOT EXISTS ITEM
(
    id          bigint not null auto_increment,
    name        varchar(100),
    price       float,
    description varchar(255),
    primary key (id)
);
insert into item (name, price, description) values ('Round Widget', 2.99, 'A widget that is round');
insert into item (name, price, description) values ('Square Widget', 1.99, 'A widget that is square');