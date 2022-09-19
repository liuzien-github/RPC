package cn.mine.service.impl;

import cn.mine.service.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public String getUserName(int userId) {
        return "" + userId;
    }
}
