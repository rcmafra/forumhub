package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.domain.Course;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    public List<Course.Category> getCategories() {
        return List.of(Course.Category.values());
    }
}
