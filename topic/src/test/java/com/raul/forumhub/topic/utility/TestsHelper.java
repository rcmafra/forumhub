package com.raul.forumhub.topic.utility;

import com.raul.forumhub.topic.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class TestsHelper {

    public static final class TopicHelper {
        public static List<Topic> topicList() {
            List<Topic> topicList = new ArrayList<>();
            topicList.add(new Topic(
                    1L,
                    "Dúvida na utilização do Feign Client",
                    "Como utilizar o Feign Client para integração do serviço x?",
                    LocalDateTime.of(2024, 8, 10, 20, 5),
                    Status.UNSOLVED,
                    AuthorHelper.authorList().get(0),
                    CourseHelper.courseList().get(0)
            ));
            topicList.add(new Topic(
                    2L,
                    "Dúvida na utilização do OpenShift",
                    "Como utilizar o Rosa/OpenShift para implantação do serviço x?",
                    LocalDateTime.of(2023, 10, 10, 15, 5),
                    Status.UNSOLVED,
                    AuthorHelper.authorList().get(1),
                    CourseHelper.courseList().get(1)
            ));
            topicList.add(new Topic(
                    3L,
                    "Dúvida em relação ao teste end-to-end",
                    "Quais as boas práticas na execução dos testes end-to-end?",
                    LocalDateTime.of(2024, 10, 1, 11, 0),
                    Status.SOLVED,
                    AuthorHelper.authorList().get(3),
                    CourseHelper.courseList().get(2)
            ));
            return topicList;
        }


    }

    public static final class AnswerHelper {
        public static List<Answer> answerList() {
            List<Answer> answerList = new ArrayList<>();
            answerList.add(new Answer(1L, TopicHelper.topicList().get(0), "Resposta do primeiro tópico",
                    false, LocalDateTime.now(), AuthorHelper.authorList().get(1)));
            answerList.add(new Answer(2L, TopicHelper.topicList().get(1), "Resposta do segundo tópico",
                    true, LocalDateTime.now(), AuthorHelper.authorList().get(0)));
            answerList.add(new Answer(3L, TopicHelper.topicList().get(2), "Resposta do terceiro tópico",
                    false, LocalDateTime.now(), AuthorHelper.authorList().get(2)));
            answerList.add(new Answer(4L, TopicHelper.topicList().get(0), "Resposta do primeiro tópico",
                    false, LocalDateTime.now(), AuthorHelper.authorList().get(3)));
            return answerList;
        }


    }

    public static final class CourseHelper {
        public static List<Course> courseList() {
            List<Course> courseList = new ArrayList<>();
            courseList.add(new Course(1L, "Criação de uma API Rest", Course.Category.JAVA));
            courseList.add(new Course(2L, "Gerenciamento de contêiners", Course.Category.COMPUTATION));
            courseList.add(new Course(3L, "Lidando com testes", Course.Category.QA));
            return courseList;
        }

    }

    public static final class AuthorHelper {
        public static List<Author> authorList() {
            List<Author> authorList = new ArrayList<>();
            authorList.add(new Author(1L, "Jose", "jose@email.com", ProfileHelper.profileList().get(0)));
            authorList.add(new Author(2L, "Maria", "maria@email.com", ProfileHelper.profileList().get(1)));
            authorList.add(new Author(3L, "Joao", "joao@email.com", ProfileHelper.profileList().get(2)));
            authorList.add(new Author(4L, "Desconhecido", "desconhecido@email.com", ProfileHelper.profileList().get(0)));
            return authorList;
        }


    }

    public static final class ProfileHelper {
        public static List<Profile> profileList() {
            List<Profile> profileList = new ArrayList<>();
            profileList.add(new Profile(1L, Profile.ProfileName.BASIC));
            profileList.add(new Profile(2L, Profile.ProfileName.MOD));
            profileList.add(new Profile(3L, Profile.ProfileName.ADM));
            return profileList;
        }


    }

}
