package com.kouetcha.service.tasksmanager;

import com.kouetcha.model.base.BaseEmail;

import java.util.List;

public interface EmailService<E extends BaseEmail<T>, T> {

    E addEmail(Long parentId, String email);

    void removeEmail(Long emailId);

    E activateEmail(Long emailId);

    E updateEmail(Long emailId, String emailValue);

    E deactivateEmail(Long emailId);

    List<E> findByParent(Long parentId);
}