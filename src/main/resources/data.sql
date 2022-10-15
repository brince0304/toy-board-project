insert into user_account (id, created_at, created_by, modified_at, modified_by, email, memo, nickname, user_id, user_password) values (1, '2022-05-01 13:04:04', 'brince', '2022-04-02 16:00:20', 'Adelaida', 'ablunt0@kickstarter.com', 'Vestibulum', 'brince', 'brince', '1234');

insert into article (id, created_at, created_by, modified_at, modified_by, content, hashtag, title, user_account_id) values (1, '2022-01-31 03:12:59', 'Lisbeth', '2021-12-31 00:41:44', 'Babb', 'Curabitur gravida nisi at nibh. In hac habitasse platea dictumst. Aliquam augue quam, sollicitudin vitae, consectetuer eget, rutrum at, lorem.

Integer tincidunt ante vel ipsum. Praesent blandit lacinia erat. Vestibulum sed magna at nunc commodo placerat.

Praesent blandit. Nam nulla. Integer pede justo, lacinia eget, tincidunt eget, tempus vel, pede.', 'Teal', 'Charlotte''s Web', 1);


insert into article_comment (id, created_at, created_by, modified_at, modified_by, content, user_account_id, article_id) values (1, '2022-10-11 21:48:43', 'Barde', '2021-11-04 11:55:27', 'Gibby', 'Cras non velit nec nisi vulputate nonummy. Maecenas tincidunt lacus at velit. Vivamus vel nulla eget eros elementum pellentesque.

Quisque porta volutpat erat. Quisque erat eros, viverra eget, congue eget, semper rutrum, nulla. Nunc purus.', 1, 1);
