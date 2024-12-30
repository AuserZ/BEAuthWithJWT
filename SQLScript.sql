create table if not exists db_auth.tbl_user
        (
            id                                 varchar(32) default '0' not null,
            username                           varchar(255)            not null,
            password                           varchar(255)            not null,
            last_login                         datetime                null on update CURRENT_TIMESTAMP,
            created_datetime                   datetime                null,
            refresh_token                      varchar(255)            null,
            email                              varchar(255)            not null,
            last_update_refresh_token_datetime datetime                null
        );

