CREATE SEQUENCE hub.users_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE hub.profile (
    profile_id NUMBER(19) GENERATED AS IDENTITY,
    profile VARCHAR2(50 CHAR) NOT NULL UNIQUE CHECK (profile IN ('ADM','MOD','BASIC')),
    PRIMARY KEY (profile_id)
);

CREATE TABLE hub.users (
    user_id NUMBER(19) DEFAULT users_seq.nextval,
    name VARCHAR2(255) NOT NULL,
    email VARCHAR2(255) UNIQUE NOT NULL,
    password VARCHAR2(255) NOT NULL,
    profile_id NUMBER(19),
    PRIMARY KEY (user_id),
    CONSTRAINT FK_PROFILE_USERS FOREIGN KEY (profile_id) REFERENCES hub.profile(profile_id)
);