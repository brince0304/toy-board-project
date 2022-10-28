insert into user_account (created_at, created_by, modified_at, modified_by, email, memo, nickname, user_id, user_password) values ('2022-05-01 13:04:04', 'test', '2022-04-02 16:00:20', 'Adelaida', 'ablunt0@kickstarter.com', 'Vestibulum', 'test', 'test', '$2a$10$ESEkLtJ50G8ly/StrADSpOvqebjUwvCtY4/ZhtT2ClqO2jfGcSCCe');

insert into article (id,created_at, created_by, modified_at, modified_by, title, content, user_id) values (1,'2022-05-01 13:04:04', 'test', '2022-04-02 16:00:20', 'Adelaida', 'test', 'test', 'test');

insert into hashtag (id,hashtag) values (1,'text');

insert into article_hashtag (article_id, hashtag_id) values (1,1);