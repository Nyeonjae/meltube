package com.nyeonjae.meltube.services;

import com.nyeonjae.meltube.endtities.EmailTokenEntity;
import com.nyeonjae.meltube.endtities.UserEntity;
import com.nyeonjae.meltube.exceptions.TransactionalException;
import com.nyeonjae.meltube.maapers.UserMapper;
import com.nyeonjae.meltube.results.CommonResult;
import com.nyeonjae.meltube.results.Result;
import com.nyeonjae.meltube.results.user.RegisterResult;
import com.nyeonjae.meltube.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class UserService {
    private  final UserMapper userMapper;

    @Autowired
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Result register(UserEntity user) {

        if (user == null ||
                user.getEmail() == null || user.getEmail().length() < 8 || user.getEmail().length() > 50 ||
                user.getPassword() == null || user.getPassword().length() < 8 || user.getPassword().length() > 50 ||
                user.getNickname() == null || user.getNickname().length() < 2 || user.getNickname().length() > 10 ||
                user.getContact() == null || user.getContact().length() < 10 || user.getContact().length() > 12) {

            return CommonResult.FAILURE;
        }
        if (this.userMapper.selectUserByContact(user.getEmail()) != null) {

            return RegisterResult.FAILURE_DUPLICATE_CONTACT;
        }
        if (this.userMapper.selectUserByNickname(user.getNickname()) != null) {

            return RegisterResult.FAILURE_DUPLICATE_NICKNAME;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeletedAt(null);
        user.setAdmin(false);
        user.setSuspended(false);
        user.setVerified(false);
        if (this.userMapper.insertUser(user) == 0) {
            throw new TransactionalException();
        }
        EmailTokenEntity emailToken = new EmailTokenEntity();
        emailToken.setUserEmail(user.getEmail());
        emailToken.setKey(CryptoUtils.hashSha512(String.format("%s%s%f%f",
                user.getEmail(),
                user.getPassword(),
                Math.random(),
                Math.random())));
        emailToken.setCreatedAt(LocalDateTime.now());
        emailToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        emailToken.setUsed(false);
        // TODO eamilToken INSERT 하기
        // TODO Transactional 걸고 설명하기
        // TODO 이메일 보내기

        return CommonResult.SUCCESS;
    }
}



