package com.orcchg.vikstra.app.ui.viewobject.mapper;

import com.orcchg.vikstra.app.ui.viewobject.UserVO;
import com.orcchg.vikstra.domain.model.User;
import com.orcchg.vikstra.domain.model.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class UserToVoMapper implements Mapper<User, UserVO> {

    @Inject
    public UserToVoMapper() {
    }

    @Override
    public UserVO map(User object) {
        return UserVO.create(object.photoUrl());
    }

    @Override
    public List<UserVO> map(List<User> list) {
        List<UserVO> mapped = new ArrayList<>();
        for (User item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }
}
