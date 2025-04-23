package vn.fptu.reasbe.service.mongodb;

import java.util.List;

import vn.fptu.reasbe.model.mongodb.User;

/**
 *
 * @author dungnguyen
 */
public interface UserMService {
    void saveUser(User user);
    void disconnect(User user);
    List<User> findConnectedUsers();
    User findByRefId(Integer refId);
    User findByUsername(String username);
    User getAdmin();
}
