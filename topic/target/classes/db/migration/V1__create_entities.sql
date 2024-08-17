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

CREATE TABLE hub.course (
    course_id NUMBER(19) GENERATED AS IDENTITY,
    name VARCHAR2(255) NOT NULL UNIQUE,
    category VARCHAR2(50 CHAR) NOT NULL CHECK (category IN ('JAVA','C','CPLUSPLUS','CSHARP','GOLANG','COMPUTATION','QA')),
    PRIMARY KEY (course_id)
);

CREATE TABLE hub.topic (
    topic_id NUMBER(19) GENERATED AS IDENTITY,
    title VARCHAR2(200) NOT NULL,
    message VARCHAR2(255) NOT NULL,
    status VARCHAR2(10 CHAR) DEFAULT 'UNSOLVED' CHECK (status IN ('SOLVED', 'UNSOLVED')),
    created_at TIMESTAMP(6) WITH LOCAL TIME ZONE,
    user_id NUMBER(19),
    course_id NUMBER(19),
    PRIMARY KEY (topic_id),
    CONSTRAINT FK_USERS_TOPIC FOREIGN KEY (user_id) REFERENCES hub.users(user_id),
    CONSTRAINT FK_COURSE_TOPIC FOREIGN KEY (course_id) REFERENCES hub.course(course_id)
);

CREATE TABLE hub.answer (
    answer_id NUMBER(19) GENERATED AS IDENTITY,
    solution VARCHAR2(255) NOT NULL,
    best_answer NUMBER(1,0) DEFAULT 0 CHECK (better_answer in (0,1)),
    created_at TIMESTAMP(6) WITH LOCAL TIME ZONE,
    topic_id NUMBER(19),
    user_id NUMBER(19),
    PRIMARY KEY (answer_id),
    CONSTRAINT FK_USERS_ANSWER FOREIGN KEY (user_id) REFERENCES hub.users(user_id),
    CONSTRAINT FK_TOPIC_ANSWER FOREIGN KEY (topic_id) REFERENCES hub.topic(topic_id)
);

CREATE SEQUENCE hub.users_seq START WITH 1 INCREMENT BY 50;