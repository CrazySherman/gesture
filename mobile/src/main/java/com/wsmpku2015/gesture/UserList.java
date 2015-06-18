package com.wsmpku2015.gesture;

import java.util.ArrayList;

/**
 * Created by wsm on 4/24/15.
 */
public class UserList {
    private ArrayList<User> list;

    public UserList() {
        list = new ArrayList<User>();
    }
    public User find(int id) {
        for (User curUser : list) {
            if (curUser.getUid() == id)
                return curUser;
        }
        User newUser = new User(id);
        list.add(newUser);
        return newUser;
    }
}
