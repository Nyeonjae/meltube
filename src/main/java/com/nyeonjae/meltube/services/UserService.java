package com.nyeonjae.meltube.services;

import com.nyeonjae.meltube.entities.EmailTokenEntity;
import com.nyeonjae.meltube.entities.UserEntity;
import com.nyeonjae.meltube.exceptions.TransactionalException;
import com.nyeonjae.meltube.mappers.EmailTokenMapper;
import com.nyeonjae.meltube.mappers.UserMapper;
import com.nyeonjae.meltube.results.CommonResult;
import com.nyeonjae.meltube.results.Result;
import com.nyeonjae.meltube.results.user.RegisterResult;
import com.nyeonjae.meltube.results.user.ValidateEmailTokenResult;
import com.nyeonjae.meltube.utils.CryptoUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;


@Service
public class UserService {
    private  final UserMapper userMapper;
    private  final EmailTokenMapper emailTokenMapper;
    private  final JavaMailSender mailSender; // 메일을 보내는데 필요함
    private  final SpringTemplateEngine templateEngine ; // thymeleaf 요소를 메일로 보내는 역할

    @Autowired
    public UserService(UserMapper userMapper, EmailTokenMapper emailTokenMapper, JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.userMapper = userMapper;
        this.emailTokenMapper = emailTokenMapper;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Transactional
    public Result register(HttpServletRequest request, UserEntity user) throws MessagingException {

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
        if (this.emailTokenMapper.insertEmailToken(emailToken) == 0) {
            throw new TransactionalException();
        }
        String validationLink = String.format("%s://%s:%d/user/validate-email-token?userEmail=%s&key=%s",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort(),
                emailToken.getUserEmail(),
                emailToken.getKey());
        //org.thymeleaf Context
        Context context = new Context();
        context.setVariable("validationLink", validationLink);
        String mailText = this.templateEngine.process("email/register", context); // "<!DOCTYPE>..." 에 넣어서 문자열로 처리하겠다.

        MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setFrom("nyeonjae94@gmail.com");
        mimeMessageHelper.setTo(emailToken.getUserEmail());
        mimeMessageHelper.setSubject("[멜튜브] 회원가입 인증 링크");
        mimeMessageHelper.setText(mailText, true);
        this.mailSender.send(mimeMessage);

        return CommonResult.SUCCESS;
    }
    public Result validateEmailToken(EmailTokenEntity emailToken) {
        if (emailToken == null ||
        emailToken.getUserEmail() == null || emailToken.getUserEmail().length() < 8 || emailToken.getUserEmail().length() > 50 || emailToken.getKey() == null || emailToken.getKey().length() != 128) {
            return CommonResult.FAILURE;
        }
        EmailTokenEntity dbEmailToken = this.emailTokenMapper.selectEmailTokenByUserEmailAndKey(emailToken.getUserEmail(), emailToken.getKey());
        if (dbEmailToken == null || dbEmailToken.isUsed()) {
            return CommonResult.FAILURE;
        }
        if (dbEmailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ValidateEmailTokenResult.FAILURE_EXPIRED;
        }
        dbEmailToken.setUsed(true);
        if (this.emailTokenMapper.updateEmailToken(dbEmailToken) == 0) {
            throw new TransactionalException();
        }
        UserEntity user = this.userMapper.selectUserByEmail(emailToken.getUserEmail());
        user.setVerified(true);
        if (this.userMapper.updateUser(user) == 0) {
            throw new TransactionalException();
        }
        return CommonResult.SUCCESS;
    }


}



