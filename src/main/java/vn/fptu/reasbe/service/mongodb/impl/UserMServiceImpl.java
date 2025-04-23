package vn.fptu.reasbe.service.mongodb.impl;


import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.fptu.reasbe.model.enums.user.StatusOnline;
import vn.fptu.reasbe.model.mongodb.User;
import vn.fptu.reasbe.repository.mongodb.UserMRepository;
import vn.fptu.reasbe.service.mongodb.UserMService;

@Service
@RequiredArgsConstructor
public class UserMServiceImpl implements UserMService {

    private final UserMRepository repository;

    @Override
    public void saveUser(User user) {
        repository.save(user);
    }

    @Override
    public void disconnect(User user) {
        User storedUser = repository.findById(user.getUserName()).orElse(null);
        if (storedUser != null) {
            storedUser.setStatusOnline(StatusOnline.OFFLINE);
            repository.save(storedUser);
        }
    }

    @Override
    public List<User> findConnectedUsers() {
        return repository.findAllByStatusOnline(StatusOnline.ONLINE);
    }

    @Override
    public User findByRefId(Integer refId) {
        return repository.findUserByRefIdIs(refId).orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return repository.findUserByUserName(username).orElse(null);
    }

    @Override
    public User getAdmin() {
        String adminName = "admin";
        return repository.findUserByUserName(adminName).orElse(null);
    }
}
