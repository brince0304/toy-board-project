insert into SAVE_FILE (CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY, FILE_NAME, FILE_PATH, FILE_SIZE, FILE_TYPE, UPLOAD_USER) VALUES ( now(), 'test', now(), 'test', 'test', 'test', 0, 'test', 'test');
insert into USER_ACCOUNT (USER_ID,CREATED_AT,CREATED_BY,MODIFIED_AT,MODIFIED_BY,EMAIL,MEMO,NICKNAME,USER_PASSWORD,PROFILE_IMG_ID) values ('test',now(),'test',now(),'test','test2','test', 'test','$2a$10$fuRt3zQSdOWFlXSjqWFP8.Ya0bj0dv1qIu535nJIUrFOsQ7AhoI66',1);

insert into USER_ACCOUNT_ROLES (USER_ACCOUNT_USER_ID, ROLES) values ('test','ROLE_USER');
