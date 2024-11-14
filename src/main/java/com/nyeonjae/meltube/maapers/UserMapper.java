package com.nyeonjae.meltube.maapers;

import com.nyeonjae.meltube.endtities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insertUser(UserEntity user);

    UserEntity selectUserByEmail(@Param("email") String email);

    UserEntity selectUserByContact(@Param("contact") String contact);



    UserEntity selectUserByNickname(@Param("nickname") String nickname);
}
