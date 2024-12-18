package com.raul.forumhub.topic.util;

import com.raul.forumhub.topic.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class TestsHelper {

    public static final class TopicHelper {
        public static List<Topic> topicList() {
            final List<Topic> topicList = new ArrayList<>();
            topicList.add(Topic.builder()
                    .id(1L)
                    .title("Dúvida na utilização do Feign Client")
                    .question("Como utilizar o Feign Client para integração do serviço x?")
                    .createdAt(LocalDateTime.of(2024, 8, 10, 20, 5))
                    .status(Status.UNSOLVED)
                    .author(AuthorHelper.authorList().get(0))
                    .course(CourseHelper.courseList().get(0))
                    .build()
            );
            topicList.add(Topic.builder()
                    .id(2L)
                    .title("Dúvida na utilização do OpenShift")
                    .question("Como utilizar o Rosa/OpenShift para implantação do serviço x?")
                    .createdAt(LocalDateTime.of(2023, 10, 10, 15, 5))
                    .status(Status.UNSOLVED)
                    .author(AuthorHelper.authorList().get(1))
                    .course(CourseHelper.courseList().get(1))
                    .build()
            );
            topicList.add(Topic.builder()
                    .id(3L)
                    .title("Dúvida em relação ao teste end-to-end")
                    .question("Quais as boas práticas na execução dos testes end-to-end?")
                    .createdAt(LocalDateTime.of(2024, 10, 1, 11, 0))
                    .status(Status.SOLVED)
                    .author(AuthorHelper.authorList().get(3))
                    .course(CourseHelper.courseList().get(2))
                    .build()
            );
            topicList.add(Topic.builder()
                    .id(4L)
                    .title("Dúvida quanto a configuração dos testes unitários")
                    .question("Não consigo entender por que x configuração não funciona")
                    .createdAt(LocalDateTime.of(2023, 1, 15, 14, 0))
                    .status(Status.UNSOLVED)
                    .author(AuthorHelper.authorList().get(0))
                    .course(CourseHelper.courseList().get(0))
                    .answers(Collections.emptySet())
                    .build());
            return topicList;
        }

        public static List<Topic> topicListWithAnswers() {
            final Topic topic1 = TestsHelper.TopicHelper.topicList().get(0);
            topic1.setAnswers(Set.of(
                    TestsHelper.AnswerHelper.answerList().get(0),
                    TestsHelper.AnswerHelper.answerList().get(3)));
            final Topic topic2 = TestsHelper.TopicHelper.topicList().get(1);
            topic2.setAnswers(Set.of(TestsHelper.AnswerHelper.answerList().get(1)));
            final Topic topic3 = TestsHelper.TopicHelper.topicList().get(2);
            topic3.setAnswers(Set.of(TestsHelper.AnswerHelper.answerList().get(2)));

            return new ArrayList<>(List.of(topic1, topic2, topic3));

        }


    }

    public static final class AnswerHelper {
        public static List<Answer> answerList() {
            final List<Answer> answerList = new ArrayList<>();
            answerList.add(Answer.builder()
                    .id(1L)
                    .topic(TopicHelper.topicList().get(0))
                    .solution("Resposta do primeiro tópico")
                    .bestAnswer(false)
                    .createdAt(LocalDateTime.now())
                    .author(AuthorHelper.authorList().get(1))
                    .build()
            );
            answerList.add(Answer.builder()
                    .id(2L)
                    .topic(TopicHelper.topicList().get(1))
                    .solution("Resposta do segundo tópico")
                    .bestAnswer(true)
                    .createdAt(LocalDateTime.now())
                    .author(AuthorHelper.authorList().get(0))
                    .build()
            );
            answerList.add(Answer.builder()
                    .id(3L)
                    .topic(TopicHelper.topicList().get(2))
                    .solution("Resposta do terceiro tópico")
                    .bestAnswer(false)
                    .createdAt(LocalDateTime.now())
                    .author(AuthorHelper.authorList().get(2))
                    .build()
            );
            answerList.add(Answer.builder()
                    .id(4L)
                    .topic(TopicHelper.topicList().get(0))
                    .solution("Resposta do primeiro tópico")
                    .bestAnswer(false)
                    .createdAt(LocalDateTime.now())
                    .author(AuthorHelper.authorList().get(3))
                    .build()
            );
            return answerList;
        }


    }

    public static final class CourseHelper {
        public static List<Course> courseList() {
            final List<Course> courseList = new ArrayList<>();
            courseList.add(Course.builder()
                    .id(1L)
                    .name("Criação de uma API Rest")
                    .category(Course.Category.JAVA)
                    .build()
            );
            courseList.add(Course.builder()
                    .id(2L)
                    .name("Gerenciamento de contêiners")
                    .category(Course.Category.COMPUTATION)
                    .build()
            );
            courseList.add(Course.builder()
                    .id(3L)
                    .name("Lidando com testes")
                    .category(Course.Category.QA)
                    .build()
            );
            return courseList;
        }

    }

    public static final class AuthorHelper {
        public static List<Author> authorList() {
            final List<Author> authorList = new ArrayList<>();
            authorList.add(Author.builder()
                    .id(1L)
                    .username("Jose")
                    .email("jose@email.com")
                    .profile(ProfileHelper.profileList().get(0))
                    .build()
            );
            authorList.add(Author.builder()
                    .id(2L)
                    .username("Maria")
                    .email("maria@email.com")
                    .profile(ProfileHelper.profileList().get(1))
                    .build()
            );
            authorList.add(Author.builder()
                    .id(3L)
                    .username("Joao")
                    .email("joao@email.com")
                    .profile(ProfileHelper.profileList().get(2))
                    .build()
            );
            authorList.add(Author.builder()
                    .id(4L)
                    .username("Desconhecido")
                    .email("desconhecido@email.com")
                    .profile(ProfileHelper.profileList().get(0))
                    .build()
            );
            return authorList;
        }


    }

    public static final class ProfileHelper {
        public static List<Profile> profileList() {
            final List<Profile> profileList = new ArrayList<>();
            profileList.add(Profile.builder()
                    .id(1L)
                    .profileName(Profile.ProfileName.BASIC)
                    .build()
            );
            profileList.add(Profile.builder()
                    .id(2L)
                    .profileName(Profile.ProfileName.MOD)
                    .build()
            );
            profileList.add(Profile.builder()
                    .id(3L)
                    .profileName(Profile.ProfileName.ADM)
                    .build()
            );
            return profileList;
        }


    }

}
