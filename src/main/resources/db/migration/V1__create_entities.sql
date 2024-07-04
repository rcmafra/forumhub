CREATE TABLE hub.profile (
    profile_id NUMBER(19) GENERATED AS IDENTITY,
    profile VARCHAR2(50 CHAR) NOT NULL UNIQUE CHECK (profile IN ('ADM','MOD','BASIC')),
    PRIMARY KEY (profile_id)
);

CREATE TABLE hub.users (
    user_id NUMBER(19) NOT NULL,
    name VARCHAR2(255) NOT NULL,
    email VARCHAR2(255) UNIQUE NOT NULL,
    password VARCHAR2(255) NOT NULL,
    profile_id NUMBER(19),
    PRIMARY KEY (user_id),
    FOREIGN KEY (profile_id) REFERENCES hub.profile(profile_id)
);

CREATE TABLE hub.course (
    course_id NUMBER(19) GENERATED AS IDENTITY,
    name VARCHAR2(255),
    category VARCHAR2(50 CHAR) NOT NULL UNIQUE CHECK (category IN ('JAVA','C','CPLUSPLUS','CSHARP','GOLANG','COMPUTATION','QA')),
    PRIMARY KEY (course_id)
);

CREATE TABLE hub.topic (
    topic_id NUMBER(19) GENERATED AS IDENTITY,
    title VARCHAR2(200) NOT NULL,
    message VARCHAR2(255) NOT NULL,
    created_at TIMESTAMP(6) WITH LOCAL TIME ZONE,
    status VARCHAR2(10 CHAR) NOT NULL UNIQUE CHECK (status IN ('SOLVED', 'UNSOLVED')),
    user_id NUMBER(19),
    course_id NUMBER(19),
    PRIMARY KEY (topic_id),
    FOREIGN KEY (user_id) REFERENCES hub.users(user_id),
    FOREIGN KEY (course_id) REFERENCES hub.course(course_id)
);

CREATE TABLE hub.answer (
    answer_id NUMBER(19) GENERATED AS IDENTITY,
    better_answer NUMBER(1,0) NOT NULL CHECK (better_answer in (0,1)),
    topic_id NUMBER(19),
    user_id NUMBER(19),
    solution VARCHAR2(255) NOT NULL,
    created_at TIMESTAMP(6) WITH LOCAL TIME ZONE,
    PRIMARY KEY (answer_id),
    FOREIGN KEY (user_id) REFERENCES hub.users(user_id),
    FOREIGN KEY (topic_id) REFERENCES hub.topic(topic_id),
    FOREIGN KEY (answer_id) REFERENCES hub.topic(topic_id)
);